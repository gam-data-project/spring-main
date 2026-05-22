package org.example.repository.renewal;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dto.purchase.request.NewPurchaseProductMappingSearchPageRequestDto;
import org.example.dto.purchase.request.NewPurchaseSearchRequestDto;
import org.example.dto.purchase.response.NewPurchaseListItemDto;
import org.example.dto.purchase.response.NewPurchaseOptionDto;
import org.example.dto.purchase.response.NewPurchaseProductMappingListItemDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 매입 조회 셀렉트박스 옵션 MyBatis Mapper.
 * category는 purchase.category_id가 아니라 product.category_id를 통해 조회한다.
 */
@Mapper
public interface NewPurchaseMapper {

    /** 대분류 DISTINCT 조회 */
    List<NewPurchaseOptionDto> selectDistinctLargeCategories();

    /** 선택한 대분류 기준 중분류 DISTINCT 조회 */
    List<NewPurchaseOptionDto> selectDistinctMediumCategoriesByLarge(
            @Param("largeCategory") String largeCategory
    );

    /** 선택한 대/중분류 기준 소분류 DISTINCT 조회 */
    List<NewPurchaseOptionDto> selectDistinctSmallCategoriesByLargeMedium(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory
    );

    /** 선택한 대/중/소분류 기준 매입처 DISTINCT 조회 */
    List<NewPurchaseOptionDto> selectDistinctSuppliersByCategory(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory,
            @Param("smallCategory") String smallCategory
    );

    /** 페이징용 총 건수 조회 */
    long countPurchasePage(@Param("req") NewPurchaseSearchRequestDto request);

    /** 페이징 목록 조회 */
    List<NewPurchaseListItemDto> selectPurchasePage(@Param("req") NewPurchaseSearchRequestDto request);



    /** 저장/수정 폼용 제품명 옵션 조회 */
    List<NewPurchaseOptionDto> selectDistinctProductsByCategory(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory,
            @Param("smallCategory") String smallCategory
    );

    /** 분류 경로로 category_id 조회 */
    Long selectCategoryIdByPath(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory,
            @Param("smallCategory") String smallCategory
    );

    /** category + product_name으로 product_id 조회 */
    Long selectProductIdByCategoryAndName(
            @Param("categoryId") Long categoryId,
            @Param("productName") String productName
    );

    /** 수정 전 대상 존재 확인 */
    int existsPurchaseById(@Param("id") Long id);

    /** 유니크(매입일,매입시간,총금액) 중복 확인 */
    int countByUniqueKey(
            @Param("purchaseDate") LocalDate purchaseDate,
            @Param("purchaseTime") LocalTime purchaseTime,
            @Param("totalCost") Integer totalCost
    );

    /** 수정 시 본인 제외 중복 확인 */
    int countByUniqueKeyExcludingId(
            @Param("id") Long id,
            @Param("purchaseDate") LocalDate purchaseDate,
            @Param("purchaseTime") LocalTime purchaseTime,
            @Param("totalCost") Integer totalCost
    );

    /** 매입 저장 */
    int insertPurchase(
            @Param("productId") Long productId,
            @Param("purchaseDate") LocalDate purchaseDate,
            @Param("purchaseTime") LocalTime purchaseTime,
            @Param("quantity") Integer quantity,
            @Param("unitCost") Integer unitCost,
            @Param("totalCost") Integer totalCost,
            @Param("supplierName") String supplierName
    );

    /** 유니크 키로 저장된 ID 조회 */
    Long findPurchaseIdByUniqueKey(
            @Param("purchaseDate") LocalDate purchaseDate,
            @Param("purchaseTime") LocalTime purchaseTime,
            @Param("totalCost") Integer totalCost
    );

    /** 매입 수정 */
    int updatePurchaseById(
            @Param("id") Long id,
            @Param("productId") Long productId,
            @Param("purchaseDate") LocalDate purchaseDate,
            @Param("purchaseTime") LocalTime purchaseTime,
            @Param("quantity") Integer quantity,
            @Param("unitCost") Integer unitCost,
            @Param("totalCost") Integer totalCost,
            @Param("supplierName") String supplierName
    );

    /** 매입 삭제 */
    int deletePurchaseById(@Param("id") Long id);


    /**
     * 제품 매핑 조회 페이지 총 건수
     *
     * @param request 발생일/키워드/페이지 요청
     * @return 총 건수
     */
    long countProductMappingPage(@Param("req") NewPurchaseProductMappingSearchPageRequestDto request);

    /**
     * 제품 매핑 조회 페이지 목록
     *
     * @param request 발생일/키워드/페이지 요청
     * @return 페이지 목록
     */
    List<NewPurchaseProductMappingListItemDto> selectProductMappingPage(
            @Param("req") NewPurchaseProductMappingSearchPageRequestDto request
    );
}
