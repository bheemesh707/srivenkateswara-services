package com.srivenkateswara.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminCredentialsService {
  private final Path credentialsFile;
  private final ObjectMapper mapper = new ObjectMapper();
  private AdminCredentials credentials;

  public AdminCredentialsService(
      @Value("${admin.credentials-file}") String credentialsFile,
      @Value("${admin.username}") String defaultUsername,
      @Value("${admin.password}") String defaultPassword) {
    this.credentialsFile = Path.of(credentialsFile);
    this.credentials = new AdminCredentials();
    this.credentials.setUsername(defaultUsername);
    this.credentials.setPassword(defaultPassword);

    load();
  }

  public synchronized AdminCredentials getCredentials() {
    return credentials;
  }

  public synchronized void updateCredentials(String username, String password) {
    credentials.setUsername(username);
    credentials.setPassword(password);
    save();
  }

  private void load() {
    try {
      if (!Files.exists(credentialsFile)) {
        save();
        return;
      }
      credentials = mapper.readValue(Files.readAllBytes(credentialsFile), AdminCredentials.class);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load admin credentials", e);
    }
  }

  private void save() {
    try {
      Files.createDirectories(credentialsFile.getParent());
      mapper.writerWithDefaultPrettyPrinter().writeValue(credentialsFile.toFile(), credentials);
    } catch (IOException e) {
      throw new RuntimeException("Failed to save admin credentials", e);
    }
  }
}
