package com.min.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ExcelDto {
    private String value;
    private String url;
}
