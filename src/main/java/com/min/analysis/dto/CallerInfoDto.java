package com.min.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CallerInfoDto {
    private String path;

    private String variable;
}
