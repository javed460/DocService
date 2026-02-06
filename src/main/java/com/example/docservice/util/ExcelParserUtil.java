package com.example.docservice.util;

import com.example.docservice.dto.ExcelRowData;
import com.example.docservice.exception.ExcelParsingException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExcelParserUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static List<ExcelRowData> parseExcel(InputStream inputStream, String filename) {
        try {
            Workbook workbook = createWorkbook(inputStream, filename);
            Sheet sheet = workbook.getSheetAt(0);
            
            List<ExcelRowData> rowDataList = new ArrayList<>();
            List<String> headers = new ArrayList<>();
            
            Iterator<Row> rowIterator = sheet.iterator();
            
            // Parse header row
            if (rowIterator.hasNext()) {
                Row headerRow = rowIterator.next();
                headers = extractHeaders(headerRow);
            }
            
            // Parse data rows
            int rowNumber = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                ExcelRowData rowData = parseRow(row, headers, rowNumber);
                rowDataList.add(rowData);
                rowNumber++;
            }
            
            workbook.close();
            return rowDataList;
            
        } catch (IOException e) {
            throw new ExcelParsingException("Error parsing Excel file: " + e.getMessage(), e);
        }
    }

    private static Workbook createWorkbook(InputStream inputStream, String filename) throws IOException {
        if (filename.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (filename.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new ExcelParsingException("Unsupported file format");
        }
    }

    private static List<String> extractHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            String header = getCellValueAsString(cell);
            headers.add(header.isEmpty() ? "Column_" + cell.getColumnIndex() : header);
        }
        return headers;
    }

    private static ExcelRowData parseRow(Row row, List<String> headers, int rowNumber) {
        Map<String, Object> columnData = new LinkedHashMap<>();
        
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            Object cellValue = getCellValue(cell);
            columnData.put(headers.get(i), cellValue);
        }
        
        return new ExcelRowData(rowNumber, columnData);
    }

    private static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FORMAT.format(cell.getDateCellValue());
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // Check if it's a whole number
                    if (numericValue == Math.floor(numericValue)) {
                        return (long) numericValue;
                    }
                    return numericValue;
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return evaluateFormula(cell);
            case BLANK:
                return null;
            default:
                return cell.toString();
        }
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATE_FORMAT.format(cell.getDateCellValue());
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return evaluateFormulaAsString(cell);
            case BLANK:
                return "";
            default:
                return cell.toString();
        }
    }

    private static Object evaluateFormula(Cell cell) {
        try {
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
            CellValue cellValue = evaluator.evaluate(cell);

            return switch (cellValue.getCellType()) {
                case NUMERIC -> cellValue.getNumberValue();
                case STRING -> cellValue.getStringValue();
                case BOOLEAN -> cellValue.getBooleanValue();
                default -> cell.getCellFormula();
            };
        } catch (Exception e) {
            return cell.getCellFormula();
        }
    }

    private static String evaluateFormulaAsString(Cell cell) {
        try {
            FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
            CellValue cellValue = evaluator.evaluate(cell);

            return switch (cellValue.getCellType()) {
                case NUMERIC -> String.valueOf(cellValue.getNumberValue());
                case STRING -> cellValue.getStringValue();
                case BOOLEAN -> String.valueOf(cellValue.getBooleanValue());
                default -> cell.getCellFormula();
            };
        } catch (Exception e) {
            return cell.getCellFormula();
        }
    }
}
