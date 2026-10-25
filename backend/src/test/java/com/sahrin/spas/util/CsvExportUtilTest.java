package com.sahrin.spas.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("CsvExportUtil Unit Tests")
class CsvExportUtilTest {

    @Test
    @DisplayName("Should escape spreadsheet formula injection characters with single quote")
    void shouldEscapeFormulaInjectionCharacters() {
        assertEquals("'=1+1", CsvExportUtil.escapeCell("=1+1"));
        assertEquals("'+SUM(A1:A10)", CsvExportUtil.escapeCell("+SUM(A1:A10)"));
        assertEquals("'-cmd|' /C calc'!A0", CsvExportUtil.escapeCell("-cmd|' /C calc'!A0"));
        assertEquals("'@SUM(B1:B5)", CsvExportUtil.escapeCell("@SUM(B1:B5)"));
        assertEquals("'\tTabStart", CsvExportUtil.escapeCell("\tTabStart"));
        assertEquals("\"'\rCarriageReturn\"", CsvExportUtil.escapeCell("\rCarriageReturn"));
    }

    @Test
    @DisplayName("Should keep regular text and numbers unescaped when safe")
    void shouldKeepSafeValuesUnchanged() {
        assertEquals("Computer Science", CsvExportUtil.escapeCell("Computer Science"));
        assertEquals("85.5", CsvExportUtil.escapeCell(85.5));
        assertEquals("42", CsvExportUtil.escapeCell(42));
        assertEquals("", CsvExportUtil.escapeCell(""));
        assertEquals("", CsvExportUtil.escapeCell(null));
    }

    @Test
    @DisplayName("Should wrap values with commas and double internal quotes")
    void shouldWrapAndEscapeCommasAndQuotes() {
        assertEquals("\"Mathematics, Advanced\"", CsvExportUtil.escapeCell("Mathematics, Advanced"));
        assertEquals("\"He said \"\"Pass\"\"\"", CsvExportUtil.escapeCell("He said \"Pass\""));
        assertEquals("\"Line 1\nLine 2\"", CsvExportUtil.escapeCell("Line 1\nLine 2"));
    }

    @Test
    @DisplayName("Should build multi-row CSV with headers and CRLF line breaks")
    void shouldBuildMultiRowCsv() {
        List<String> headers = List.of("ID", "Name", "Score", "Comments");
        List<List<Object>> rows = List.of(
                List.of("STU-001", "Alice Smith", 88.5, "Good, solid work"),
                List.of("STU-002", "Bob = Dangerous", 45.0, "Needs help")
        );

        String csv = CsvExportUtil.buildCsv(headers, rows);

        assertTrue(csv.contains("ID,Name,Score,Comments\r\n"));
        assertTrue(csv.contains("STU-001,Alice Smith,88.5,\"Good, solid work\"\r\n"));
        assertTrue(csv.contains("STU-002,Bob = Dangerous,45.0,Needs help\r\n"));
    }

    @Test
    @DisplayName("Should prepend UTF-8 BOM to CSV bytes for Excel compatibility")
    void shouldPrependUtf8Bom() {
        String content = "Col1,Col2\r\nVal1,Val2\r\n";
        byte[] bytes = CsvExportUtil.toCsvBytesWithBom(content);

        // UTF-8 BOM is 0xEF, 0xBB, 0xBF
        assertEquals((byte) 0xEF, bytes[0]);
        assertEquals((byte) 0xBB, bytes[1]);
        assertEquals((byte) 0xBF, bytes[2]);

        byte[] payload = new byte[bytes.length - 3];
        System.arraycopy(bytes, 3, payload, 0, payload.length);
        assertEquals(content, new String(payload, StandardCharsets.UTF_8));
    }
}
