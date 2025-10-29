package org.folio.rest.camunda.delegate;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.enums.FileOp;
import org.folio.rest.workflow.model.FileTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
@Scope("prototype")
public class FileDelegate extends AbstractWorkflowIODelegate {

  private static final String LINE_KEY = "line";
  private static final String PATH_KEY = "path";
  private static final String TARGET_KEY = "target";

  private Expression path;

  private Expression line;

  private Expression op;

  private Expression target;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    final FileOp fileOp = FileOp.valueOf(this.op.getValue(execution).toString());
    final long startTime = determineStartTime(execution, fileOp);

    String pathTemplate = this.path.getValue(execution).toString();
    String lineTemplate = this.line != null ? this.line.getValue(execution).toString() : "0";

    StringTemplateLoader templateLoader = new StringTemplateLoader();
    templateLoader.putTemplate(PATH_KEY, pathTemplate);
    templateLoader.putTemplate(LINE_KEY, lineTemplate);

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    cfg.setTemplateLoader(templateLoader);

    Map<String, Object> inputs = getInputs(execution);

    String filePath = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate(PATH_KEY), inputs);
    Integer lineValue = Integer.parseInt(FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate(LINE_KEY), inputs));

    File file = new File(filePath);

    switch (fileOp) {
      case COPY:
        if (file.exists()) {
          String targetTemplate = this.target.getValue(execution).toString();
          templateLoader.putTemplate(TARGET_KEY, targetTemplate);
          String targetPath = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate(TARGET_KEY), inputs);

          File targetFile = new File(targetPath);

          FileUtils.copyFile(file, targetFile);

        } else {
          getLogger().info("{} does not exist", filePath);
        }
        break;
      case MOVE:
        if (file.exists()) {
          String targetTemplate = this.target.getValue(execution).toString();
          templateLoader.putTemplate(TARGET_KEY, targetTemplate);
          String targetPath = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate(TARGET_KEY), inputs);

          File targetFile = new File(targetPath);

          FileUtils.moveFile(file, targetFile);

        } else {
          getLogger().info("{} does not exist", filePath);
        }
        break;
      case DELETE:
        if (file.exists()) {
          boolean deleted = file.delete();
          if (deleted) {
            getLogger().info("{} has been deleted", filePath);
          }
        } else {
          getLogger().info("{} does not exist", filePath);
        }
        break;
      case LINE_COUNT:
        if (file.exists()) {
          try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            long lineCount = reader.lines().count();
            setOutput(execution, lineCount);
            getLogger().info("{} read", filePath);
          }
        } else {
          getLogger().info("{} does not exist", filePath);
        }
        break;
      case READ_LINE:
        if (file.exists() && lineValue > 0) {
          try (BufferedReader reader = Files.newBufferedReader(Path.of(filePath), StandardCharsets.UTF_8)) {
            int lineCount = 0;
            String currerntLine = "";
            while ((currerntLine = reader.readLine()) != null && (++lineCount) < lineValue) {}
            reader.close();
            setOutput(execution, currerntLine);
            getLogger().info("{} read", filePath);
          }
        } else {
          getLogger().info("{} does not exist", filePath);
        }
        break;
      case READ:
        if (file.exists()) {
          String content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
          setOutput(execution, content);
          getLogger().info("{} read", filePath);
        } else {
          getLogger().info("{} does not exist", filePath);
        }
        break;
      case WRITE:
        // iterate over `target` input varaible
        // writing entry per line
        String targetInputVariable = this.target.getValue(execution).toString();
        StringBuilder content = new StringBuilder();
        Object obj = inputs.get(targetInputVariable);

        if (obj == null) {
          getLogger().warn("The target parameter '{}' of the WRITE operation is missing from the {} '{}'.", targetInputVariable, getDelegateClass(), getDelegateName(execution));
        } else if (obj instanceof List) {
          List<?> objects = (List<?>) obj;
          getLogger().info("{} {} has {} entries to write",
            obj.getClass().getSimpleName(), targetInputVariable, objects.size());

          for (Object value : objects) {
              if (value instanceof String) {
                content.append(value);
              } else {
                content.append(objectMapper.writeValueAsString(value));
              }
              content.append("\n");
            }
        } else if (obj instanceof String) {
          getLogger().info("{} {} has a single string to write.", obj.getClass().getSimpleName(), targetInputVariable);

          content.append(obj);
          content.append("\n");
        } else {
          getLogger().warn("The target parameter '{}' of the WRITE operation is unsupported for the {} '{}'.", targetInputVariable, getDelegateClass(), getDelegateName(execution));
        }
        FileUtils.writeStringToFile(file, content.toString(), StandardCharsets.UTF_8);
        getLogger().info("{} written", filePath);
        break;
      case LIST:
        if (file.exists()) {
          if (file.isDirectory()) {
            List<String> listing = new ArrayList<>();
            traverseDirectory(file, listing);
            setOutput(execution, listing);
          } else {
            getLogger().info("{} is not a directory to list", filePath);
          }
        } else {
          getLogger().info("{} does not exist", filePath);
        }
        break;
      default:
        break;
    }

    determineEndTime(execution, startTime);
  }

  public void setPath(Expression path) {
    this.path = path;
  }

  public void setLine(Expression line) {
      this.line = line;
  }

  public void setOp(Expression op) {
    this.op = op;
  }

  public void setTarget(Expression target) {
    this.target = target;
  }

  @Override
  public Class<?> fromTask() {
    return FileTask.class;
  }

  private void traverseDirectory(File directory, List<String> listing) {
    if (directory.isDirectory()) {
      File[] files = directory.listFiles();
      Arrays.sort(files, Comparator.comparingLong(File::lastModified));
      for (File file : files) {
        if (file.isFile()) {
          listing.add(file.getAbsolutePath());
        } else if (file.isDirectory()) {
          traverseDirectory(file, listing);
        }
      }
    }
  }

}
