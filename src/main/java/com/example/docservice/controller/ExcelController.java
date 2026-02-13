package com.example.docservice.controller;

import com.example.docservice.dto.ExcelUploadResponse;
import com.example.docservice.service.ExcelService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/excel")
@Tag(name = "Excel Operations", description = "APIs for Excel file operations")
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ExcelUploadResponse> uploadExcelFile(
            @Parameter(description = "Excel file to upload (.xlsx or .xls)", required = true)
            @RequestParam("file") MultipartFile file) {
        
        ExcelUploadResponse response = excelService.parseExcelFile(file);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
