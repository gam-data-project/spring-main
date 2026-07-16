package org.example.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dto.settlementDetail.request.SettlementDetailRequestDto;
import org.example.dto.settlementDetail.response.SettlementDetailOptionDto;
import org.example.dto.settlementDetail.response.SettlementDetailRowDto;

import java.util.List;

@Mapper
public interface SettlementDetailMapper {
    /** 대분류 DISTINCT 조회 */
    List<SettlementDetailOptionDto> selectDistinctLargeCategories();

    /** 선택한 대분류 기준 중분류 DISTINCT 조회 */
    List<SettlementDetailOptionDto> selectDistinctMediumCategoriesByLarge(
            @Param("largeCategory") String largeCategory
    );

    /** 선택한 대분류 + 중분류 기준 소분류 DISTINCT 조회 */
    List<SettlementDetailOptionDto> selectDistinctSmallCategoriesByLargeMedium(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory
    );

    List<SettlementDetailRowDto> selectSettlementDetailRows(
            @Param("req") SettlementDetailRequestDto request
    );
}
