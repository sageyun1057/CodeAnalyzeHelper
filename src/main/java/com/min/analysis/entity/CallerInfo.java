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
public class CallerInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long callerId;

    private boolean isClassMember;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "classId", referencedColumnName = "id")
    private ClassInfo classInfo;
}
