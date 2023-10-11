package com.min.analysis.repository;

import com.min.analysis.entity.FuncInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FuncRepository extends JpaRepository<FuncInfo, Long> {
    @Query("select func from FuncInfo func where func.definePlace = :definePlace")
    List<FuncInfo> findAllByPath(String definePlace);
}
