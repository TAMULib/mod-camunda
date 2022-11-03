package org.folio.rest.aspect;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DelegateExecutionExceptionAspect {

  private static final Logger logger = LoggerFactory.getLogger(DelegateExecutionExceptionAspect.class);

  @Autowired
  private JavaMailSender emailSender;

  @Value("${error.handling.environment:DEV}")
  private String errorHandlingEnvironment;

  @Value("${error.handling.emailFrom}")
  private String errorHandlingEmailFrom;

  @Value("${error.handling.emailTo}")
  private String errorHandlingEmailTo;

  @AfterThrowing(pointcut = "execution(* org.camunda.bpm.engine.delegate.JavaDelegate.execute (org.camunda.bpm.engine.delegate.DelegateExecution)) && args(execution))", throwing = "exception")
  public void afterDelegateExecutionThrowsException(DelegateExecution execution, Throwable exception) {

    RepositoryService repositoryService = execution.getProcessEngineServices().getRepositoryService();

    String workflowName = repositoryService.createProcessDefinitionQuery()
        .processDefinitionId(execution.getProcessDefinitionId())
        .singleResult()
        .getName();

    String taskName = execution.getCurrentActivityName();

    String tenant = execution.getTenantId();

    StringBuilder stackTraceBuilder = new StringBuilder();
    for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
      stackTraceBuilder.append(stackTraceElement.toString());
      stackTraceBuilder.append("\n");
    }

    StringBuilder subject = new StringBuilder();

    subject
        .append(errorHandlingEnvironment)
        .append(": ")
        .append(workflowName)
        .append(" failed on ")
        .append(taskName)
        .append(" for tenant ")
        .append(tenant);

    StringBuilder text = new StringBuilder();

    text.append(exception.getClass().getSimpleName())
        .append("\n\n")
        .append(exception.getMessage())
        .append("\n\n")
        .append(stackTraceBuilder.toString())
        .append("\n");

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(errorHandlingEmailFrom);
    message.setTo(errorHandlingEmailTo.split(","));
    message.setSubject(subject.toString());
    message.setText(text.toString());

    try {
      emailSender.send(message);
    } catch (MailException e) {
      logger.error("Failed to send email notification of incident", e);
    }

  }

}
