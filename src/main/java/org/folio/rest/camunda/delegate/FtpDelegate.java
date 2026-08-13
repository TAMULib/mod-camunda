package org.folio.rest.camunda.delegate;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.VFS;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.enums.SftpOp;
import org.folio.rest.workflow.model.FtpTask;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class FtpDelegate extends AbstractWorkflowIODelegate {

  private Expression originPath;

  private Expression destinationPath;

  private Expression op;

  private Expression scheme;

  private Expression host;

  private Expression port;

  private Expression username;

  private Expression password;

  /**
   * Initializer.
   */
  public FtpDelegate(ObjectMapper objectMapper, RuntimeService runtimeService) {

    super(objectMapper, runtimeService);
  }

  /**
   * Perform the execution.
   *
   * @param execution The execution data.
   * @param name      The delegate name.
   * @param id        The delegate ID.
   */
  @Override
  public void execute(DelegateExecution execution) throws Exception {
    final long startTime = determineStartTime(execution);

    String originPathValue = this.originPath.getValue(execution).toString();

    String destinationPathValue = this.destinationPath.getValue(execution).toString();

    SftpOp opValue = SftpOp.valueOf(this.op.getValue(execution).toString());

    String schemeValue = this.scheme.getValue(execution).toString();

    String hostValue = this.host.getValue(execution).toString();

    int portValue = Integer.parseInt(this.port.getValue(execution).toString());

    Optional<String> usernameValue = Objects.nonNull(this.username)
      ? Optional.ofNullable(this.username.getValue(execution).toString())
      : Optional.empty();

    Optional<String> passwordValue = Objects.nonNull(this.password)
      ? Optional.ofNullable(this.password.getValue(execution).toString())
      : Optional.empty();

    FileSystemManager manager = VFS.getManager();

    String userInfo = null;

    if (usernameValue.isPresent()) {
      userInfo = usernameValue.get();
    }

    if (passwordValue.isPresent()) {
      userInfo += ":" + passwordValue.get();
    }

    switch (opValue) {
      case GET: {

        File file = new File(destinationPathValue);

        URI uri = new URI(
          schemeValue,
          userInfo,
          hostValue,
          portValue,
          originPathValue,
          null,
          null
        );

        try (
          FileObject local = manager.resolveFile(file.getAbsolutePath());
          FileObject remote = manager.resolveFile(uri);
        ) {
          local.copyFrom(remote, Selectors.SELECT_SELF);
        }

      } break;
      case PUT: {

        File file = new File(originPathValue);

        URI uri = new URI(
          schemeValue,
          userInfo,
          hostValue,
          portValue,
          destinationPathValue,
          null,
          null
        );

        try (
          FileObject local = manager.resolveFile(file.getAbsolutePath());
          FileObject remote = manager.resolveFile(uri);
        ) {
          remote.copyFrom(local, Selectors.SELECT_SELF);
        }

      } break;
      default:
        break;
    }

    determineEndTime(execution, startTime);
  }

  public void setOriginPath(Expression originPath) {
    this.originPath = originPath;
  }

  public void setDestinationPath(Expression destinationPath) {
    this.destinationPath = destinationPath;
  }

  public void setOp(Expression op) {
    this.op = op;
  }

  public void setScheme(Expression scheme) {
    this.scheme = scheme;
  }

  public void setHost(Expression host) {
    this.host = host;
  }

  public void setPort(Expression port) {
    this.port = port;
  }

  public void setUsername(Expression username) {
    this.username = username;
  }

  public void setPassword(Expression password) {
    this.password = password;
  }

  @Override
  public Class<?> fromTask() {
    return FtpTask.class;
  }

}
