package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.example.domain.legacy.Report;

import java.util.List;
import java.util.Map;

@Mapper
public interface ResultMapper {
    void result(Map<String, String> param);
    List<Report> showReport(String month);
}
