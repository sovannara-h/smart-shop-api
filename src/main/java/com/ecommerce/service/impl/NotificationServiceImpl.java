package com.ecommerce.service.impl;

import com.ecommerce.service.interfaces.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class NotificationServiceImpl implements NotificationService {

  private final JavaMailSender emailSender;

  // private final Slack slack;

  @Value("${notification.admin.email}")
  private String adminEmail;

  // @Value("${notification.slack.webhook}")
  // private String slackWebhook;

  @Autowired(required = false)
  public NotificationServiceImpl(JavaMailSender emailSender) {
    this.emailSender = emailSender;
    log.info("NotificationService created with emailSender: {}", emailSender != null);
    // this.slack = Slack.getInstance();
  }

  public NotificationServiceImpl() {
    this.emailSender = null;
    log.info("NotificationService created without emailSender");
  }

  @Transactional
  public void sendEmail(String message) {
    try {
      SimpleMailMessage email = new SimpleMailMessage();
      email.setTo(adminEmail);
      email.setSubject("Security Alert - Secret Management");
      email.setText(message);
      emailSender.send(email);
    } catch (Exception e) {
      log.error("Error sending email", e);
    }
  }

  public void notifyAdmins(String message) {
    // En développement, nous nous contentons de logger le message
    log.info("ADMIN NOTIFICATION: {}", message);

    // Si l'emailSender est disponible, envoyer un email
    if (emailSender != null) {
      // Code d'envoi d'email...
    }

    // In production, you would implement email or Slack message sending here
    // emailService.sendToAdmins(message);
    // slackService.sendToSecurityChannel(message);
  }

  // private void sendSlackNotification(String message) {
  //   try {
  //     slack
  //         .methods(slackWebhook)
  //         .chatPostMessage(req -> req.channel("#security-alerts").text(message));
  //   } catch (Exception e) {
  //     log.error("Error sending Slack notification", e);
  //   }
  // }
}
