package com.min.analysis.service;

import com.min.analysis.dto.CallerInfoDto;
import com.min.analysis.dto.ClassInfoDto;
import com.min.analysis.dto.ClassVariableDto;
import com.min.analysis.entity.*;
import com.min.analysis.parser.JavaParserImpl;
import com.min.analysis.repository.CallerInfoRepository;
import com.min.analysis.repository.ClassInfoRepository;
import com.min.analysis.repository.ClassVariableRepository;
import com.min.analysis.repository.LocalVariableRepository;
import com.min.analysis.parser.ClassParser;
import com.min.analysis.util.Constants;
import com.min.analysis.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class ClassInfoService {
    private final ClassInfoRepository repository;
    private final ClassVariableRepository variableRepository;
    private final LocalVariableRepository localVariableRepository;
    private final CallerInfoRepository callerInfoRepository;

    public ClassInfo createTemplate(ClassInfoDto classInfo) {
        ClassInfo template = find(classInfo.getFilePlace(), classInfo.getClassName(), Constants.TEMPLATE);
        if(template != null) {
            return template;
        }
        try {
            String path = classInfo.getFilePlace();
            Collection<ClassInfo> classInfoList = null;
            if(path.endsWith(".java")) {
                log.error("call javaparser");
                JavaParserImpl parser = new JavaParserImpl();
                classInfoList = parser.parse(path);
            } else if(path.endsWith("txt")) {
                FileReader reader = new FileReader("." + classInfo.getFilePlace());
                BufferedReader br = new BufferedReader(reader);
                List<String> content = br.lines().collect(Collectors.toList());
                classInfoList = ClassParser.ParseClass(content, classInfo.getFilePlace());
            }

            if(classInfoList != null) {
                classInfoList.stream().forEach( parsedInfo -> {
                    ClassInfo registeredInfo = find(parsedInfo.getFilePlace(), parsedInfo.getClassName(), Constants.TEMPLATE);
                    if(registeredInfo != null) {
                        log.error(registeredInfo + " is already registered");
                    } else {
                        repository.save(parsedInfo);
                    }
                });
                template = find(classInfo.getFilePlace(), classInfo.getClassName(), Constants.TEMPLATE);
            }
        } catch (IOException e) {
            log.error("createTemplate failed - " + e);
        }
        return template;
    }

    public ClassInfo find(String filePlace, String className, String instance) {
        Optional<ClassInfo> entity = repository.find(filePlace, className, instance);
        return entity.isPresent() ? entity.get() : null;
    }

    public ClassInfoDto find(ClassInfoDto dto) {
        ClassInfo classInfo = find(dto.getFilePlace(), dto.getClassName(), dto.getInstance());

        if(classInfo == null) {
            return null;
        }

        ClassInfo template = findTemplate(dto);

        ClassInfoDto result = Util.toDto(classInfo);

        template.getVariables().forEach(variable -> {
            if(variable.getIsStatic()) {
                result.getStaticMembers().add(Util.toDto(variable));
            }
        });

        return result;
    }

    public ClassInfo findTemplate(ClassInfoDto dto) {
        return find(dto.getFilePlace(), dto.getClassName(), Constants.TEMPLATE);
    }

    public Long addClass(ClassInfoDto dto) {
        ClassInfo registered = find(dto.getFilePlace(), dto.getClassName(), dto.getInstance());
        if(registered != null) {
            return registered.getId();
        }
        ClassInfo template = findTemplate(dto);
        if(template == null) {
            template = createTemplate(dto);
        }

        ClassInfo info = ClassInfo.builder()
                .className(dto.getClassName())
                .filePlace(dto.getFilePlace())
                .instance(dto.getInstance())
                .variables(new ArrayList<>())
                .callers(new ArrayList<>())
                .build();

        if(template != null) {
            template.getVariables().forEach(templateVariable -> {
                if(!templateVariable.getIsStatic()) {
                    ClassVariable variable = ClassVariable.builder()
                            .type(templateVariable.getType())
                            .name(templateVariable.getName())
                            .value(templateVariable.getValue())
                            .url(templateVariable.getUrl())
                            .isStatic(false)
                            .classInfo(info)
                            .build();
                    info.getVariables().add(variable);
                }
            });
        }

        ClassInfo result = repository.save(info);
        return result == null ? Long.valueOf(-1) : result.getId();
    }

    public void deleteClass(long classNo) {
        Optional<ClassInfo> opt = repository.findById(classNo);
        if(opt.isPresent()) {
            ClassInfo info = opt.get();
            info.getVariables().forEach(variable -> {
                if(variable.getUrl() != null) {
                    valueChange(variable.getId(), "");
                }
            });
            repository.delete(info);
        }
    }

    public Long addVariable(List<ClassVariableDto> dtoList) {
        Optional<ClassInfo> targetOpt = repository.findById(dtoList.get(0).getClassId());
        if(!targetOpt.isPresent()) {
            return Long.valueOf(-1);
        }
        ClassInfo target = targetOpt.get();
        for(ClassVariableDto dto : dtoList) {
            ClassVariable variable = Util.toEntity(dto, target);
            target.getVariables().add(variable);
        }
        repository.save(target);
        return target.getId();
    }

    public boolean deleteVariable(long classId, long variableId) {
        Optional<ClassInfo> targetOpt = repository.findById(classId);
        if(!targetOpt.isPresent()) {
            return false;
        }
        ClassInfo target = targetOpt.get();
        //remove caller count
        valueChange(variableId, "");

        target.getVariables().removeIf(variable -> {
            return variable.getId() == variableId;
        });

        repository.save(target);
        variableRepository.deleteById(variableId);

        return true;
    }

    public boolean addUrl(long variableId, boolean classMember, String url) {
        ClassInfoDto dto = Util.url2ClassInfoDto(url);
        ClassInfo target = find(dto.getFilePlace(), dto.getClassName(), dto.getInstance());
        if(target == null) {
            log.error("Class instance not made yet - ", target);
            return false;
        }
        CallerInfo caller = CallerInfo.builder()
                .callerId(variableId)
                .isClassMember(classMember)
                .classInfo(target)
                .build();
        target.getCallers().add(caller);
        repository.save(target);
        return true;
    }

    public boolean deleteUrl(long variableId, boolean classMember, String url) {
        if(url.contains("/func/")) {
            return true;
        }
        ClassInfoDto dto = Util.url2ClassInfoDto(url);
        ClassInfo target = find(dto.getFilePlace(), dto.getClassName(), dto.getInstance());
        if(target == null) {
            return false;
        }
        //valueChange and deleteClass are cyclic.
        //So need check caller size
        if(target.getCallers().size() > 0) {
            AtomicReference<CallerInfo> caller = new AtomicReference<>();

            target.getCallers().forEach(callerEntity -> {
                if(callerEntity.getCallerId() == variableId && callerEntity.isClassMember() == classMember) {
                    caller.set(callerEntity);
                }
            });
            target.getCallers().remove(caller.get());
            repository.save(target);
            if(caller.get() != null) {
                callerInfoRepository.deleteById(caller.get().getId());
            }

            if(target.getCallers().size() == 0) {
                boolean deleteSelf = false;
                ClassInfo self = variableRepository.findById(variableId).get().getClassInfo();
                if(target.getId() == self.getId()) {
                    deleteSelf = true;
                }
                deleteClass(target.getId());
                if(deleteSelf) {
                    return false;
                }
                //TODO how deal with cyclic?
            }
        }
        return true;
    }

    public Long valueChange(long variableId, String value) {
        Optional<ClassVariable> variableOpt = variableRepository.findById(variableId);
        if(!variableOpt.isPresent()) {
            log.error("wrong variable id");
            return Long.valueOf(-1);
        }

        ClassVariable variable = variableOpt.get();
        String originUrl = variable.getUrl();

        if(originUrl != null && !originUrl.isEmpty()) {
            if(!deleteUrl(variableId, true, originUrl)) {
                log.error("delete " + originUrl + " failed!");
                return Long.valueOf(-1);
            }
        }

        variable.setUrl("");

        if(value.contains("/srcs")) {
            if(!addUrl(variableId, true, value)) {
                return Long.valueOf(-1);
            }
            variable.setUrl(value);
            value = value.substring(value.lastIndexOf(":") + 1);
        }
        variable.setValue(value);
        return variableRepository.save(variable).getId();
    }

    public List<ClassInfoDto> findSameClass(String path, String name) {
        List<ClassInfoDto> result = new ArrayList<>();
        List<ClassInfo> infos = repository.findSameClass(path, name);
        infos.stream().forEach(entity -> {
            ClassInfoDto dto = Util.toDto(entity);
            result.add(dto);
        });
        return result;
    }

    public ClassInfoDto findById(long classNo) {
        ClassInfoDto dto = null;
        Optional<ClassInfo> opt = repository.findById(classNo);
        if(opt.isPresent()) {
            dto = Util.toDto(opt.get());
            dto = find(dto);
        }
        return dto;
    }

    public List<String> getAllInstanciatedClasses() {
        List<ClassInfo> infoes =  repository.findAll();

        HashSet<String> pathSet = new HashSet<>();
        infoes.stream().forEach(info -> pathSet.add(info.getFilePlace() + ":" + info.getClassName()));

        return new ArrayList<>(pathSet);
    }

    public List<CallerInfoDto> getCallers(ClassInfoDto dto) {
        ClassInfo target = find(dto.getFilePlace(), dto.getClassName(), dto.getInstance());
        ArrayList<CallerInfoDto> callerList = new ArrayList<>();
        if(target.getCallers() == null) {
            return callerList;
        }

        target.getCallers().stream().forEach(caller -> {
            Long variableId = caller.getCallerId();

            if (caller.isClassMember()) {
                Optional<ClassVariable> variableOpt = variableRepository.findById(variableId);
                if(variableOpt.isPresent()) {
                    ClassInfo callerClass = variableOpt.get().getClassInfo();
                    CallerInfoDto infoDto = CallerInfoDto.builder()
                            .path(callerClass.getFilePlace() + ":" + callerClass.getClassName() + "-" + callerClass.getInstance())
                            .variable(callerClass.getClassName() + "-" + callerClass.getInstance() + "::" + variableOpt.get().getName())
                            .build();
                    callerList.add(infoDto);
                }
            } else {
                Optional<LocalVariableInfo> variableOpt = localVariableRepository.findById(variableId);
                if (variableOpt.isPresent()) {
                    FuncInfo funcInfo = variableOpt.get().getFuncInfo();
                    CallerInfoDto infoDto = CallerInfoDto.builder()
                            .path("/func/" + funcInfo.getDefinePlace())
                            .variable(funcInfo.getName() + "::" + variableOpt.get().getName() + " (" + funcInfo.getDefinePlace() + ")")
                            .build();
                    callerList.add(infoDto);
                }
            }
        });

        return callerList;
    }
}
