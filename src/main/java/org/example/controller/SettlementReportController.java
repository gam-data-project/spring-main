package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.settlementReport.request.SettlementReportRequestDto;
import org.example.dto.settlementReport.response.SettlementReportResponseDto;
import org.example.service.settlementReport.SettlementReportReadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/renewal/settlementReport")
@RequiredArgsConstructor
public class SettlementReportController {
    private final SettlementReportReadService settlementReadService;

    @GetMapping("/report")
    public ResponseEntity<SettlementReportResponseDto> getSettlementReport(
            @ModelAttribute SettlementReportRequestDto request
    ) {
        return ResponseEntity.ok(settlementReadService.getSettlementReport(request));
    }
}
