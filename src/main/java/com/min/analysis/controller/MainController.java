package com.min.analysis.controller;

import com.min.analysis.dto.ClassInfoDto;
import com.min.analysis.dto.FuncInfoDto;
import com.min.analysis.service.FuncService;
import com.min.analysis.service.ClassInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
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

        model.addAttribute("files", funcs);
        model.addAttribute("infoes", classInfos);

        return "main";
    }

    @RequestMapping(value = "/*", method = RequestMethod.GET)
    public String func(HttpServletRequest request, Model model) {
        return "not_found";
    }
}
