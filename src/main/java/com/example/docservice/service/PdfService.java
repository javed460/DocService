package com.example.docservice.service;

import com.example.docservice.dto.ExcelRowData;
import com.example.docservice.exception.InvalidFileFormatException;
import com.example.docservice.util.PdfGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final ExcelService excelService;

    public byte[] generatePdfFromExcel(MultipartFile file) {
        validateFile(file);
        List<ExcelRowData> excelData = excelService.parseExcelFile(file).getData();
        String title = generateTitle(file.getOriginalFilename());
        return PdfGeneratorUtil.generatePdfFromExcelData(title, excelData);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileFormatException("File is empty");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx") 
                && !filename.toLowerCase().endsWith(".xls"))) {
            throw new InvalidFileFormatException(
                    "Invalid file format. Only .xlsx and .xls files are supported"
            );
        }
    }

    private String generateTitle(String filename) {
        if (filename == null) {
            return "Excel Data Report";
        }
        
        // Remove extension
        String title = filename.replaceAll("\\.(xlsx|xls)$", "");
        
        // Capitalize first letter and replace underscores/hyphens with spaces
        title = title.replace("_", " ").replace("-", " ");
        
        return title.substring(0, 1).toUpperCase() + title.substring(1);
    }
}
