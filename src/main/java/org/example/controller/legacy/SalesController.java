package org.example.controller.legacy;

import org.example.service.SalesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class SalesController {
    @Autowired
    SalesService salesService;
    //농라 매출 엑셀 업로드
    @PostMapping("/uploadnongrasales")
    public Boolean uploadNongraSales(@RequestParam(value="file") MultipartFile multi) throws IOException {
        return salesService.uploadNongraSales(multi);
    }
    //농라 정산 엑셀 업로드
    @PostMapping("/uploadnongracommission")
    public Boolean uploadNongraCommission(@RequestParam(value="file") MultipartFile multi) throws IOException {
        return salesService.uploadNongraCommission(multi);
    }

//    @PostMapping("/uploadsmartstoresales")
//    public Boolean uploadSmartstoreSales(@RequestParam(value="file") MultipartFile multi){
//
//    }
//
//    @PostMapping("/uploadsmartstorecommission")
//    public Boolean uploadSmartstoreCal(@RequestParam(value="file") MultipartFile multi){
//
//    }
}
