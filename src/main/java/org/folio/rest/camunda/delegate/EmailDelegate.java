package org.folio.rest.camunda.delegate;

import static org.folio.rest.camunda.utility.FileUtility.createFile;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.util.Map;
import java.util.Objects;
import org.folio.rest.camunda.exception.DelegateExecutionFailure;
import org.folio.rest.camunda.exception.EmailDelegateAddressFailure;
import org.folio.rest.workflow.model.EmailTask;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.Expression;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import tools.jackson.databind.json.JsonMapper;

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

  /**
   * Initializer.
   */
  public EmailDelegate(JsonMapper mapper, RuntimeService runtimeService, JavaMailSender emailSender) {

    super(mapper, runtimeService);

    this.emailSender = emailSender;
  }

  /**
   * Perform the execution.
   *
   * @param execution The execution data.
   * @param name      The delegate name.
   * @param id        The delegate ID.
   */
  @Override
  protected void performExecute(DelegateExecution execution, String name, String id) {

    String subjectTemplate = this.mailSubject.getValue(execution).toString();
    String textTemplate = this.mailText.getValue(execution).toString();
    String markupTemplate = Objects.nonNull(this.mailMarkup) ? this.mailMarkup.getValue(execution).toString() : "";
    String mailToTemplate = this.mailTo.getValue(execution).toString();
    String mailFromTemplate = this.mailFrom.getValue(execution).toString();
    String attachmentPathTemplate = Objects.nonNull(this.attachmentPath) ? this.attachmentPath.getValue(execution).toString() : "";

    StringTemplateLoader stringLoader = new StringTemplateLoader();
    stringLoader.putTemplate("subject", subjectTemplate);
    stringLoader.putTemplate("text", textTemplate);
    stringLoader.putTemplate("markup", markupTemplate);
    stringLoader.putTemplate("mailFrom", mailFromTemplate);
    stringLoader.putTemplate("mailTo", mailToTemplate);
    stringLoader.putTemplate("attachmentPath", attachmentPathTemplate);

    Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
    cfg.setTemplateLoader(stringLoader);

    final Map<String, Object> inputs = getInputs(execution);
    final String subject;
    final String plainText;
    final String htmlMarkup;
    final String to;
    final String from;
    final String cc;
    final String bcc;
    final String attachmentPathValue;
    final Object includeAttachmentObject = this.includeAttachment.getValue(execution);
    final Boolean includeAttachmentValue = includeAttachmentObject instanceof Boolean includeAttachmentBoolean
      ? includeAttachmentBoolean
      : Boolean.parseBoolean((String) includeAttachmentObject);

    try {
      subject = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("subject"), inputs);
      plainText = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("text"), inputs);
      htmlMarkup = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("markup"), inputs);
      to = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("mailTo"), inputs);
      from = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("mailFrom"), inputs);

      cc = mailCc == null
        ? null
        : mailCc.getValue(execution).toString();

      bcc = mailBcc == null
        ? null
        : mailBcc.getValue(execution).toString();

      attachmentPathValue = attachmentPath == null
        ? null
        : FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("attachmentPath"), inputs);
    } catch (Exception e) {
      throw new DelegateExecutionFailure(name, id, e.getMessage(), e);
    }

    getLogger().debug("E-mail To: {}, E-mail From: {}, E-mail Subject: {}, Has Attachment: {}", to, from, subject, attachmentPathValue == null ? "No" : "Yes");

    MimeMessagePreparator preparator = new MimeMessagePreparator() {
      public void prepare(MimeMessage mimeMessage) throws Exception {
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        try {
          message.setFrom(from);
        } catch (AddressException e) {
          throw new EmailDelegateAddressFailure("from", from, e.getMessage(), e);
        }

        for (String ct : to.split(",")) {
          try {
            message.addTo(ct);
          } catch (AddressException e) {
            throw new EmailDelegateAddressFailure("to", ct, e.getMessage(), e);
          }
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

        if (cc != null) {
          for (String ccc : cc.split(",")) {
            try {
              message.addCc(ccc);
            } catch (AddressException e) {
              throw new EmailDelegateAddressFailure("cc", ccc, e.getMessage(), e);
            }
          }
        }

        if (bcc != null) {
          for (String cbcc : bcc.split(",")) {
            try {
              message.addCc(cbcc);
            } catch (AddressException e) {
              throw new EmailDelegateAddressFailure("bcc", cbcc, e.getMessage(), e);
            }
          }
        }

        if (Boolean.TRUE.equals(includeAttachmentValue)) {
          if (attachmentPathValue == null) {
            getLogger().error("Attachment is to be included but the attachment path is missing.");
          } else {
            final File attachment = createFile(attachmentPathValue);
  
            if (attachment.exists() && attachment.isFile()) {
              message.addAttachment(attachment.getName(), attachment);
            } else {
              getLogger().warn("Attachment {} does not exist.", attachmentPathValue);
            }
          }
        } else {
          getLogger().debug("No attachment required.");
        }
      }
    };

    emailSender.send(preparator);
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
