package com.min.analysis.controller;

import com.min.analysis.dto.*;
import com.min.analysis.dto.ClassVariableDto;
import com.min.analysis.entity.*;
import com.min.analysis.service.FuncService;
import com.min.analysis.service.ClassInfoService;
import com.min.analysis.util.Constants;
import com.min.analysis.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@Log4j2
@RequestMapping({"/srcs"})
@RequiredArgsConstructor
public class ClassController {
    private final ClassInfoService service;

    @RequestMapping(value = "/**/{path:.+}", method = RequestMethod.GET)
    public String srcs(HttpServletRequest request, Model model) {
        String url = request.getRequestURI();
        ClassInfoDto searchDto = Util.url2ClassInfoDto(url);
        if(searchDto == null) {
            return "not_found";
        }

        ClassInfoDto dto = service.find(searchDto);

        if(dto == null) {
            ClassInfo template = service.findTemplate(searchDto);
            if (template == null) {
                template = service.createTemplate(searchDto);
            }
            //If file not exist or file didn't have class, template is null
            if(template != null) {
                if (searchDto.getInstance().equals(Constants.TEMPLATE)) {
                    dto = Util.toDto(template);
                }
            }
        }
        model.addAttribute("info", dto);

        if(dto != null) {
            List<ClassInfoDto> sameClasses = service.findSameClass(searchDto.getFilePlace(), searchDto.getClassName());
            ArrayList<String> instanceList = new ArrayList<>();
            sameClasses.stream().forEach(info -> {
                instanceList.add(info.getInstance());
            });
            Collections.sort(instanceList);
            model.addAttribute("instanceList", instanceList);

            List<CallerInfoDto> callers = service.getCallers(dto);
            model.addAttribute("callerList", callers);
        }

        return "class";
    }

    @RequestMapping(value = "/add_class", method = RequestMethod.POST)
    public ResponseEntity<String> add_class(@RequestParam("url")String url) {
        ClassInfoDto dto = Util.url2ClassInfoDto(url);
        Long classId = service.addClass(dto);
        if(classId <= 0) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok("요청 성공!");
    }

    @RequestMapping(value = "/delete_class", method = RequestMethod.POST)
    public String delete_class(ClassInfoDto dto) {
        service.deleteClass(dto.getId());
        return "redirect:" + dto.getFilePlace() + ":" + dto.getClassName() + "-" + Constants.TEMPLATE;
    }

    @RequestMapping(value = "/add_var", method = RequestMethod.POST)
    public ResponseEntity<String> add_var(HttpServletRequest request, @RequestBody List<ClassVariableDto> dtoList) {
        Long variableId = service.addVariable(dtoList);
        if(variableId <= 0) {
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok("요청 성공!");
    }

    @RequestMapping(value = "/del_var", method = RequestMethod.DELETE)
    public ResponseEntity<String> del_var(@RequestParam("class_id")String classId, @RequestParam("variable_id")String variableId) {
        boolean result = service.deleteVariable(Long.parseLong(classId), Long.parseLong(variableId));
        if(!result) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok("요청 성공!");
    }

    @RequestMapping(value = "/value_change", method = RequestMethod.POST)
    public ResponseEntity<String>  value_change(@RequestParam("variable_id")String variableId,
                                              @RequestParam("value")String value) {
        Long id = service.valueChange(Long.parseLong(variableId), value);
        if(id < 0) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok("요청 성공!");
    }
}
