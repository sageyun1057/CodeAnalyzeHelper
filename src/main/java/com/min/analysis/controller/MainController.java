package com.min.analysis.controller;

import com.min.analysis.dto.*;
import com.min.analysis.service.FuncService;
import com.min.analysis.service.ClassInfoService;
import com.min.analysis.util.Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.poi.ss.usermodel.*;

@Controller
@Log4j2
@RequiredArgsConstructor
public class MainController {
    private final ClassInfoService classInfoService;
    private final FuncService funcService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String main(ClassInfoDto dto, Model model) {
        List<String> funcs = funcService.getAllPaths();
        List<String> classInfos = classInfoService.getAllInstanciatedClasses();

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

    @RequestMapping(value = "/plain/*", method = RequestMethod.GET)
    public String show_plain(HttpServletRequest request, Model model) {
        String url = request.getRequestURI();
        String file = url.substring("/plain/".length());
        StringBuilder content = new StringBuilder();
        try {
            FileSystemResource resource = new FileSystemResource(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), "UTF-8"));
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
            reader.close();

        } catch (Exception e) {

        }

        model.addAttribute("title", file);
        model.addAttribute("plain", content.toString());
        return "show_plain";
    }

    @RequestMapping(value = "/excel/*", method = RequestMethod.GET)
    public String show_excel(HttpServletRequest request, Model model) {
        String url = request.getRequestURI();
        String file = url.substring("/excel/".length()) + ".xlsx";

        try {
            ArrayList<ArrayList<ExcelDto>> sheet = readExcelFile(file);
            model.addAttribute("sheet", sheet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        model.addAttribute("title", file);

        return "show_excel";
    }

    public ArrayList<ArrayList<ExcelDto>> readExcelFile(String filePath) throws IOException {
        FileInputStream excelFile = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(excelFile);
        Sheet sheet = workbook.getSheetAt(0);
        ArrayList<ArrayList<ExcelDto>> result = new ArrayList<>();
        for (Row row : sheet) {
            ArrayList<ExcelDto> rowArray = new ArrayList<>();
            for (Cell cell : row) {
                ExcelDto.ExcelDtoBuilder builder = ExcelDto.builder();
                String content = cell.toString();
                if (content.startsWith("http")) {
                    int colonIdx = content.lastIndexOf(":");
                    int backSlashIdx = content.lastIndexOf("/");
                    int max = colonIdx > backSlashIdx ? colonIdx : backSlashIdx;
                    builder.value(content.substring(max + 1));
                    builder.url(content);
                } else {
                    builder.value(content);
                    builder.url("");
                }
                rowArray.add(builder.build());
            }
            result.add(rowArray);
        }

        workbook.close();
        excelFile.close();
        return result;
    }

    @RequestMapping(value = "/*", method = RequestMethod.GET)
    public String func(HttpServletRequest request, Model model) {
        return "not_found";
    }
}