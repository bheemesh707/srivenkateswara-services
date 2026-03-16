package com.srivenkateswara.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RequestLogService {
  private static final DateTimeFormatter TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC);

  private final Path logFile;
  private final ReentrantLock lock = new ReentrantLock();

  public RequestLogService(@Value("${admin.log-file}") String logFilePath) {
    this.logFile = Path.of(logFilePath);
  }

  public Path getLogFile() {
    return logFile;
  }

  public void log(ContactRequest request) {
    lock.lock();
    try {
      Files.createDirectories(logFile.getParent());

      try (Workbook wb = loadOrCreateWorkbook()) {
        Sheet sheet = wb.getSheetAt(0);
        int nextRow = sheet.getLastRowNum() + 1;

        Row row = sheet.createRow(nextRow);
        addCell(row, 0, TIMESTAMP_FORMATTER.format(Instant.now()));
        addCell(row, 1, request.getFullName());
        addCell(row, 2, request.getEmail());
        addCell(row, 3, request.getLoanType());
        addCell(row, 4, request.getAmount());

        try (var out = Files.newOutputStream(logFile)) {
          wb.write(out);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to log request", e);
    } finally {
      lock.unlock();
    }
  }

  public List<RequestLogEntry> readAll() {
    lock.lock();
    try {
      if (!Files.exists(logFile)) {
        return List.of();
      }

      try (Workbook wb = new XSSFWorkbook(Files.newInputStream(logFile))) {
        Sheet sheet = wb.getSheetAt(0);
        List<RequestLogEntry> entries = new ArrayList<>();
        int lastRow = sheet.getLastRowNum();

        for (int rowIndex = 1; rowIndex <= lastRow; rowIndex++) {
          Row row = sheet.getRow(rowIndex);
          if (row == null) {
            continue;
          }

          RequestLogEntry entry = new RequestLogEntry();
          entry.setTimestamp(getCellString(row.getCell(0)));
          entry.setFullName(getCellString(row.getCell(1)));
          entry.setEmail(getCellString(row.getCell(2)));
          entry.setLoanType(getCellString(row.getCell(3)));
          entry.setAmount(getCellString(row.getCell(4)));

          entries.add(entry);
        }

        return entries;
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to read request log", e);
    } finally {
      lock.unlock();
    }
  }

  private String getCellString(Cell cell) {
    return cell == null ? "" : cell.toString();
  }

  private Workbook loadOrCreateWorkbook() throws IOException {
    if (Files.exists(logFile)) {
      return new XSSFWorkbook(Files.newInputStream(logFile));
    }

    Workbook wb = new XSSFWorkbook();
    Sheet sheet = wb.createSheet("Requests");
    Row header = sheet.createRow(0);
    addCell(header, 0, "Timestamp (UTC)");
    addCell(header, 1, "Full name");
    addCell(header, 2, "Email");
    addCell(header, 3, "Loan type");
    addCell(header, 4, "Amount");
    return wb;
  }

  private void addCell(Row row, int col, String value) {
    Cell cell = row.createCell(col);
    cell.setCellValue(value != null ? value : "");
  }
}
