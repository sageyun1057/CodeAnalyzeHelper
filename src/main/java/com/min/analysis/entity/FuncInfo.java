package com.min.analysis.entity;

import com.min.analysis.dto.LocalVariableInfoDto;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class FuncInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String definePlace;

    @Column(nullable = false)
    private String name;

    private String thisUrl;

    @OneToMany(mappedBy = "funcInfo", cascade = CascadeType.ALL)
    private List<LocalVariableInfo> localList;

    public void setThisUrl(String url) {
        thisUrl = url;
    }
}
