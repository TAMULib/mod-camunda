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
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.DatabaseQueryTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class DatabaseQueryDelegate extends AbstractDatabaseOutputDelegate {

  private Expression query;

  private Expression outputPath;

  private Expression resultType;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String key = this.designation.getValue(execution).toString();
    String query = this.query.getValue(execution).toString();

    Connection conn = connectionService.getConnection(key);

    try (Statement statement = conn.createStatement()) {
      statement.execute(query);

      ResultSet results = null;
      if (statement.getUpdateCount() == -1) {
        results = statement.getResultSet();

        ResultOp resultOp;

        if (Objects.nonNull(this.outputPath) && StringUtils.isNotEmpty(this.outputPath.getValue(execution).toString())) {
          String outputPath = this.outputPath.getValue(execution).toString();
          String resultType = this.resultType.getValue(execution).toString();
          resultOp = new FileResultOp(results, outputPath, resultType);
        } else {
          resultOp = new VariableResultOp(execution, results);
        }

        while (results.next()) {
          resultOp.next();
        }

        resultOp.finish();
      }
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

  @Override
  public Class<?> fromTask() {
    return DatabaseQueryTask.class;
  }

  private interface ResultOp {
    void next() throws Exception;

    void finish() throws Exception;
  }

  private class FileResultOp implements ResultOp {

    private final ResultSet results;

    private final FileWriter fw;

    private final DatabaseResultTypeOp rowOp;

    public FileResultOp(ResultSet results, String path, String resultType) throws SQLException, IOException {
      this.results = results;
      this.fw = new FileWriter(path);
      this.rowOp = DatabaseResultTypeOp.valueOf(resultType);
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

  public enum DatabaseResultTypeOp implements RowOp {
    CSV() {
      public void process(FileWriter fw, ResultSet results) throws SQLException, IOException {
        StringBuilder builder = new StringBuilder("");
        ResultSetMetaData metadata = results.getMetaData();
        for (int count = 1; count <= metadata.getColumnCount(); ++count) {
          String columnName = metadata.getColumnName(count);
          builder.append(results.getString(columnName));
          if (count < metadata.getColumnCount()) {
            builder.append(",");
          }
        }
        builder.append("\n");
        fw.write(builder.toString());
      }
    },
    TSV() {
      public void process(FileWriter fw, ResultSet results) throws SQLException, IOException {
        StringBuilder builder = new StringBuilder("");
        ResultSetMetaData metadata = results.getMetaData();
        for (int count = 1; count <= metadata.getColumnCount(); ++count) {
          String columnName = metadata.getColumnName(count);
          builder.append(results.getString(columnName));
          if (count < metadata.getColumnCount()) {
            builder.append("\t");
          }
        }
        builder.append("\n");
        fw.write(builder.toString());
      }
    },
    JSON() {
      public void process(FileWriter fw, ResultSet results) throws SQLException, IOException {
        StringBuilder builder = new StringBuilder("{");
        ResultSetMetaData metadata = results.getMetaData();
        for (int count = 1; count <= metadata.getColumnCount(); ++count) {
          String columnName = metadata.getColumnName(count);
          builder.append("\"").append(columnName).append("\":\"").append(results.getString(columnName)).append("\"");
        }
        builder.append("}").append("\n");
        fw.write(builder.toString());
      }
    }
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

}
