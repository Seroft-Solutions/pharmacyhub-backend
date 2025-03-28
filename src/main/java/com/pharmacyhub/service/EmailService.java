package com.pharmacyhub.service;

import com.pharmacyhub.entity.Otp;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import java.nio.charset.StandardCharsets;
import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;

@Service
public class EmailService
{
  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
  
  @Autowired
  private JavaMailSender mailSender;
  
  @Autowired
  private ResourceLoader resourceLoader;
  
  @Autowired
  private Environment environment;

  @Value("${spring.mail.username}")
  private String emailAddress;
  
  @Value("${pharmacyhub.frontend.url}")
  private String frontendUrl;
  
  @Value("${spring.mail.sender.name:PharmacyHub}")
  private String senderName;

  /**
   * Log email configuration details at startup
   */
  @PostConstruct
  public void logEmailConfiguration() {
    logger.info("EmailService initialized with configuration:");
    logger.info("Mail Server: {} (Port: {})", getEnvironmentInfo("Mail Host"), getEnvironmentInfo("Mail Port"));
    logger.info("Mail Username: {}", getEnvironmentInfo("Mail Username"));
    logger.info("Mail Sender Name: {}", senderName);
    logger.info("Frontend URL: {}", frontendUrl);
    logger.info("SMTP Auth: {}", getEnvironmentInfo("Mail Auth"));
    logger.info("SMTP StartTLS: {}", getEnvironmentInfo("Mail StartTLS"));
    
    // Don't log actual password but check if it's present
    String mailPassword = environment.getProperty("spring.mail.password");
    logger.info("Mail Password configured: {}", (mailPassword != null && !mailPassword.isEmpty()) ? "YES" : "NO");
  }

  public void sendHtmlMail(Otp otp) throws MessagingException
  {
    logger.info("Preparing OTP email for user: {}", otp.getUser().getEmailAddress());
    String subject = "Your OTP for Pharmacy Hub";
    String body = prepareHtmlContent("${otp}", otp.getCode(), "OtpEmail.html");
    emailSender(otp.getUser().getEmailAddress(), subject, body);
    logger.info("OTP email sent successfully to: {}", otp.getUser().getEmailAddress());
  }

  /**
   * Sends a verification email with a verification token
   * 
   * @param emailAddress User's email address
   * @param token Verification token
   * @throws MessagingException If there's an error sending the email
   */
  public void sendVerificationEmail(String emailAddress, String token) throws MessagingException
  {
    logger.info("Preparing basic verification email for: {}", emailAddress);
    String verificationUrl = frontendUrl + "/verify-email?token=" + token;
    logger.debug("Verification URL: {}", verificationUrl);

    String subject = "Welcome to Pharmacy Hub - Verify Your Email";
    String body = prepareHtmlContent("${verificationUrl}", verificationUrl, "EmailVerification.html");
    emailSender(emailAddress, subject, body);
    logger.info("Basic verification email sent successfully to: {}", emailAddress);
  }
  
  /**
   * Sends a verification email with a verification token and device information
   * 
   * @param emailAddress User's email address
   * @param token Verification token
   * @param ipAddress IP address from which the request was made
   * @param userAgent User agent information
   * @throws MessagingException If there's an error sending the email
   */
  public void sendVerificationEmail(String emailAddress, String token, String ipAddress, String userAgent) throws MessagingException
  {
    logger.info("Preparing verification email for: {}", emailAddress);
    logger.debug("Verification details - IP: {}, UserAgent: {}, Token: {}", 
               ipAddress != null ? ipAddress : "Unknown", 
               userAgent != null ? userAgent : "Unknown", 
               token.substring(0, Math.min(token.length(), 8)) + "...");
    
    String verificationUrl = frontendUrl + "/verify-email?token=" + token;
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    String subject = "Welcome to Pharmacy Hub - Verify Your Email";
    String body = prepareHtmlContent("${verificationUrl}", verificationUrl, "EmailVerification.html");
    
    // Add device information if the template supports it
    body = body.replace("${ipAddress}", ipAddress != null ? ipAddress : "Unknown");
    body = body.replace("${userAgent}", userAgent != null ? userAgent : "Unknown");
    body = body.replace("${timestamp}", timestamp);
    
    logger.debug("Verification email prepared successfully with verification URL: {}", verificationUrl);
    emailSender(emailAddress, subject, body);
    logger.info("Verification email sent successfully to: {}", emailAddress);
  }
  
  /**
   * Asynchronously sends a verification email with a verification token and device information
   * 
   * @param emailAddress User's email address
   * @param token Verification token
   * @param ipAddress IP address from which the request was made
   * @param userAgent User agent information
   * @return CompletableFuture with result of operation
   */
  @Async
  public CompletableFuture<Boolean> sendVerificationEmailAsync(String emailAddress, String token, String ipAddress, String userAgent) {
      logger.info("Asynchronous verification email requested for: {}", emailAddress);
      return CompletableFuture.supplyAsync(() -> {
          try {
              logger.debug("Starting async email process for: {} from thread: {}", 
                         emailAddress, Thread.currentThread().getName());
              // Send verification email with device tracking information
              sendVerificationEmail(emailAddress, token, ipAddress, userAgent);
              logger.info("Verification email sent asynchronously to: {}", emailAddress);
              return true;
          } catch (Exception e) {
              logger.error("Failed to send verification email asynchronously to: {} - Error: {}", 
                          emailAddress, e.getMessage(), e);
              return false;
          }
      });
  }

  /**
   * Sends a password reset email with a reset token
   * 
   * @param emailAddress User's email address
   * @param token Reset token
   * @param ipAddress IP address from which the request was made
   * @param userAgent User agent information
   * @throws MessagingException If there's an error sending the email
   */
  public void sendPasswordResetEmail(String emailAddress, String token, String ipAddress, String userAgent) throws MessagingException
  {
    logger.info("Sending password reset email to: {}", emailAddress);
    
    String resetUrl = frontendUrl + "/reset-password/" + token;
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    String subject = "Password Reset Request - Pharmacy Hub";
    String body = prepareHtmlContent("${resetUrl}", resetUrl, "PasswordResetEmail.html");
    
    // Replace device information placeholders
    body = body.replace("${ipAddress}", ipAddress != null ? ipAddress : "Unknown");
    body = body.replace("${userAgent}", userAgent != null ? userAgent : "Unknown");
    body = body.replace("${timestamp}", timestamp);
    
    // Add logging for debugging
    logger.debug("Password reset email details:\nTo: {}\nReset URL: {}\nIP: {}\nAgent: {}", 
               emailAddress, resetUrl, ipAddress, userAgent);
    
    emailSender(emailAddress, subject, body);
    
    logger.info("Password reset email sent successfully to: {}", emailAddress);
  }

  /**
   * Send a test email to verify email configuration
   * 
   * @param testEmailAddress Address to send test email to
   * @return Success message or error message
   */
  public String sendTestEmail(String testEmailAddress) {
    try {
      logger.info("Sending test email to: {}", testEmailAddress);
      
      String subject = "PharmacyHub Test Email - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      StringBuilder htmlBody = new StringBuilder();
      htmlBody.append("<html><body>");
      htmlBody.append("<h1>PharmacyHub Test Email</h1>");
      htmlBody.append("<p>This is a test email to verify the email configuration.</p>");
      htmlBody.append("<p>Server time: ").append(LocalDateTime.now()).append("</p>");
      htmlBody.append("<p>Configuration:</p>");
      htmlBody.append("<ul>");
      htmlBody.append("<li>SMTP Server: ").append(getEnvironmentInfo("Mail Host")).append("</li>");
      htmlBody.append("<li>SMTP Port: ").append(getEnvironmentInfo("Mail Port")).append("</li>");
      htmlBody.append("<li>Auth: ").append(getEnvironmentInfo("Mail Auth")).append("</li>");
      htmlBody.append("<li>StartTLS: ").append(getEnvironmentInfo("Mail StartTLS")).append("</li>");
      htmlBody.append("</ul>");
      htmlBody.append("</body></html>");
      
      emailSender(testEmailAddress, subject, htmlBody.toString());
      logger.info("Test email sent successfully to: {}", testEmailAddress);
      return "Test email sent successfully to: " + testEmailAddress;
    } catch (Exception e) {
      logger.error("Failed to send test email to: {}, Error: {}", testEmailAddress, e.getMessage(), e);
      return "Failed to send test email: " + e.getMessage();
    }
  }

  private void emailSender(String toEmailAddress, String subject, String body) throws MessagingException
  {
    logger.info("Preparing to send email to: {}, subject: {}", toEmailAddress, subject);
    logger.debug("Email configuration - Host: {}, Port: {}, Username: {}", 
               getEnvironmentInfo("Mail Host"), getEnvironmentInfo("Mail Port"), getEnvironmentInfo("Mail Username"));
    
    try {
      logger.debug("Creating MIME message with encoding: {}", StandardCharsets.UTF_8.name());
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
      
      // Set proper From address with display name
      helper.setFrom(new InternetAddress(emailAddress, senderName));
      helper.setTo(toEmailAddress);
      helper.setSubject(subject);
      
      // Always add both plain text and HTML versions
      String plainText = extractPlainTextFromHtml(body);
      helper.setText(plainText, body);
      
      // Set Reply-To header (same as from address)
      helper.setReplyTo(emailAddress);
      
      // Set important headers to improve deliverability
      message.addHeader("X-Priority", "1");
      message.addHeader("X-MSMail-Priority", "High");
      message.addHeader("Importance", "High");
      message.addHeader("X-Auto-Response-Suppress", "OOF, AutoReply");
      message.addHeader("X-Mailer", "PharmacyHub Mailer");
      
      logger.debug("Email prepared successfully with headers: Priority=High, From={}, To={}", emailAddress, toEmailAddress);
      logger.info("Attempting to connect to mail server and send email to: {}", toEmailAddress);
      
      long startTime = System.currentTimeMillis();
      mailSender.send(message);
      long endTime = System.currentTimeMillis();
      
      logger.info("Email sent successfully to: {} in {} ms", toEmailAddress, (endTime - startTime));
      logger.debug("SMTP transaction completed successfully for recipient: {}", toEmailAddress);
    } catch (jakarta.mail.AuthenticationFailedException e) {
      logger.error("SMTP Authentication failed. Check username and password. Error: {}", e.getMessage(), e);
      throw new MessagingException("Failed to authenticate with mail server: " + e.getMessage(), e);
    } catch (jakarta.mail.SendFailedException e) {
      logger.error("Failed to send email to: {}. Invalid recipient or sending rejected: {}", toEmailAddress, e.getMessage(), e);
      throw new MessagingException("Mail server rejected the recipient or sender: " + e.getMessage(), e);
    } catch (jakarta.mail.MessagingException e) {
      logger.error("Messaging error when sending to: {}. SMTP Error: {}", toEmailAddress, e.getMessage(), e);
      throw new MessagingException("Mail server communication error: " + e.getMessage(), e);
    } catch (Exception e) {
      logger.error("Failed to send email to: {}, Unexpected error: {}", toEmailAddress, e.getMessage(), e);
      throw new MessagingException("Failed to send email: " + e.getMessage(), e);
    }
  }

  private String extractPlainTextFromHtml(String html) {
    // Simple HTML to plain text conversion
    String plainText = html
        .replaceAll("<br\\s*/?>|<p>|</p>|<div>|</div>", "\n")
        .replaceAll("<.*?>", "")
        .replaceAll("&nbsp;", " ")
        .replaceAll("&lt;", "<")
        .replaceAll("&gt;", ">")
        .replaceAll("&amp;", "&")
        .replaceAll("&quot;", "\"")
        .replaceAll("&apos;", "'");
    return plainText.trim();
  }

  public String prepareHtmlContent(String key, String value, String template)
  {
    logger.debug("Preparing HTML content using template: {}", template);
    String htmlTemplate = loadHtmlTemplate(template);
    String result = htmlTemplate.replace(key, value);
    logger.debug("HTML content prepared successfully using template: {}", template);
    return result;
  }

  public String loadHtmlTemplate(String htmlTemplate)
  {
    logger.debug("Loading HTML template: {}", htmlTemplate);
    Resource resource = resourceLoader.getResource("classpath:templates/"+htmlTemplate);
    StringBuilder contentBuilder = new StringBuilder();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)))
    {
      String line;
      int lineCount = 0;
      while ((line = reader.readLine()) != null)
      {
        contentBuilder.append(line).append(System.lineSeparator());
        lineCount++;
      }
      logger.debug("Successfully loaded template: {} ({} lines)", htmlTemplate, lineCount);
    }
    catch (IOException e)
    {
      logger.error("Failed to load HTML template: {} - Error: {}", htmlTemplate, e.getMessage(), e);
    }

    return contentBuilder.toString();
  }
  
  /**
   * Get a mail environment property with a safe default
   */
  private String getEnvironmentInfo(String propertyType) {
    switch (propertyType) {
      case "Mail Host":
        return environment.getProperty("spring.mail.host", "<not set>");
      case "Mail Port":
        return environment.getProperty("spring.mail.port", "<not set>");
      case "Mail Username":
        return environment.getProperty("spring.mail.username", "<not set>");
      case "Mail Auth":
        return environment.getProperty("spring.mail.properties.mail.smtp.auth", "<not set>");
      case "Mail StartTLS":
        return environment.getProperty("spring.mail.properties.mail.smtp.starttls.enable", "<not set>");
      default:
        return "<unknown property>";
    }
  }
}