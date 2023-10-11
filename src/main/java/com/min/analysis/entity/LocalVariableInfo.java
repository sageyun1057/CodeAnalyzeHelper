package com.min.analysis.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
public class LocalVariableInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String name;

    private String value;

    private String url;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "funcId", referencedColumnName = "id")
    private FuncInfo funcInfo;


    public void setUrl(String url) {
        this.url = url;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
