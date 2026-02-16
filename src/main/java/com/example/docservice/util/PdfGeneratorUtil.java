package com.example.docservice.util;

import com.example.docservice.dto.ExcelRowData;
import com.example.docservice.exception.PdfGenerationException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfGeneratorUtil {

    private static final float MARGIN = 30;
    private static final float TITLE_FONT_SIZE = 16;
    private static final float HEADER_FONT_SIZE = 10;
    private static final float CELL_FONT_SIZE = 9;
    private static final float ROW_HEIGHT = 20;
    private static final float CELL_PADDING = 5;
    private static final float MIN_COLUMN_WIDTH = 60;

    public static byte[] generatePdfFromExcelData(String title, List<ExcelRowData> excelData) {
        if (excelData == null || excelData.isEmpty()) {
            throw new PdfGenerationException("No data provided for PDF generation");
        }

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Extract headers from first row
            List<String> headers = new ArrayList<>(excelData.get(0).getColumns().keySet());

            // Use landscape orientation for better table display
            PDRectangle pageSize = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            float pageWidth = pageSize.getWidth();
            float tableWidth = pageWidth - (2 * MARGIN);

            // Calculate dynamic column widths based on content
            Map<String, Float> columnWidths = calculateColumnWidths(headers, excelData, tableWidth);

            PDPage page = new PDPage(pageSize);
            document.addPage(page);

            float yPosition = pageSize.getHeight() - MARGIN;

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Draw title
            yPosition = drawTitle(contentStream, title, pageWidth, yPosition);
            yPosition -= 20; // Space after title

            // Draw table header
            yPosition = drawTableHeader(contentStream, headers, MARGIN, yPosition, columnWidths);

            // Draw table rows
            for (ExcelRowData rowData : excelData) {
                // Check if we need a new page
                if (yPosition < MARGIN + ROW_HEIGHT) {
                    contentStream.close();
                    page = new PDPage(pageSize);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = pageSize.getHeight() - MARGIN;

                    // Redraw header on new page
                    yPosition = drawTableHeader(contentStream, headers, MARGIN, yPosition, columnWidths);
                }

                yPosition = drawTableRow(contentStream, rowData, headers, MARGIN, yPosition, columnWidths);
            }

            contentStream.close();
            document.save(outputStream);

            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new PdfGenerationException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private static Map<String, Float> calculateColumnWidths(List<String> headers, List<ExcelRowData> data, float totalWidth) throws IOException {
        Map<String, Float> columnWidths = new HashMap<>();
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        // Calculate max width needed for each column
        Map<String, Float> maxWidths = new HashMap<>();

        for (String header : headers) {
            // Start with header width
            float headerWidth = (font.getStringWidth(header) / 1000 * HEADER_FONT_SIZE) + (2 * CELL_PADDING);
            maxWidths.put(header, Math.max(headerWidth, MIN_COLUMN_WIDTH));

            // Check all data values for this column
            for (ExcelRowData row : data) {
                Object value = row.getColumns().get(header);
                String cellValue = value != null ? value.toString() : "";
                float cellWidth = (font.getStringWidth(cellValue) / 1000 * CELL_FONT_SIZE) + (2 * CELL_PADDING);
                maxWidths.put(header, Math.max(maxWidths.get(header), cellWidth));
            }
        }

        // Calculate total width needed
        float totalNeeded = maxWidths.values().stream().reduce(0f, Float::sum);

        // If total needed is more than available, scale down proportionally
        if (totalNeeded > totalWidth) {
            float scaleFactor = totalWidth / totalNeeded;
            for (String header : headers) {
                columnWidths.put(header, maxWidths.get(header) * scaleFactor);
            }
        } else {
            // Use calculated widths
            columnWidths.putAll(maxWidths);
        }

        return columnWidths;
    }

    private static float drawTitle(PDPageContentStream contentStream, String title, float pageWidth, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), TITLE_FONT_SIZE);

        float titleWidth = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
                .getStringWidth(title) / 1000 * TITLE_FONT_SIZE;
        float titleX = (pageWidth - titleWidth) / 2;

        contentStream.newLineAtOffset(titleX, yPosition);
        contentStream.showText(title);
        contentStream.endText();

        return yPosition - TITLE_FONT_SIZE - 10;
    }

    private static float drawTableHeader(PDPageContentStream contentStream, List<String> headers,
                                  float startX, float yPosition, Map<String, Float> columnWidths) throws IOException {
        float currentX = startX;
        float totalWidth = columnWidths.values().stream().reduce(0f, Float::sum);

        // Draw header background
        contentStream.setNonStrokingColor(0.85f, 0.85f, 0.85f);
        contentStream.addRect(startX, yPosition - ROW_HEIGHT, totalWidth, ROW_HEIGHT);
        contentStream.fill();
        contentStream.setNonStrokingColor(0, 0, 0);

        // Draw header borders
        drawRowBorder(contentStream, headers, startX, yPosition, columnWidths);

        // Draw header text
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), HEADER_FONT_SIZE);

        for (String header : headers) {
            float colWidth = columnWidths.get(header);
            contentStream.newLineAtOffset(currentX + CELL_PADDING, yPosition - 14);
            String displayText = fitTextToWidth(header, colWidth - (2 * CELL_PADDING), HEADER_FONT_SIZE,
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD));
            contentStream.showText(displayText);
            contentStream.newLineAtOffset(-(currentX + CELL_PADDING), -(yPosition - 14));
            currentX += colWidth;
        }
        contentStream.endText();

        return yPosition - ROW_HEIGHT;
    }

    private static float drawTableRow(PDPageContentStream contentStream, ExcelRowData rowData,
                               List<String> headers, float startX, float yPosition,
                               Map<String, Float> columnWidths) throws IOException {
        float currentX = startX;

        // Draw row borders
        drawRowBorder(contentStream, headers, startX, yPosition, columnWidths);

        // Draw cell text
        contentStream.beginText();
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), CELL_FONT_SIZE);

        Map<String, Object> columns = rowData.getColumns();
        for (String header : headers) {
            float colWidth = columnWidths.get(header);
            Object value = columns.get(header);
            String cellValue = value != null ? value.toString() : "";

            contentStream.newLineAtOffset(currentX + CELL_PADDING, yPosition - 14);
            String displayText = fitTextToWidth(cellValue, colWidth - (2 * CELL_PADDING), CELL_FONT_SIZE,
                    new PDType1Font(Standard14Fonts.FontName.HELVETICA));
            contentStream.showText(displayText);
            contentStream.newLineAtOffset(-(currentX + CELL_PADDING), -(yPosition - 14));
            currentX += colWidth;
        }
        contentStream.endText();

        return yPosition - ROW_HEIGHT;
    }

    private static void drawRowBorder(PDPageContentStream contentStream, List<String> headers,
                               float startX, float yPosition, Map<String, Float> columnWidths) throws IOException {
        float totalWidth = columnWidths.values().stream().reduce(0f, Float::sum);

        // Horizontal lines
        contentStream.moveTo(startX, yPosition);
        contentStream.lineTo(startX + totalWidth, yPosition);
        contentStream.moveTo(startX, yPosition - ROW_HEIGHT);
        contentStream.lineTo(startX + totalWidth, yPosition - ROW_HEIGHT);
        contentStream.stroke();

        // Vertical lines
        float currentX = startX;
        contentStream.moveTo(currentX, yPosition);
        contentStream.lineTo(currentX, yPosition - ROW_HEIGHT);
        contentStream.stroke();

        for (String header : headers) {
            currentX += columnWidths.get(header);
            contentStream.moveTo(currentX, yPosition);
            contentStream.lineTo(currentX, yPosition - ROW_HEIGHT);
            contentStream.stroke();
        }
    }

    private static String fitTextToWidth(String text, float maxWidth, float fontSize, PDType1Font font) throws IOException {
        float textWidth = font.getStringWidth(text) / 1000 * fontSize;

        if (textWidth <= maxWidth) {
            return text;
        }

        // Try to truncate with ellipsis
        String ellipsis = "...";
        float ellipsisWidth = font.getStringWidth(ellipsis) / 1000 * fontSize;

        StringBuilder fitted = new StringBuilder();
        for (char c : text.toCharArray()) {
            String temp = fitted.toString() + c;
            float tempWidth = font.getStringWidth(temp) / 1000 * fontSize;

            if (tempWidth + ellipsisWidth > maxWidth) {
                break;
            }
            fitted.append(c);
        }

        return fitted.length() > 0 ? fitted.toString() + ellipsis : "";
    }
}