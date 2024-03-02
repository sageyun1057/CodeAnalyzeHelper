package com.min.analysis.controller;

import com.min.analysis.dto.*;
import com.min.analysis.service.FuncService;
import com.min.analysis.service.ClassInfoService;
import com.min.analysis.util.Util;
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
import java.util.Collections;
import java.util.List;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MainController {
    private final ClassInfoService classInfoService;
    private final FuncService funcService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String main(ClassInfoDto dto, Model model) {
        log.error("hello main");
        List<String> funcs = funcService.getAllPaths();
        log.error("funcs : " + funcs);
        List<String> classInfos = classInfoService.getAllInstanciatedClasses();
        log.error("classInfos : " + classInfos);

        Collections.sort(classInfos);

        model.addAttribute("files", funcs);
        model.addAttribute("infoes", classInfos);

        return "main";
    }

    @RequestMapping(value = "/update_all", method = RequestMethod.GET)
    public String update_all(HttpServletRequest request, Model model) {
        model.addAttribute("title", "Update All");
        return "update_all";
    }

    @RequestMapping(value = "/update_all", method = RequestMethod.POST)
    public String update_all_post(HttpServletRequest request, @RequestBody UpdateAllVariableDto update) {
        ClassInfoDto searchDto = Util.url2ClassInfoDto(update.getUrl());
        if(searchDto == null) {
            return "update_all";
        }

        ClassInfoDto dto = classInfoService.find(searchDto);
        if(dto == null) {
            return "update_all";
        }
        List<ClassVariableDto> list;
        if (update.getStatic_check()) {
            list = dto.getStaticMembers();
        } else {
            list = dto.getMembers();
        }
        for (int i = 0; i < update.getValue().size(); i++) {
            if (i + update.getStart() >= list.size()) {
                break;
            }
            Long variableId = list.get((int) (i + update.getStart())).getId();
            classInfoService.valueChange(variableId, update.getValue().get(i));
        }

        return "update_all";
    }

    @RequestMapping(value = "/make_all", method = RequestMethod.GET)
    public String make_all(HttpServletRequest request, Model model) {
        return "make_all";
    }

    @RequestMapping(value = "/make_all", method = RequestMethod.POST)
    public ResponseEntity<String> make_all_post(@RequestParam("url")String url,
                                                @RequestParam("start")Integer start,
                                                @RequestParam("end")Integer end) {
        ClassInfoDto dtoUrl = Util.url2ClassInfoDto(url);
        if(dtoUrl == null) {
            return ResponseEntity.badRequest().build();
        }

        ClassInfoDto templateDto = classInfoService.find(dtoUrl);
        if(templateDto == null || !templateDto.getInstance().equals("template")) {
            return ResponseEntity.badRequest().build();
        }
        if (url.contains("-template")) {
            url = url.substring(0, url.indexOf("-template"));
        }

        for (int i=start; i<=end;i++) {
            String newUrl = url + "-" + i;
            ClassInfoDto dto = Util.url2ClassInfoDto(newUrl);
            classInfoService.addClass(dto);
        }

        return ResponseEntity.ok("요청 성공!");
    }

    @RequestMapping(value = "/*", method = RequestMethod.GET)
    public String func(HttpServletRequest request, Model model) {
        return "not_found";
    }
}
