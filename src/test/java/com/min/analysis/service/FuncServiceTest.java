package com.min.analysis.service;

import com.min.analysis.dto.FuncInfoDto;
import com.min.analysis.dto.LocalVariableInfoDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class FuncServiceTest {
    @Autowired
    private FuncService service;

    @Test
    void addFunction() {
        FuncInfoDto dto = FuncInfoDto.builder()
                        .name("test")
                        .definePlace("test")
                        .build();

        Long funcId = service.addFunc(dto);
        assertTrue(funcId > 0, "function Id는 0보다 커야 합니다.");
    }

    @Test
    void addNoNameFunction() {
        FuncInfoDto dto = FuncInfoDto.builder()
                .build();

        Long funcId = service.addFunc(dto);
        assertEquals(Long.valueOf(-1), funcId, "name, definePlace가 없을 경우 function id는 -1을 반환해야 합니다.");
    }

    @Test
    void addVariable() {
        FuncInfoDto dto = FuncInfoDto.builder()
                .name("test")
                .definePlace("test")
                .build();
        Long funcId = service.addFunc(dto);

        LocalVariableInfoDto local1 = LocalVariableInfoDto.builder()
                .funcId(funcId)
//                .definePlace(dto.getDefinePlace())
                .type("int")
                .name("test_int")
                .build();
//        service.addLocalVariable(local1);

        LocalVariableInfoDto local2 = LocalVariableInfoDto.builder()
                .funcId(funcId)
//                .definePlace(dto.getDefinePlace())
                .type("double")
                .name("test_double")
                .build();

//        service.addLocalVariable(local2);

    }

}
