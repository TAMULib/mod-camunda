package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.enums.CompressFileContainer;
import org.folio.rest.workflow.enums.CompressFileFormat;
import org.folio.rest.workflow.model.CompressFileTask;
import org.h2.util.IOUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
@Scope("prototype")
public class CompressFileDelegate extends AbstractWorkflowIODelegate {

  private static final String ENCODING = "UTF8";
  private static final int BLOCK_SIZE = 8192;

  private static final String EXT_BZIP2 = ".bz2";
  private static final String EXT_GZIP = ".gz";
  private static final String EXT_ZIP = ".zip";
  private static final String EXT_TAR = ".tar";

  private Expression source;

  private Expression destination;

  private Expression format;

  private Expression container;

  /**
   * Initializer.
   */
  public CompressFileDelegate(ObjectMapper objectMapper, RuntimeService runtimeService) {

    super(objectMapper, runtimeService);
  }

  /**
   * Perform the execution.
   *
   * @param execution The execution data.
   */
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    final long startTime = determineStartTime(execution);

    String sourcePathTemplate = this.source.getValue(execution).toString();
    String destinationPathTemplate = this.destination.getValue(execution).toString();

    StringTemplateLoader pathLoader = new StringTemplateLoader();
    pathLoader.putTemplate("sourcePath", sourcePathTemplate);
    pathLoader.putTemplate("destinationPath", destinationPathTemplate);

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    cfg.setTemplateLoader(pathLoader);

    Map<String, Object> inputs = getInputs(execution);
    String sourcePath = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("sourcePath"), inputs);
    String destinationPath = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("destinationPath"), inputs);

    CompressFileFormat compressFormat = CompressFileFormat.valueOf(this.format.getValue(execution).toString());
    CompressFileContainer useContainer = CompressFileContainer.valueOf(this.container.getValue(execution).toString());
    String formatType = null;
    String extension = "";
    File sourceFile = new File(sourcePath);
    File destinationFile = new File(destinationPath);

    if (destinationFile.isDirectory()) {
      if (!destinationPath.endsWith(File.separator)) {
        destinationPath += File.separator;
      }

      destinationFile = new File(destinationPath + sourceFile.getName() + extension);
    }

    // see: https://commons.apache.org/proper/commons-compress/limitations.html
    switch (compressFormat) {
      case BZIP2:
        formatType = CompressorStreamFactory.BZIP2;
        extension = EXT_BZIP2;
        break;

      case GZIP:
        formatType = CompressorStreamFactory.GZIP;
        extension = EXT_GZIP;
        break;

      case ZIP:
        extension = EXT_ZIP;
        break;

      default:
        break;
    }

    if (useContainer == CompressFileContainer.TAR) {
      extension = EXT_TAR + extension;
    }

    if (soureFileHasProblems(sourceFile, sourcePath,  useContainer)) {
      formatType = null;
    }

    getLogger().info("Destination: {}", destinationFile.getPath());
    getLogger().info("Source: {}", sourceFile.getPath());
    getLogger().info("Compress format: {}", compressFormat);

    if (compressFormat == CompressFileFormat.ZIP) {
      try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(destinationFile))) {
        zipOut.putNextEntry(new ZipEntry(sourceFile.getName()));
        Files.copy(sourceFile.toPath(), zipOut);
      }
    } else {
      getLogger().info("Format type: {}", formatType);
      getLogger().info("Use container: {}", useContainer);
      if (formatType != null) {
        if (useContainer == CompressFileContainer.NONE) {
          try (
            FileInputStream inputFile = new FileInputStream(sourceFile);
            BufferedInputStream input = new BufferedInputStream(inputFile);
            FileOutputStream outputFile = new FileOutputStream(destinationFile);
            BufferedOutputStream output = new BufferedOutputStream(outputFile);
            CompressorOutputStream compress = new CompressorStreamFactory()
              .createCompressorOutputStream(formatType, output);
          ) {
              IOUtils.copy(input, compress);
          }
        } else if (useContainer == CompressFileContainer.TAR) {
          FileOutputStream outputFile = new FileOutputStream(destinationFile);
          BufferedOutputStream output = new BufferedOutputStream(outputFile);

          CompressorOutputStream compress = new CompressorStreamFactory()
            .createCompressorOutputStream(formatType, output);

          TarArchiveOutputStream tar = new TarArchiveOutputStream(compress, BLOCK_SIZE, ENCODING);

          tar.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
          tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);

          try {
            addPaths(sourcePath, "", tar);

            tar.finish();
          } finally {
            tar.close();
            compress.close();
            outputFile.close();
          }
        }

        getLogger().info("{} written to {} as {}", sourcePath, destinationPath, compressFormat);
      }
    }

    determineEndTime(execution, startTime);
  }

  public void setSource(Expression source) {
    this.source = source;
  }

  public void setDestination(Expression destination) {
    this.destination = destination;
  }

  public void setFormat(Expression format) {
    this.format = format;
  }

  public void setContainer(Expression container) {
    this.container = container;
  }

  @Override
  public Class<?> fromTask() {
    return CompressFileTask.class;
  }

  private void addPaths(String path, String parentPath, TarArchiveOutputStream tar) throws IOException {
    File file = new File(path);

    setupEntryHeader(path, parentPath, tar, file);

    if (file.isDirectory()) {
      tar.closeArchiveEntry();

      for (File f : file.listFiles()) {
        addPaths(f.getAbsolutePath(), parentPath + file.getName() + File.separator, tar);
      }
    } else {
      try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
        IOUtils.copy(input, tar);
      }

      tar.closeArchiveEntry();
    }
  }

  private void setupEntryHeader(String path, String parentPath, TarArchiveOutputStream tar, File file) throws IOException {
    Path filePath = Path.of(path);
    TarArchiveEntry entry = new TarArchiveEntry(file, parentPath + file.getName());

    if (file.isFile()) {
      entry.setSize(Files.size(Paths.get(path)));
    }

    entry.setModTime(Files.getLastModifiedTime(filePath).toMillis());

    tar.putArchiveEntry(entry);
  }

  /**
   * Helper function for execute() to help solve "S3776" coding practice.
   *
   * @param sourceFile The source file.
   * @param sourcePath The source path.
   * @param useContainer The container.
   *
   * @return True if the source file has problems and false otherwise.
   */
  private boolean soureFileHasProblems(File sourceFile, String sourcePath, CompressFileContainer useContainer) {
    boolean hasProblems = false;

    if (sourceFile.exists()) {
      if (!sourceFile.canRead()) {
        getLogger().info("{} could not be read", sourcePath);
        hasProblems = true;
      }

      if (useContainer == CompressFileContainer.NONE && sourceFile.isDirectory()) {
        getLogger().info("{} is a directory and cannot be compressed when container is NONE", sourcePath);
        hasProblems = true;
      }
    } else {
      getLogger().info("{} does not exist", sourcePath);
      hasProblems = true;
    }

    return hasProblems;
  }

}
