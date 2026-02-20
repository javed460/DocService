package com.example.docservice.controller;

import com.example.docservice.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/pdf")
@Tag(name = "PDF Operations", description = "APIs for PDF generation")
public class PdfController {

    private final PdfService pdfService;

    public PdfController(PdfService pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping(value = "/generate-from-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Convert Excel file to PDF")
    public ResponseEntity<byte[]> generatePdfFromExcel(@RequestParam("file") MultipartFile file) {
        
        byte[] pdfBytes = pdfService.generatePdfFromExcel(file);
        
        // Generate filename
        String originalFilename = file.getOriginalFilename();
        String pdfFilename = originalFilename != null 
                ? originalFilename.replaceAll("\\.(xlsx|xls)$", ".pdf")
                : "document.pdf";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", pdfFilename);
        headers.setContentLength(pdfBytes.length);
        
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
