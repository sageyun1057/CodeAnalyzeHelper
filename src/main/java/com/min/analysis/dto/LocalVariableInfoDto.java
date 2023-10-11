package com.min.analysis.dto;

import com.min.analysis.entity.FuncInfo;
import lombok.*;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class LocalVariableInfoDto {
    private Long id;

    private Long funcId;

    private String type;

    private String name;

    private String value;

    private String url;
}
