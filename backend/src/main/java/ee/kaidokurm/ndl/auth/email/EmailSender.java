package ee.kaidokurm.ndl.auth.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Email sender service supporting both real SMTP and console logging (testing)
 */
@Service
public class EmailSender {

  private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

  private final JavaMailSender mailSender;
  private final EmailConfig emailConfig;

  public EmailSender(JavaMailSender mailSender, EmailConfig emailConfig) {
    this.mailSender = mailSender;
    this.emailConfig = emailConfig;
  }

  /**
   * Send email verification code to user
   * 
   * @param toEmail recipient email
   * @param code    6-digit verification code
   */
  public void sendVerificationCode(String toEmail, String code) {
    String subject = "Email Verification Code";
    // Build verification link
    String verificationUrl = emailConfig.getFrontendUrl() + "/verify-email?email=" + encodeUrl(toEmail) + "&code="
        + code;
    String body = buildVerificationEmailBody(code, verificationUrl);

    sendEmail(toEmail, subject, body);
  }

  /**
   * Send password reset link to user
   * 
   * @param toEmail  recipient email
   * @param resetUrl full URL with token (e.g., https://app.com/reset?token=xyz)
   */
  public void sendPasswordReset(String toEmail, String resetUrl) {
    String subject = "Reset Your Password";
    String body = buildPasswordResetEmailBody(resetUrl);

    sendEmail(toEmail, subject, body);
  }

  /**
   * Core email sending logic - switches between real SMTP and console logging
   */
  private void sendEmail(String toEmail, String subject, String body) {
    if (!emailConfig.isEnabled()) {
      // Testing mode: log to console
      logToConsole(toEmail, subject, body);
      return;
    }

    // Production mode: send via SMTP
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      try {
        helper.setFrom(emailConfig.getFromAddress(), emailConfig.getFromName());
      } catch (java.io.UnsupportedEncodingException e) {
        logger.error("Invalid encoding for from address: {}", e.getMessage());
        throw new RuntimeException("Invalid email configuration", e);
      }
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(body, true); // true = HTML content

      mailSender.send(message);

      logger.info("Email sent to {}", toEmail);
    } catch (MessagingException e) {
      logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
      throw new RuntimeException("Failed to send email", e);
    }
  }

  /**
   * Log email to console (for testing when SMTP is disabled)
   */
  private void logToConsole(String toEmail, String subject, String body) {
    logger.info("========== EMAIL (TESTING MODE) ==========");
    logger.info("To: {}", toEmail);
    logger.info("Subject: {}", subject);
    logger.info("Body:\n{}", body);
    logger.info("==========================================");
  }

  /**
   * URL encode a string (e.g., email addresses with special chars)
   */
  private String encodeUrl(String value) {
    try {
      return java.net.URLEncoder.encode(value, "UTF-8");
    } catch (java.io.UnsupportedEncodingException e) {
      return value; // Fallback if encoding fails
    }
  }

  /**
   * Build HTML email body for verification code
   */
  private String buildVerificationEmailBody(String code, String verificationUrl) {
    return """
        <html>
        <body style="font-family: Arial, sans-serif; color: #333; max-width: 600px;">
          <h2>Email Verification</h2>
          <p>Click the button below to verify your email:</p>
          <div style="margin: 25px 0;">
            <a href="%s" style="background-color: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; display: inline-block; font-weight: bold;">
              Verify Email
            </a>
          </div>
          <p style="color: #666; font-size: 14px;">Or enter this code:</p>
          <div style="background-color: #f0f0f0; padding: 15px; font-size: 24px; font-weight: bold; letter-spacing: 3px; margin: 20px 0;">
            %s
          </div>
          <p style="color: #666; font-size: 12px;">
            This code expires in 24 hours.
          </p>
          <p style="color: #999; font-size: 11px; border-top: 1px solid #ddd; padding-top: 15px;">
            If you didn't request this code, please ignore this email.
          </p>
        </body>
        </html>
        """
        .formatted(verificationUrl, code);
  }

  /**
   * Build HTML email body for password reset
   */
  private String buildPasswordResetEmailBody(String resetUrl) {
    return """
        <html>
        <body style="font-family: Arial, sans-serif; color: #333;">
          <h2>Reset Your Password</h2>
          <p>Click the button below to reset your password:</p>
          <div style="margin: 20px 0;">
            <a href="%s" style="background-color: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 4px; display: inline-block;">
              Reset Password
            </a>
          </div>
          <p>This link expires in 1 hour.</p>
          <p style="color: #666; font-size: 12px;">
            Or copy this link: <br/><code>%s</code>
          </p>
          <p style="color: #666; font-size: 12px;">
            If you didn't request a password reset, please ignore this email.
          </p>
        </body>
        </html>
        """
        .formatted(resetUrl, resetUrl);
  }
}
