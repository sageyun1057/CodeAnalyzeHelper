package com.min.analysis.util;

import com.min.analysis.dto.*;
import com.min.analysis.entity.*;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class Util {
    public static FuncInfoDto toDto(FuncInfo funcEntity) {
        String thisValue = funcEntity.getThisUrl() == null ? null : funcEntity.getThisUrl().substring(funcEntity.getThisUrl().lastIndexOf(":") + 1);
        FuncInfoDto dto = FuncInfoDto.builder()
                .id(funcEntity.getId())
                .definePlace(funcEntity.getDefinePlace())
                .name(funcEntity.getName())
                .thisUrl(funcEntity.getThisUrl())
                .thisValue(thisValue)
                .localList(new ArrayList<>())
                .build();
        return dto;
    }

    public static FuncInfo parseVariable(FuncInfoDto dto) {
        FuncInfo info = FuncInfo.builder()
                .id(dto.getId())
                .name(dto.getName())
                .thisUrl(dto.getThisUrl())
                .definePlace(dto.getDefinePlace())
                .build();
        return info;
    }

    // ======================================
    public static ClassInfoDto toDto(ClassInfo entity) {
        ClassInfoDto result = ClassInfoDto.builder()
                .id(entity.getId())
                .className(entity.getClassName())
                .filePlace(entity.getFilePlace())
                .instance(entity.getInstance())
                .members(new ArrayList<>())
                .staticMembers(new ArrayList<>())
                .build();

        entity.getVariables().forEach(variable -> {
            if(!variable.getIsStatic()) {
                result.getMembers().add(toDto(variable));
            }
        });

        return result;
    }

    public static ClassVariableDto toDto(ClassVariable entity) {
        return ClassVariableDto.builder()
                .id(entity.getId())
                .type(entity.getType())
                .name(entity.getName())
                .value(entity.getValue())
                .url(entity.getUrl())
                .isStatic(entity.getIsStatic())
                .classId(entity.getClassInfo().getId())
                .build();
    }

    public static ClassVariable toEntity(ClassVariableDto dto, ClassInfo target) {
        return ClassVariable.builder()
                .id(dto.getId())
                .type(dto.getType())
                .name(dto.getName())
                .value(dto.getValue())
                .url(dto.getUrl())
                .isStatic(dto.getIsStatic())
                .classInfo(target)
                .build();
    }

    public static LocalVariableInfoDto toDto(LocalVariableInfo variable) {
        return LocalVariableInfoDto.builder()
                .id(variable.getId())
                .type(variable.getType())
                .name(variable.getName())
                .value(variable.getValue())
                .url(variable.getUrl())
                .funcId(variable.getFuncInfo().getId())
                .build();
    }

    public static LocalVariableInfo toEntity(LocalVariableInfoDto dto, FuncInfo funcInfo) {
        return LocalVariableInfo.builder()
                .id(dto.getId())
                .type(dto.getType())
                .name(dto.getName())
                .value(dto.getValue())
                .url(dto.getUrl())
                .funcInfo(funcInfo)
                .build();
    }

    public static ClassInfoDto url2ClassInfoDto(String url) {
        url = url.substring(url.indexOf("/srcs"));
        String[] tokens = url.split(":");
        if(tokens.length != 2) {
            return null;
        }
        String path = tokens[0];
        String name = tokens[1];
        String instance = Constants.TEMPLATE;
        if(name.contains("-")) {
            int index = name.indexOf("-");
            instance = name.substring(index + 1);
            name = name.substring(0, index);
        }
        ClassInfoDto dto = ClassInfoDto.builder()
                .instance(instance)
                .className(name)
                .filePlace(path)
                .build();
        return dto;
    }

    public static boolean isEmpty(String str) {
        if(str == null || str.isEmpty()) {
            return true;
        }
        return false;
    }

}
