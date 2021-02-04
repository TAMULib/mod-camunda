package org.folio.rest.delegate;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.mail.internet.MimeMessage;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.folio.rest.workflow.model.EmailTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;

@Service
@Scope("prototype")
public class EmailDelegate extends AbstractWorkflowInputDelegate {

  @Autowired
  private JavaMailSender emailSender;

  private Expression mailTo;

  private Expression mailCc;

  private Expression mailBcc;

  private Expression mailFrom;

  private Expression mailSubject;

  private Expression mailText;

  private Expression attachmentPath;

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    long startTime = System.nanoTime();
    FlowElement bpmnModelElement = execution.getBpmnModelElementInstance();
    String delegateName = bpmnModelElement.getName();

    logger.info("{} started", delegateName);

    String subjectTemplate = this.mailSubject.getValue(execution).toString();
    String textTemplate = this.mailText.getValue(execution).toString();

    Map<String, Object> inputs = getInputs(execution);

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

    StringTemplateLoader stringLoader = new StringTemplateLoader();
    stringLoader.putTemplate("subject", subjectTemplate);
    stringLoader.putTemplate("text", textTemplate);
    cfg.setTemplateLoader(stringLoader);

    String subject = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("subject"), inputs);
    String text = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("text"), inputs);

    String to = this.mailTo.getValue(execution).toString();
    String from = this.mailFrom.getValue(execution).toString();

    Optional<String> cc = Objects.nonNull(this.mailCc) ? Optional.of(this.mailCc.getValue(execution).toString()) : Optional.empty();
    Optional<String> bcc = Objects.nonNull(this.mailBcc) ? Optional.of(this.mailBcc.getValue(execution).toString()) : Optional.empty();
    Optional<String> attachmentPath = Objects.nonNull(this.attachmentPath) ? Optional.of(this.attachmentPath.getValue(execution).toString()) : Optional.empty();

    MimeMessagePreparator preparator = new MimeMessagePreparator() {
        public void prepare(MimeMessage mimeMessage) throws Exception 
        {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            message.setFrom(from);
            for (String ct : to.split(",")) {
              message.addTo(ct);
            }
            message.setSubject(subject);
            message.setText(text, true);

            if (cc.isPresent()) {
              for (String ccc : cc.get().split(",")) {
                message.addCc(ccc);
              }
            }

            if (bcc.isPresent()) {
              for (String cbcc : bcc.get().split(",")) {
                message.addCc(cbcc);
              }
            }

            if (attachmentPath.isPresent()) {
              File attachment = new File(attachmentPath.get());
              if (attachment.exists() && attachment.isFile()) {
                message.addAttachment(attachment.getName(), attachment);
              } else {
                logger.info("{} does not exist", attachmentPath.get());
              }
            }
        }
    };

    emailSender.send(preparator);

    long endTime = System.nanoTime();
    logger.info("{} finished in {} milliseconds", delegateName, (endTime - startTime) / (double) 1000000);
  }

  public void setMailTo(Expression mailTo) {
    this.mailTo = mailTo;
  }

  public void setMailCc(Expression mailCc) {
    this.mailCc = mailCc;
  }

  public void setMailBcc(Expression mailBcc) {
    this.mailBcc = mailBcc;
  }

  public void setMailFrom(Expression mailFrom) {
    this.mailFrom = mailFrom;
  }

  public void setMailSubject(Expression mailSubject) {
    this.mailSubject = mailSubject;
  }

  public void setMailText(Expression mailText) {
    this.mailText = mailText;
  }

  public void setAttachmentPath(Expression attachmentPath) {
    this.attachmentPath = attachmentPath;
  }

  @Override
  public Class<?> fromTask() {
    return EmailTask.class;
  }

}
