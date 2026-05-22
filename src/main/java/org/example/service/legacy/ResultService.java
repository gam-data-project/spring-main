package org.example.service.legacy;

import org.example.domain.legacy.Report;
import org.example.repository.ResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResultService {
    @Autowired
    private ResultMapper resultMapper;

    public Boolean result(String date){
        String cnt = "";
        Map<String, String> param = new HashMap<>();
        param.put("date",date);
        param.put("cnt", cnt);
        resultMapper.result(param);
        Integer check = Integer.parseInt(param.get("cnt"));
        if (check > 1 ){
            return true;
        }
        else{
            return false;
        }
    }

    public List<Report> showReport(String date){
        List<Report> report = new ArrayList<>();
        report = resultMapper.showReport(date);

        return report;
    }
}
