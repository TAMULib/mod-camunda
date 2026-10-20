package org.folio.rest.camunda.delegate;

import static org.folio.rest.camunda.utility.FileUtility.createFile;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import jakarta.mail.MessagingException;
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

    final StringTemplateLoader stringLoader = new StringTemplateLoader();

    populateTemplate(execution, stringLoader);

    final Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);
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
    final String includeAttachmentString;
    final Boolean includeAttachmentValue;

    try {
      subject = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("subject"), inputs);
      plainText = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("text"), inputs);
      htmlMarkup = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("markup"), inputs);
      to = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("mailTo"), inputs);
      from = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("mailFrom"), inputs);
      includeAttachmentString = FreeMarkerTemplateUtils.processTemplateIntoString(cfg.getTemplate("includeAttachment"), inputs);
      includeAttachmentValue = Boolean.parseBoolean(includeAttachmentString);

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

    final PreparedMessage preparator = new PreparedMessage(from, to, cc, bcc, subject, plainText, htmlMarkup, includeAttachmentValue, attachmentPathValue);

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

  /**
   * Populate the template loader while performing execution.
   *
   * @param execution    The execution data.
   * @param stringLoader The string loader to populate.
   */
  private void populateTemplate(final DelegateExecution execution, final StringTemplateLoader stringLoader) {

    final String subjectTemplate = this.mailSubject.getValue(execution).toString();
    final String textTemplate = this.mailText.getValue(execution).toString();
    final String markupTemplate = Objects.nonNull(this.mailMarkup) ? this.mailMarkup.getValue(execution).toString() : "";
    final String mailToTemplate = this.mailTo.getValue(execution).toString();
    final String mailFromTemplate = this.mailFrom.getValue(execution).toString();
    final String attachmentPathTemplate = Objects.nonNull(this.attachmentPath) ? this.attachmentPath.getValue(execution).toString() : "";
    final String includeAttachmentTemplate = Objects.nonNull(this.includeAttachment) ? this.includeAttachment.getValue(execution).toString() : "";

    stringLoader.putTemplate("subject", subjectTemplate);
    stringLoader.putTemplate("text", textTemplate);
    stringLoader.putTemplate("markup", markupTemplate);
    stringLoader.putTemplate("mailFrom", mailFromTemplate);
    stringLoader.putTemplate("mailTo", mailToTemplate);
    stringLoader.putTemplate("attachmentPath", attachmentPathTemplate);
    stringLoader.putTemplate("includeAttachment", includeAttachmentTemplate);
  }

  class PreparedMessage implements MimeMessagePreparator {

    private final String from;

    private final String to;

    private final String cc;

    private final String bcc;

    private final String subject;

    private final String plainText;

    private final String htmlMarkup;

    private final Boolean includeAttachment;

    private final String attachmentPath;

    private MimeMessageHelper helper;

    public PreparedMessage(
      String from, String to, String cc, String bcc, String subject, String plainText,
      String htmlMarkup, Boolean includeAttachment, String attachmentPath
    ) {

      this.from = from;
      this.to = to;
      this.cc = cc;
      this.bcc = bcc;
      this.subject = subject;
      this.plainText = plainText;
      this.htmlMarkup = htmlMarkup;
      this.includeAttachment = includeAttachment;
      this.attachmentPath = attachmentPath;
      this.helper = null;
    }

    /**
     * Prepare the message.
     *
     * @param mimeMessage The message.
     */
    public void prepare(MimeMessage mimeMessage) throws MessagingException {

      helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      setFrom();
      setTo();
      setCc();
      setBcc();
      setSubject();
      setPayload();
      setAttachment();
    }

    /**
     * Add any attachments.
     *
     * @throws MessagingException On error.
     */
    private void setAttachment() throws MessagingException {

      if (Boolean.TRUE.equals(includeAttachment)) {
        if (attachmentPath == null) {
          getLogger().error("Attachment is to be included but the attachment path is missing.");
        } else {
          final File attachment = createFile(attachmentPath);

          if (attachment.exists() && attachment.isFile()) {
            helper.addAttachment(attachment.getName(), attachment);
          } else {
            getLogger().warn("Attachment {} does not exist.", attachmentPath);
          }
        }
      } else {
        getLogger().debug("No attachment required.");
      }
    }

    /**
     * Set the "bcc" address.
     *
     * @throws MessagingException On error.
     */
    private void setBcc() throws MessagingException {

      if (bcc != null) {
        for (final String cbcc : bcc.split(",")) {
          try {
            helper.addCc(cbcc);
          } catch (AddressException e) {
            throw new EmailDelegateAddressFailure("bcc", cbcc, e.getMessage(), e);
          }
        }
      }
    }

    /**
     * Set the "cc" address.
     *
     * @throws MessagingException On error.
     */
    private void setCc() throws MessagingException {

      if (cc != null) {
        for (final String ccc : cc.split(",")) {
          try {
            helper.addCc(ccc);
          } catch (AddressException e) {
            throw new EmailDelegateAddressFailure("cc", ccc, e.getMessage(), e);
          }
        }
      }
    }

    /**
     * Set the "from" address.
     *
     * @throws MessagingException On error.
     */
    private void setFrom() throws MessagingException {

      try {
        helper.setFrom(from);
      } catch (AddressException e) {
        throw new EmailDelegateAddressFailure("from", from, e.getMessage(), e);
      }
    }

    /**
     * Set the "payload" (e-mail body text).
     *
     * @throws MessagingException On error.
     */
    private void setPayload() throws MessagingException {

      if (htmlMarkup == null) {
        helper.setText(plainText, false);
      } else if (plainText.isEmpty()) {
        helper.setText(htmlMarkup, true);
      } else {
        helper.setText(plainText, htmlMarkup);
      }
    }

    /**
     * Set the "subject".
     *
     * @throws MessagingException On error.
     */
    private void setSubject() throws MessagingException {

      helper.setSubject(subject);
    }

    /**
     * Set the "to" address.
     *
     * @throws MessagingException On error.
     */
    private void setTo() throws MessagingException {

      try {
        helper.setTo(to);
      } catch (AddressException e) {
        throw new EmailDelegateAddressFailure("to", to, e.getMessage(), e);
      }
    }
  }

}
