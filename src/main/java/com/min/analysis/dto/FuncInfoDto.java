package com.min.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class FuncInfoDto {
    private Long id;

    private String definePlace;

    private String name;

    private String thisUrl;

    private String thisValue;

    private List<LocalVariableInfoDto> localList;
}
