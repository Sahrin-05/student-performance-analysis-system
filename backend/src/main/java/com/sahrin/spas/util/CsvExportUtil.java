package com.sahrin.spas.util;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Utility for generating RFC 4180 compliant CSV files with protection
 * against Spreadsheet Formula Injection (CWE-1236 / CSV Injection).
 */
public final class CsvExportUtil {

    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    private CsvExportUtil() {
    }

    /**
     * Sanitizes a cell value for CSV export:
     * 1. Protects against formula injection by prefixing with a single quote (') if the value starts
     *    with '=', '+', '-', '@', '\t', or '\r'.
     * 2. Escapes double quotes by doubling them ("").
     * 3. Wraps the cell in quotes if it contains commas, double quotes, or line breaks.
     */
    public static String escapeCell(Object value) {
        if (value == null) {
            return "";
        }
        String str = value.toString();
        if (str.isEmpty()) {
            return "";
        }

        // Prevent formula injection
        char firstChar = str.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@' || firstChar == '\t' || firstChar == '\r') {
            str = "'" + str;
        }

        // RFC 4180 escaping
        boolean needsQuotes = str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r");
        if (needsQuotes) {
            str = "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    /**
     * Builds a CSV string from headers and data rows.
     */
    public static String buildCsv(List<String> headers, List<List<Object>> rows) {
        StringBuilder sb = new StringBuilder();

        // Write header row
        if (headers != null && !headers.isEmpty()) {
            for (int i = 0; i < headers.size(); i++) {
                sb.append(escapeCell(headers.get(i)));
                if (i < headers.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("\r\n");
        }

        // Write data rows
        if (rows != null) {
            for (List<Object> row : rows) {
                if (row == null) continue;
                for (int i = 0; i < row.size(); i++) {
                    sb.append(escapeCell(row.get(i)));
                    if (i < row.size() - 1) {
                        sb.append(",");
                    }
                }
                sb.append("\r\n");
            }
        }

        return sb.toString();
    }

    /**
     * Converts a CSV string to bytes with UTF-8 BOM for Microsoft Excel compatibility.
     */
    public static byte[] toCsvBytesWithBom(String csvContent) {
        byte[] contentBytes = (csvContent != null ? csvContent : "").getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[UTF8_BOM.length + contentBytes.length];
        System.arraycopy(UTF8_BOM, 0, result, 0, UTF8_BOM.length);
        System.arraycopy(contentBytes, 0, result, UTF8_BOM.length, contentBytes.length);
        return result;
    }
}
