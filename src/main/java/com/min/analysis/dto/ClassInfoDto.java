package com.min.analysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ClassInfoDto {
    private Long id;

    private String className;

    private String filePlace;

    private String instance;

    private List<ClassVariableDto> members;

    private List<ClassVariableDto> staticMembers;
}
