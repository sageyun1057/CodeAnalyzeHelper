package com.min.analysis.entity;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@EntityListeners(value = {AuditingEntityListener.class})
public class ClassVariable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private String name;

    private String value;

    private String url;

    private Boolean isStatic;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "classId", referencedColumnName = "id")
    private ClassInfo classInfo;

public void setId(Long id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
