package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.settlementDetail.request.SettlementDetailRequestDto;
import org.example.dto.settlementDetail.response.SettlementDetailOptionDto;
import org.example.dto.settlementDetail.response.SettlementDetailResponseDto;
import org.example.service.settlementDetail.SettlementDetailReadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/renewal/settlementDetail")
@RequiredArgsConstructor
public class SettlementDetailController {

    private final SettlementDetailReadService settlementDetailReadService;

    @GetMapping("/options/large")
    public ResponseEntity<List<SettlementDetailOptionDto>> getLargeCategories() {
        return ResponseEntity.ok(settlementDetailReadService.getLargeCategories());
    }

    @GetMapping("/options/medium")
    public ResponseEntity<List<SettlementDetailOptionDto>> getMediumCategories(
            @RequestParam(required = false, defaultValue = "ALL") String largeCategory
    ) {
        return ResponseEntity.ok(
                settlementDetailReadService.getMediumCategories(largeCategory)
        );
    }

    @GetMapping("/options/small")
    public ResponseEntity<List<SettlementDetailOptionDto>> getSmallCategories(
            @RequestParam(required = false, defaultValue = "ALL") String largeCategory,
            @RequestParam(required = false, defaultValue = "ALL") String mediumCategory
    ) {
        return ResponseEntity.ok(
                settlementDetailReadService.getSmallCategories(largeCategory, mediumCategory)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<SettlementDetailResponseDto> getSettlementDetail(
            @ModelAttribute SettlementDetailRequestDto request
    ) {
        return ResponseEntity.ok(
                settlementDetailReadService.getSettlementDetail(request)
        );
    }
}
