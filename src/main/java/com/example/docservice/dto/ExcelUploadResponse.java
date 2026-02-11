package com.example.docservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelUploadResponse {
    private boolean success;
    private String message;
    private Integer totalRows;
    private List<ExcelRowData> data;
}
