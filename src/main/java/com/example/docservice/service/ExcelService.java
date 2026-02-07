package com.example.docservice.service;

import com.example.docservice.dto.ExcelRowData;
import com.example.docservice.dto.ExcelUploadResponse;
import com.example.docservice.exception.ExcelParsingException;
import com.example.docservice.exception.InvalidFileFormatException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.example.docservice.util.ExcelParserUtil.parseExcel;

@Service
public class ExcelService {

    public ExcelUploadResponse parseExcelFile(MultipartFile file) {
        validateFile(file);
        
        try {
            List<ExcelRowData> parsedData = parseExcel(
                    file.getInputStream(), 
                    file.getOriginalFilename()
            );
            
            ExcelUploadResponse response = new ExcelUploadResponse();
            response.setSuccess(true);
            response.setMessage("File parsed successfully");
            response.setTotalRows(parsedData.size());
            response.setData(parsedData);
            
            return response;
            
        } catch (IOException e) {
            throw new ExcelParsingException("Failed to read file: " + e.getMessage(), e);
        }
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
        
        if (file.getSize() == 0) {
            throw new InvalidFileFormatException("File size is 0 bytes");
        }
    }
}
