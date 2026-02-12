package com.example.docservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelRowData {
    private int rowNumber;
    private Map<String, Object> columns;
}
