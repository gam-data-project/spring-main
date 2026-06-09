package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dto.settlementReport.response.SettlementReportRowDto;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface SettlementReportMapper {
    List<SettlementReportRowDto> selectMonthlySettlementReport(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
