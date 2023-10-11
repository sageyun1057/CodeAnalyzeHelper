package com.min.analysis.entity;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@EntityListeners(value = {AuditingEntityListener.class})
public class ClassInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1024, nullable = false)
    private String className;

    @Column(length = 1024, nullable = false)
    private String filePlace;

    private String instance;

    @OneToMany(mappedBy = "classInfo", cascade = CascadeType.ALL)
    private List<ClassVariable> variables;

    @OneToMany(mappedBy = "classInfo", cascade = CascadeType.ALL)
    private List<CallerInfo> callers;
}
