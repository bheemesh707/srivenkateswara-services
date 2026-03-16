package com.srivenkateswara.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
  private final JavaMailSender mailSender;
  private final String recipient;
  private final RequestLogService requestLogService;

  public EmailService(
      JavaMailSender mailSender,
      @Value("${contact.email.to}") String recipient,
      RequestLogService requestLogService) {
    this.mailSender = mailSender;
    this.recipient = recipient;
    this.requestLogService = requestLogService;
  }

  public void sendContactEmail(ContactRequest request) {
    // Log the request to the shared Excel sheet (for admin access)
    requestLogService.log(request);

    String subject = "Loan request from " + request.getFullName();
    String body = String.format(
      "Name: %s\nEmail: %s\nLoan type: %s\nAmount: %s",
      request.getFullName(),
      request.getEmail(),
      request.getLoanType(),
      request.getAmount()
    );

    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(recipient);
    message.setFrom("bheem100g@gmail.com");
    message.setSubject(subject);
    message.setText(body);

    mailSender.send(message);
  }
}
