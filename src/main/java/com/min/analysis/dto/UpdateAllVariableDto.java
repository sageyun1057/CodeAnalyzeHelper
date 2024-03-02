package com.min.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UpdateAllVariableDto {

    private Long id;

    private String url;

    private Boolean static_check;

    private Long start;

    private List<String> value;
}