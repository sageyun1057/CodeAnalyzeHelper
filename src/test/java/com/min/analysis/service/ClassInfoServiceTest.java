package com.min.analysis.service;

import com.min.analysis.dto.ClassInfoDto;
import com.min.analysis.dto.ClassVariableDto;
import com.min.analysis.entity.ClassInfo;
import com.min.analysis.entity.ClassVariable;
import com.min.analysis.util.Constants;
import com.min.analysis.util.Util;
import org.apache.tomcat.util.bcel.Const;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
//        TestClass.cpp content
//            #Foo
//            int@test_int
//            Bar@bar
//            <S>
//            Bar@static_Bar
//
//            #Bar
//            double@test_double
//            <S>
//            int@static_int
@SpringBootTest
@Transactional
public class ClassInfoServiceTest {
    @Autowired
    private ClassInfoService service;

    @Test
    void urlParse() {
        String url = "/srcs/TestClass.cpp:Foo";
        ClassInfoDto classInfo = Util.url2ClassInfoDto(url);
        assertEquals(classInfo.getClassName(), "Foo", "className check");
        assertEquals(classInfo.getFilePlace(), "/srcs/TestClass.cpp", "filePlace check");
        assertEquals(classInfo.getInstance(), Constants.TEMPLATE, "instance check");

        url = "http/127.0.0.1:8088/srcs/TestClass.cpp:Bar-1";
        classInfo = Util.url2ClassInfoDto(url);
        assertEquals(classInfo.getClassName(), "Bar", "className check");
        assertEquals(classInfo.getFilePlace(), "/srcs/TestClass.cpp", "filePlace check");
        assertEquals(classInfo.getInstance(), "1", "instance check");

        url = "http/127.0.0.1:8088/srcs/TestClass.cpp";
        classInfo = Util.url2ClassInfoDto(url);
        assertNull(classInfo);
    }

    @Test
    void addClassInstance() {
        String url = "/srcs/TestClass.cpp:Foo-1";
        ClassInfoDto searchDto = Util.url2ClassInfoDto(url);
        Long id = service.addClass(searchDto);
        assertTrue(id > 0);
        ClassInfoDto find = service.find(searchDto);
        assertEquals(find.getId(), id, "Id");
        assertEquals(find.getFilePlace(), searchDto.getFilePlace());
        assertEquals(find.getClassName(), searchDto.getClassName());
        assertEquals(find.getInstance(), searchDto.getInstance());
    }

    @Test
    void addDupClassInstance() {
        String url = "/srcs/DupTestClass.cpp:Foo";
        ClassInfoDto searchDto = Util.url2ClassInfoDto(url);
        Long id = service.addClass(searchDto);
        assertTrue(id > 0);
    }

    @Test
    void addVariable() {
        String url = "/srcs/TestClass.cpp:Foo-1";
        ClassInfoDto searchDto = Util.url2ClassInfoDto(url);
        Long id = service.addClass(searchDto);

        ClassInfoDto result = service.findById(id);
        assertEquals(2, result.getMembers().size(), "Member size");
        assertEquals(1, result.getStaticMembers().size(), "Static Member size");

        String TEST_TYPE = "test_type";
        String TEST_NAME = "test_name";
        String TEST_VALUE = "test_value";

        ClassVariableDto dto = ClassVariableDto.builder()
                .classId(id)
                .isStatic(false)
                .type(TEST_TYPE)
                .name(TEST_NAME)
                .value(TEST_VALUE)
                .build();

        ArrayList<ClassVariableDto> variables = new ArrayList<>();
        variables.add(dto);
        service.addVariable(variables);

        result = service.findById(id);

        assertEquals(3, result.getMembers().size(), "Member size");
        ClassVariableDto resultVariable = result.getMembers().get(2);
        assertEquals(dto.getClassId(), resultVariable.getClassId(), "classId");
        assertEquals(dto.getType(), resultVariable.getType(), "type");
        assertEquals(dto.getName(), resultVariable.getName(), "name");
        assertEquals(dto.getValue(), resultVariable.getValue(), "value");
    }

    @Test
    void delVariable() {
        String url = "/srcs/TestClass.cpp:Foo-1";
        ClassInfoDto searchDto = Util.url2ClassInfoDto(url);
        Long id = service.addClass(searchDto);

        ClassInfoDto result = service.findById(id);
        assertEquals(2, result.getMembers().size(), "Member size");
        assertEquals(1, result.getStaticMembers().size(), "Static Member size");

        boolean success = service.deleteVariable(id, result.getMembers().get(0).getId());
        result = service.findById(id);
        assertTrue(success);
        assertEquals(1, result.getMembers().size(), "Member size");

        success = service.deleteVariable(id, result.getMembers().get(0).getId());
        result = service.findById(id);
        assertTrue(success);
        assertEquals(0, result.getMembers().size(), "Member size");
    }

    @Test
    void changeValue() {
        String url = "/srcs/TestClass.cpp:Foo-1";
        ClassInfoDto searchDto = Util.url2ClassInfoDto(url);
        Long id = service.addClass(searchDto);

        ClassInfoDto result = service.findById(id);
        Long variableId = result.getMembers().get(0).getId();

        String TEST_VALUE = "test_value";
        service.valueChange(variableId, TEST_VALUE);
        result = service.findById(id);
        assertEquals(TEST_VALUE, result.getMembers().get(0).getValue());

        service.valueChange(variableId, url);
        result = service.findById(id);
        assertEquals("Foo-1", result.getMembers().get(0).getValue());
        assertEquals(url, result.getMembers().get(0).getUrl());
    }
}
