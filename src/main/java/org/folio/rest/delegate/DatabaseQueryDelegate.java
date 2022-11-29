package org.folio.rest.delegate;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.DatabaseQueryTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;

@Service
@Scope("prototype")
public class DatabaseQueryDelegate extends AbstractDatabaseOutputDelegate {

  private Expression query;

  private Expression outputPath;

  private Expression resultType;

  private Expression includeHeader;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String queryTemplate = this.query.getValue(execution).toString();

    StringTemplateLoader stringLoader = new StringTemplateLoader();
    stringLoader.putTemplate("query", queryTemplate);

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    cfg.setTemplateLoader(stringLoader);

    Map<String, Object> inputs = getInputs(execution);

    String key = this.designation.getValue(execution).toString();
    String query = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("query"), inputs);

    Boolean includeHeader = this.includeHeader != null ?
      (this.includeHeader.getValue(execution) != null ?
        Boolean.parseBoolean(this.includeHeader.getValue(execution).toString())
          : false)
      : false;

    Connection conn = connectionService.getConnection(key);

    try (Statement statement = conn.createStatement()) {
      statement.execute(query);

      ResultSet results = null;
      if (statement.getUpdateCount() == -1) {
        results = statement.getResultSet();

        ResultOp resultOp;

        if (Objects.nonNull(this.outputPath)) {
          String outputPathTemplate = this.outputPath.getValue(execution).toString();
          stringLoader.putTemplate("outputPath", outputPathTemplate);

          String outputPath = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("outputPath"), inputs);
          String resultType = this.resultType.getValue(execution).toString();
          resultOp = new FileResultOp(results, outputPath, resultType, includeHeader);
        } else {
          resultOp = new VariableResultOp(execution, results);
        }

        int count = 0; 
        while (results.next()) {
          resultOp.next();
          count++;
        }

        if (resultOp instanceof FileResultOp) {
          if (hasOutputVariable(execution)) {
            setOutput(execution, count);
          } else {
            logger.info("{} did not specify output variable for result count", delegateName);
          }
        }

        resultOp.finish();
      }
    } finally {
      conn.close();
    }

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setQuery(Expression query) {
    this.query = query;
  }

  public void setOutputPath(Expression outputPath) {
    this.outputPath = outputPath;
  }

  public void setResultType(Expression resultType) {
    this.resultType = resultType;
  }

  public void setIncludeHeader(Expression includeHeader) {
    this.includeHeader = includeHeader;
  }

  @Override
  public Class<?> fromTask() {
    return DatabaseQueryTask.class;
  }

  private interface ResultOp {
    void next() throws Exception;

    void finish() throws Exception;
  }

  private class VariableResultOp implements ResultOp {

    private final DelegateExecution execution;

    private final ResultSet results;

    private final ResultSetMetaData metadata;

    private List<JsonNode> output = new ArrayList<>();

    public VariableResultOp(DelegateExecution execution, ResultSet results) throws SQLException {
      this.execution = execution;
      this.results = results;
      this.metadata = results.getMetaData();
    }

    @Override
    public void next() throws SQLException {
      ObjectNode row = objectMapper.createObjectNode();
      for (int count = 1; count <= metadata.getColumnCount(); ++count) {
        String columnName = metadata.getColumnName(count);
        // TODO: consider types; int, date, boolean, string, etc.
        row.put(columnName, results.getString(columnName));
      }
      output.add(row);
    }

    @Override
    public void finish() throws JsonProcessingException {
      setOutput(execution, output);
    }

  }

  private class FileResultOp implements ResultOp {

    private final ResultSet results;

    private final FileWriter fw;

    private final DatabaseResultTypeOp rowOp;

    public FileResultOp(ResultSet results, String path, String resultType, Boolean includeHeader) throws SQLException, IOException {
      this.results = results;
      this.fw = new FileWriter(path);
      this.rowOp = DatabaseResultTypeOp.valueOf(resultType);

      logger.info("Writing results to file as {}{}", rowOp, includeHeader ? " including headers" : "");
      if (includeHeader) {
        if (rowOp == DatabaseResultTypeOp.CSV) {
          fw.write(buildHeader(results, ","));
        } else if(rowOp == DatabaseResultTypeOp.TSV) {
          fw.write(buildHeader(results, "\t"));
        } else {
          logger.warn("{} does not support building headers", rowOp);
        }
      }
    }

    @Override
    public void next() throws Exception {
      rowOp.process(fw, results);
    }

    @Override
    public void finish() throws IOException {
      this.fw.close();
    }

  }

  private interface RowOp {
    void process(FileWriter fw, ResultSet results) throws Exception;
  }

  private static String buildHeader(ResultSet results, String delimiter) throws SQLException, IOException {
    StringBuilder builder = new StringBuilder();
    ResultSetMetaData metadata = results.getMetaData();
    for (int count = 1; count <= metadata.getColumnCount(); ++count) {
      String columnName = metadata.getColumnName(count);
      if (Objects.nonNull(columnName)) {
        builder.append(columnName);
      }
      if (count < metadata.getColumnCount()) {
        builder.append(delimiter);
      }
    }
    builder.append("\n");
    return builder.toString();
  }

  private enum DatabaseResultTypeOp implements RowOp {
    CSV() {
      public void process(FileWriter fw, ResultSet results) throws SQLException, IOException {
        fw.write(processDelimited(results, ","));
      }
    },
    TSV() {
      public void process(FileWriter fw, ResultSet results) throws SQLException, IOException {
        fw.write(processDelimited(results, "\t"));
      }
    },
    JSON() {
      public void process(FileWriter fw, ResultSet results) throws SQLException, IOException {
        StringBuilder builder = new StringBuilder("{");
        ResultSetMetaData metadata = results.getMetaData();
        for (int count = 1; count <= metadata.getColumnCount(); ++count) {
          String columnName = metadata.getColumnName(count);
          String value = results.getString(columnName);
          if (Objects.nonNull(value)) {
            builder.append("\"")
              .append(columnName)
              .append("\":\"")
              .append(value)
              .append("\"");
          }
        }
        builder.append("}").append("\n");
        fw.write(builder.toString());
      }
    };

    private static String processDelimited(ResultSet results, String delimiter) throws SQLException, IOException {
      StringBuilder builder = new StringBuilder();
      ResultSetMetaData metadata = results.getMetaData();
      for (int count = 1; count <= metadata.getColumnCount(); ++count) {
        String columnName = metadata.getColumnName(count);
        String value = results.getString(columnName);
        if (Objects.nonNull(value)) {
          builder.append(value);
        }
        if (count < metadata.getColumnCount()) {
          builder.append(delimiter);
        }
      }
      builder.append("\n");
      return builder.toString();
    }
  }

}
