package com.min.analysis.repository;

import com.min.analysis.entity.ClassInfo;
import com.min.analysis.entity.ClassVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ClassInfoRepository extends JpaRepository<ClassInfo, Long> {
    @Query("select info from ClassInfo info where info.filePlace = :filePlace and info.className = :className and info.instance = :instance")
    Optional<ClassInfo> find(String filePlace, String className, String instance);

    @Query("select info from ClassInfo info where info.filePlace = :filePlace and info.className = :className")
    List<ClassInfo> findSameClass(String filePlace, String className);

    @Query("select info from ClassVariable info where info.classInfo.id = :classId")
    List<ClassVariable> findVariablesByClass(Long classId);
}
