package com.pharmacyhub.service;

import com.pharmacyhub.entity.Otp;
import jakarta.mail.MessagingException;
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

@Service
public class EmailService
{
  private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
  
  @Autowired
  private JavaMailSender mailSender;
  @Autowired
  private ResourceLoader resourceLoader;

  @Value("${spring.mail.username}")
  private String emailAddress;
  
  @Value("${pharmacyhub.frontend.url}")
  private String frontendUrl;

  public void sendHtmlMail(Otp otp) throws MessagingException
  {
    String subject = "Your OTP for Pharmacy Hub";
    String body = prepareHtmlContent("${otp}",otp.getCode(),"OtpEmail.html");
    emailSender(otp.getUser().getEmailAddress(), subject, body);
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
    String verificationUrl = frontendUrl + "/verify-email?token=" + token;

    String subject = "Welcome to Pharmacy Hub - Verify Your Email";
    String body = prepareHtmlContent("${verificationUrl}",verificationUrl,"EmailVerification.html");
    emailSender(emailAddress, subject, body);
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
    String verificationUrl = frontendUrl + "/verify-email?token=" + token;
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    String subject = "Welcome to Pharmacy Hub - Verify Your Email";
    String body = prepareHtmlContent("${verificationUrl}", verificationUrl, "EmailVerification.html");
    
    // Add device information if the template supports it
    body = body.replace("${ipAddress}", ipAddress != null ? ipAddress : "Unknown");
    body = body.replace("${userAgent}", userAgent != null ? userAgent : "Unknown");
    body = body.replace("${timestamp}", timestamp);
    
    emailSender(emailAddress, subject, body);
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
      return CompletableFuture.supplyAsync(() -> {
          try {
              // Send verification email with device tracking information
              sendVerificationEmail(emailAddress, token, ipAddress, userAgent);
              logger.info("Verification email sent asynchronously to: {}", emailAddress);
              return true;
          } catch (Exception e) {
              logger.error("Failed to send verification email asynchronously to: {}", emailAddress, e);
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


  private void emailSender(String emailAddress, String subject, String body) throws MessagingException
  {
    logger.debug("Preparing to send email to: {}, subject: {}", emailAddress, subject);
    
    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
      helper.setTo(emailAddress);
      helper.setSubject(subject);
      helper.setText(body, true);

      mailSender.send(message);
      logger.debug("Email sent successfully to: {}", emailAddress);
    } catch (Exception e) {
      logger.error("Failed to send email to: {}, error: {}", emailAddress, e.getMessage(), e);
      throw e; // Re-throw the exception to be handled by the caller
    }
  }

  public String prepareHtmlContent(String key, String value, String template)
  {
    String htmlTemplate = loadHtmlTemplate(template);
    return htmlTemplate.replace(key, value);
  }

  public String loadHtmlTemplate(String htmlTemplate)
  {
    Resource resource = resourceLoader.getResource("classpath:templates/"+htmlTemplate);
    StringBuilder contentBuilder = new StringBuilder();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8")))
    {
      String line;
      while ((line = reader.readLine()) != null)
      {
        contentBuilder.append(line).append(System.lineSeparator());
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    return contentBuilder.toString();
  }



}