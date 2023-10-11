package com.min.analysis.controller;

import com.min.analysis.dto.FuncInfoDto;
import com.min.analysis.dto.LocalVariableInfoDto;
import com.min.analysis.service.FuncService;
import com.min.analysis.service.ClassInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor
@RequestMapping({"/func"})
public class FuncController {
    private final FuncService service;

    @RequestMapping(value = "/*", method = RequestMethod.GET)
    public String func(HttpServletRequest request, Model model) {
        String path = request.getRequestURI();
        path = path.substring(path.lastIndexOf("/") + 1);

        List<FuncInfoDto> result = service.getFuncListByPath(path);

        model.addAttribute("funcList", result);

        return "func";
    }

    @RequestMapping(value = "/add_func", method = RequestMethod.POST)
    public ResponseEntity<String> add_func(@RequestParam("name")String name,
                                           @RequestParam("path")String path) {
        path = path.substring(path.lastIndexOf("/") + 1);
        FuncInfoDto dto = FuncInfoDto.builder()
                .name(name)
                .definePlace(path)
                .build();
        Long funcId = service.addFunc(dto);
        if(funcId <= 0) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok("요청 성공!");
    }

    @RequestMapping(value = "/del_func", method = RequestMethod.DELETE)
    public ResponseEntity<String> del_func(@RequestParam("funcId")Long funcId) {
        boolean success = service.deleteFunc(funcId);
        if(!success) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok("success");
    }
    
    @RequestMapping(value = "/this_change", method = RequestMethod.POST)
    public ResponseEntity<String>  this_change(@RequestParam("funcId")Long funcId,
                              @RequestParam("url")String url) {
        boolean success = service.addThisUrl(funcId, url);
        if(!success) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok("요청 성공!");
    }


    @RequestMapping(value = "/add_var", method = RequestMethod.POST)
    public ResponseEntity<String> add_var(@RequestBody List<LocalVariableInfoDto> dtoList) {
        boolean result = service.addLocalVariables(dtoList);
        if(!result) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok("요청 성공!");
    }

    @RequestMapping(value = "/del_var", method = RequestMethod.DELETE)
    public ResponseEntity<String> del_var(@RequestParam("variableId")Long variableId) {
        service.delLocalVariable(variableId);

        return ResponseEntity.ok("요청 성공!");
    }

    @RequestMapping(value = "/var_change", method = RequestMethod.POST)
    public ResponseEntity<String>  var_change(@RequestParam("variableId")Long variableId,
                                               @RequestParam("value")String value) {
        Long id = service.valueChange(variableId, value);
        if(id < 0) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok("요청 성공!");
    }
}
