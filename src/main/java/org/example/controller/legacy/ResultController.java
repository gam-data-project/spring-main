package org.example.controller.legacy;

import org.example.domain.Report;
import org.example.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ResultController {
    @Autowired
    ResultService resultService;
    //년월로 매출,매입,비용을 총 집계하기
    @PostMapping("/result")
    public Boolean result(@RequestParam String date){
        return resultService.result(date);
    }
    //검색한 달의 카테고리별 매출, 매입, 비용을 결과와
    @PostMapping("/showreport")
    public List<Report> showReport(@RequestParam String date){
        return resultService.showReport(date);
    }

}
