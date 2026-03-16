package com.srivenkateswara.services;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
  private final EmailService emailService;

  public ContactController(EmailService emailService) {
    this.emailService = emailService;
  }

  @PostMapping
  public ResponseEntity<Void> sendContact(@Valid @RequestBody ContactRequest request) {
    emailService.sendContactEmail(request);
    return ResponseEntity.ok().build();
  }
}
