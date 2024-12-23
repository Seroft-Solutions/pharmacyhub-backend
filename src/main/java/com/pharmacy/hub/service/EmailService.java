package com.pharmacy.hub.service;

import com.pharmacy.hub.entity.Otp;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
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

@Service
public class EmailService
{
  @Autowired
  private JavaMailSender mailSender;
  @Autowired
  private ResourceLoader resourceLoader;

  @Value("${spring.mail.username}")
  private String emailAddress;

  public void sendHtmlMail(Otp otp) throws MessagingException
  {
    String subject = "Your OTP for Pharmacy Hub";
    String body = prepareHtmlContent("${otp}",otp.getCode(),"OtpEmail.html");
    emailSender(otp.getUser().getEmailAddress(), subject, body);
  }


  public void sendVerificationEmail(String emailAddress, String token) throws MessagingException
  {
    String verificationUrl = "https://localhost:8080/auth/verify?token=" + token;

    String subject = "Welcome to Pharmacy Hub";
    String body = prepareHtmlContent("${verificationUrl}",verificationUrl,"EmailVerification.html");
    emailSender(emailAddress, subject, body);
  }


  private void emailSender(String emailAddress, String subject, String body) throws MessagingException
  {
    MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
      helper.setTo(emailAddress);
      helper.setSubject(subject);
      helper.setText(body, true);

    mailSender.send(message);
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