package com.min.analysis.service;

import com.min.analysis.dto.FuncInfoDto;
import com.min.analysis.dto.LocalVariableInfoDto;
import com.min.analysis.entity.*;
import com.min.analysis.repository.FuncRepository;
import com.min.analysis.repository.LocalVariableRepository;
import com.min.analysis.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Log4j2
@RequiredArgsConstructor
public class FuncService {
    private final FuncRepository funcRepository;
    private final LocalVariableRepository localVariableRepository;
    private final ClassInfoService classInfoService;

    public List<FuncInfoDto> getFuncListByPath(String path) {
        List<FuncInfoDto> result = new ArrayList<>();
        for(FuncInfo func : funcRepository.findAllByPath(path)) {
            FuncInfoDto dto = Util.toDto(func);
            func.getLocalList().forEach(localVariable -> {
                dto.getLocalList().add(Util.toDto(localVariable));
            });
            result.add(dto);
        }
        return result;
    }

    public List<String> getAllPaths() {
        List<FuncInfo> infoes = funcRepository.findAll();
        HashSet<String> pathSet = new HashSet<>();
        infoes.stream().forEach(info -> pathSet.add(info.getDefinePlace()));

        return new ArrayList<>(pathSet);
    }

    public Long addFunc(FuncInfoDto dto) {
        if(Util.isEmpty(dto.getName()) || Util.isEmpty(dto.getDefinePlace())) {
            log.error("FuncInfo must have name and define place");
            return Long.valueOf(-1);
        }
        FuncInfo entity = Util.parseVariable(dto);
        FuncInfo result = funcRepository.save(entity);
        return result.getId();
    }

    public boolean deleteFunc(Long funcId) {
        Optional<FuncInfo> funcInfoOpt = funcRepository.findById(funcId);
        if(!funcInfoOpt.isPresent()) {
            return false;
        }
        FuncInfo funcInfo = funcInfoOpt.get();
        ArrayList<Long> variableIds = new ArrayList<>();
        funcInfo.getLocalList().forEach(variable -> {
            variableIds.add(variable.getId());
        });
        variableIds.forEach(id -> {
            delLocalVariable(id);
        });
        funcRepository.delete(funcInfo);
        return true;
    }

    public Long addLocalVariable(LocalVariableInfoDto dto, FuncInfo funcInfo) {
        LocalVariableInfo variableInfo = Util.toEntity(dto, funcInfo);
        funcInfo.getLocalList().add(variableInfo);
        return funcRepository.save(funcInfo).getId();
    }

    public boolean addLocalVariables(List<LocalVariableInfoDto> dtoList) {
        log.error("dtoList.get(0) : " + dtoList.get(0));
        Optional<FuncInfo> funcInfoOpt = funcRepository.findById(dtoList.get(0).getFuncId());
        if(!funcInfoOpt.isPresent()) {
            return false;
        }

        AtomicBoolean result = new AtomicBoolean(true);
        dtoList.forEach(dto -> {
            if(addLocalVariable(dto, funcInfoOpt.get()) <= 0) {
                result.set(false);
            }
        });
        return result.get();
    }

    public boolean delLocalVariable(Long variableId) {
        Optional<LocalVariableInfo> variableInfoOpt = localVariableRepository.findById(variableId);
        if(!variableInfoOpt.isPresent()) {
            return false;
        }

        LocalVariableInfo variable = variableInfoOpt.get();
        //remove caller info
        valueChange(variableId, "");
        FuncInfo funcInfo = variable.getFuncInfo();

        funcInfo.getLocalList().remove(variable);
        funcRepository.save(funcInfo);
        localVariableRepository.delete(variable);

        return true;
    }

    public FuncInfoDto findById(Long id) {
        Optional<FuncInfo> opt = funcRepository.findById(id);
        FuncInfo entity = opt.get();

        return Util.toDto(entity);
    }

    public Long valueChange(long variableId, String value) {
        Optional<LocalVariableInfo> variableOpt = localVariableRepository.findById(variableId);
        if(!variableOpt.isPresent()) {
            log.error("wrong variable id");
            return Long.valueOf(-1);
        }

        LocalVariableInfo variable = variableOpt.get();
        String originUrl = variable.getUrl();

        if(originUrl != null && !originUrl.isEmpty()) {
            classInfoService.deleteUrl(variableId, false, originUrl);
        }

        variable.setUrl("");

        if(value.contains("/srcs/")) {
            boolean success = classInfoService.addUrl(variableId, false, value);
            if(!success) {
                log.error(value + " is not made yet");
                return Long.valueOf(-1);
            }

            variable.setUrl(value);
            value = value.substring(value.lastIndexOf(":") + 1);
        }
        if(value.contains("/plain/") || value.contains("/excel/")) {
            variable.setUrl(value);
            value = value.substring(value.lastIndexOf("/") + 1);
        }

        if(value.contains("/func/")) {
            variable.setUrl(value);
            value = "func() - " + value.substring(value.lastIndexOf("/") + 1);
        }
        variable.setValue(value);
        return localVariableRepository.save(variable).getId();
    }

    public boolean addThisUrl(Long funcId, String url) {
        Optional<FuncInfo> funcInfoOpt = funcRepository.findById(funcId);
        if(!funcInfoOpt.isPresent()) {
            return false;
        }
        FuncInfo funcInfo = funcInfoOpt.get();
        funcInfo.setThisUrl(url);
        funcRepository.save(funcInfo);
        return true;
    }
}
