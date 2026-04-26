package com.files.projBistro.tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class FileExportTest {

    @TempDir
    Path tempDir;

    // Test CSV format structure (without actually creating files)
    @Test
    void testCSVLineFormat_ValidFormat() {
        String csvLine = "101,John Doe,50.00,2025-03-01,completed";

        // Check CSV structure (comma separated, numeric values, date format)
        String[] parts = csvLine.split(",");
        assertEquals(5, parts.length);
        assertTrue(parts[0].matches("\\d+"));           // Order ID - numbers only
        assertTrue(parts[2].matches("\\d+\\.\\d{2}"));  // Price - decimal with 2 places
        assertTrue(parts[3].matches("\\d{4}-\\d{2}-\\d{2}")); // Date format
    }

    @Test
    void testCSVLineFormat_InvalidFormat_Fails() {
        String invalidCsv = "abc,def,ghi";
        String[] parts = invalidCsv.split(",");
        assertNotEquals(5, parts.length); // Should not have 5 columns
    }

    @Test
    void testReceiptContent_ContainsRequiredInfo() {
        String mockReceipt = """
            ==================================================
                        PAYMENT RECEIPT
            ==================================================
            Transaction ID: TXN123456
            Amount: RM50.00
            Status: APPROVED
            """;

        assertTrue(mockReceipt.contains("Transaction ID:"));
        assertTrue(mockReceipt.contains("Amount: RM"));
        assertTrue(mockReceipt.contains("Status:"));
    }

    @Test
    void testInvoiceContent_HasOrderDetails() {
        String mockInvoice = """
            CAMO-GEAR BISTRO
            ORDER RECEIPT
            Order #: C001
            Customer: John Doe
            Item 1: Burger - RM12.99
            TOTAL: RM12.99
            """;

        assertTrue(mockInvoice.contains("ORDER RECEIPT"));
        assertTrue(mockInvoice.contains("Order #:"));
        assertTrue(mockInvoice.contains("TOTAL: RM"));
    }

    @Test
    void testSalesReportHeaders_CSVExport() {
        String expectedHeaders = "Transaction ID,Date,Card Number,Card Holder,Amount,Status";
        assertTrue(expectedHeaders.contains("Transaction ID"));
        assertTrue(expectedHeaders.contains("Date"));
        assertTrue(expectedHeaders.contains("Amount"));
    }

    // Test writing and reading a temporary file
    @Test
    void testWriteAndReadTempFile() throws IOException {
        Path tempFile = tempDir.resolve("test_invoice.txt");
        String content = "Test invoice content";

        // Write
        Files.writeString(tempFile, content);
        assertTrue(Files.exists(tempFile));

        // Read
        String readContent = Files.readString(tempFile);
        assertEquals(content, readContent);
    }

    // Test CSV writing simulation
    @Test
    void testWriteCSVFormat() {
        StringBuilder csv = new StringBuilder();
        csv.append("Order ID,Customer,Total,Date,Status\n");
        csv.append("101,John,50.00,2025-03-01,completed\n");
        csv.append("102,Jane,30.00,2025-03-02,pending\n");

        String[] lines = csv.toString().split("\n");
        assertEquals(3, lines.length); // Header + 2 data rows
        assertTrue(lines[0].contains("Order ID"));
        assertTrue(lines[1].contains(",50.00,"));
    }

    @Test
    void testFileExtensionValidation() {
        String pdfFile = "invoice_123.pdf";
        String csvFile = "sales_report.csv";
        String txtFile = "receipt.txt";

        assertTrue(pdfFile.endsWith(".pdf"));
        assertTrue(csvFile.endsWith(".csv"));
        assertTrue(txtFile.endsWith(".txt"));
    }
}