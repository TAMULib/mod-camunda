package org.folio.rest.camunda.delegate;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.folio.rest.workflow.model.EmailTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
@Scope("prototype")
public class EmailDelegate extends AbstractWorkflowInputDelegate {

  private JavaMailSender emailSender;

  private Expression mailTo;

  private Expression mailCc;

  private Expression mailBcc;

  private Expression mailFrom;

  private Expression mailSubject;

  private Expression mailText;

  private Expression mailMarkup;

  private Expression attachmentPath;

  private Expression includeAttachment;

  @Autowired
  public EmailDelegate(JavaMailSender emailSender) {
    this.emailSender = emailSender;
  }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    final long startTime = determineStartTime(execution);

    String subjectTemplate = this.mailSubject.getValue(execution).toString();
    String textTemplate = this.mailText.getValue(execution).toString();
    String markupTemplate = Objects.nonNull(this.mailMarkup) ? this.mailMarkup.getValue(execution).toString() : "";
    String mailToTemplate = this.mailTo.getValue(execution).toString();
    String mailFromTemplate = this.mailFrom.getValue(execution).toString();
    String attachmentPathTemplate = Objects.nonNull(this.attachmentPath) ? this.attachmentPath.getValue(execution).toString() : "";
    String includeAttachmentTemplate = Objects.nonNull(this.includeAttachment) ? this.includeAttachment.getValue(execution).toString() : "";

    StringTemplateLoader stringLoader = new StringTemplateLoader();
    stringLoader.putTemplate("subject", subjectTemplate);
    stringLoader.putTemplate("text", textTemplate);
    stringLoader.putTemplate("markup", markupTemplate);
    stringLoader.putTemplate("mailFrom", mailFromTemplate);
    stringLoader.putTemplate("mailTo", mailToTemplate);
    stringLoader.putTemplate("attachmentPath", attachmentPathTemplate);
    stringLoader.putTemplate("includeAttachment", includeAttachmentTemplate);

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    cfg.setTemplateLoader(stringLoader);

    Map<String, Object> inputs = getInputs(execution);
    String subject = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("subject"), inputs);
    String plainText = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("text"), inputs);
    String htmlMarkup = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("markup"), inputs);
    String to = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("mailTo"), inputs);
    String from = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("mailFrom"), inputs);

    Optional<String> cc = Objects.nonNull(this.mailCc) ? Optional.ofNullable(this.mailCc.getValue(execution).toString()) : Optional.empty();
    Optional<String> bcc = Objects.nonNull(this.mailBcc) ? Optional.ofNullable(this.mailBcc.getValue(execution).toString()) : Optional.empty();
    Optional<String> attachmentPathValue = Objects.nonNull(this.attachmentPath) ? Optional.ofNullable(FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("attachmentPath"), inputs)) : Optional.empty();

    MimeMessagePreparator preparator = new MimeMessagePreparator() {
      public void prepare(MimeMessage mimeMessage) throws Exception {
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        message.setFrom(from);
        for (String ct : to.split(",")) {
          message.addTo(ct);
        }
        message.setSubject(subject);

        if (Objects.nonNull(mailMarkup)) {
          if (plainText.isEmpty()) {
            message.setText(htmlMarkup, true);
          } else {
            message.setText(plainText, htmlMarkup);
          }
        } else {
          message.setText(plainText, false);
        }

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

        // This is a hot fix to address an issue with the workflow not attaching e-mails.
        if (attachmentPathValue.isPresent()) {
          File attachment = new File(attachmentPathValue.get());
          if (attachment.exists() && attachment.isFile()) {
            message.addAttachment(attachment.getName(), attachment);
          } else {
            getLogger().info("{} does not exist", attachmentPathValue.get());
          }
        } else {
          getLogger().info("No attachment required");
        }
      }
    };

    emailSender.send(preparator);

    determineEndTime(execution, startTime);
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

  public void setMailMarkup(Expression mailMarkup) {
    this.mailMarkup = mailMarkup;
  }

  public void setAttachmentPath(Expression attachmentPath) {
    this.attachmentPath = attachmentPath;
  }

  public void setIncludeAttachment(Expression includeAttachment) {
    this.includeAttachment = includeAttachment;
  }

  @Override
  public Class<?> fromTask() {
    return EmailTask.class;
  }

}
