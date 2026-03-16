package com.srivenkateswara.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final AdminCredentialsService credentialsService;
  private final RequestLogService requestLogService;

  public AdminController(
      AdminCredentialsService credentialsService, RequestLogService requestLogService) {
    this.credentialsService = credentialsService;
    this.requestLogService = requestLogService;
  }

  @GetMapping("/requests")
  public ResponseEntity<byte[]> downloadRequests(
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    if (!isAuthorized(authorization)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"admin\"")
          .build();
    }

    try {
      Path file = requestLogService.getLogFile();
      if (!Files.exists(file)) {
        return ResponseEntity.notFound().build();
      }

      byte[] payload = Files.readAllBytes(file);
      return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contact-requests.xlsx")
          .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
          .body(payload);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @GetMapping("/requests/list")
  public ResponseEntity<Object> listRequests(
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    if (!isAuthorized(authorization)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"admin\"")
          .build();
    }

    return ResponseEntity.ok(requestLogService.readAll());
  }

  @PostMapping("/credentials")
  public ResponseEntity<Void> updateCredentials(
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @RequestBody AdminCredentials update) {
    if (!isAuthorized(authorization)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"admin\"")
          .build();
    }

    if (update.getUsername() == null || update.getUsername().isBlank()) {
      return ResponseEntity.badRequest().build();
    }

    if (update.getPassword() == null || update.getPassword().isBlank()) {
      return ResponseEntity.badRequest().build();
    }

    credentialsService.updateCredentials(update.getUsername(), update.getPassword());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/requests/list")
  public ResponseEntity<Object> listRequests(
      @RequestHeader(value = "Authorization", required = false) String authorization) {
    if (!isAuthorized(authorization)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"admin\"")
          .build();
    }

    return ResponseEntity.ok(requestLogService.readAll());
  }

  private boolean isAuthorized(String authorization) {
    if (authorization == null || !authorization.startsWith("Basic ")) {
      return false;
    }

    String base64 = authorization.substring("Basic ".length());
    String decoded;
    try {
      decoded = new String(Base64.getDecoder().decode(base64), StandardCharsets.UTF_8);
    } catch (IllegalArgumentException ignored) {
      return false;
    }

    String[] parts = decoded.split(":", 2);
    if (parts.length != 2) {
      return false;
    }

    String user = parts[0];
    String pass = parts[1];

    AdminCredentials creds = credentialsService.getCredentials();
    return creds.getUsername().equals(user) && creds.getPassword().equals(pass);
  }
}
