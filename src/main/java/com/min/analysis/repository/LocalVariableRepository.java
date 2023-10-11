package com.min.analysis.repository;

import com.min.analysis.entity.LocalVariableInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalVariableRepository extends JpaRepository<LocalVariableInfo, Long> {
}
