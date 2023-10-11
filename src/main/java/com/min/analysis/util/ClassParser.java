package com.min.analysis.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.min.analysis.entity.ClassInfo;
import com.min.analysis.entity.ClassVariable;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ClassParser {
    public static Collection<ClassInfo> ParseClass(List<String> content, String path) {
        HashMap<String, ClassInfo> result = new HashMap<>();
        boolean is_static = false;

        ClassInfo classInfo = null;
        for(String line : content) {
            if(line.startsWith("#")) {
                classInfo = ClassInfo.builder()
                        .className(line.substring(1))
                        .filePlace(path)
                        .instance(Constants.TEMPLATE)
                        .variables(new ArrayList<>())
                        .build();
                continue;
            }
            if(line.isEmpty()) {
                result.put(classInfo.getClassName(), classInfo);
                is_static = false;
            } else if (line.equals("<S>")) {
                is_static = true;
            } else {
                ClassVariable variable = parseLine(classInfo, line, is_static);
                classInfo.getVariables().add(variable);
            }
        }

        result.put(classInfo.getClassName(), classInfo);

        return result.values();
    }

    private static ClassVariable parseLine(ClassInfo classInfo, String line, boolean is_static) {
        String[] tokens = line.split("@");
        if(tokens.length < 2) {
            throw new RuntimeException(line + " is malformed");
        }
        return ClassVariable.builder()
                .type(tokens[0])
                .name(tokens[1])
                .value(tokens.length == 3 ? tokens[2] : "")
                .isStatic(is_static)
                .classInfo(classInfo)
                .build();
    }
}
