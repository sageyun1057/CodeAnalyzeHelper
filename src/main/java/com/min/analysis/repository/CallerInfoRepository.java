package com.min.analysis.repository;

import com.min.analysis.entity.CallerInfo;
import com.min.analysis.entity.ClassInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface CallerInfoRepository extends JpaRepository<CallerInfo, Long> {
}
