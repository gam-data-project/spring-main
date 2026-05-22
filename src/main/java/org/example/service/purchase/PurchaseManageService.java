package org.example.service.purchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.purchase.request.PurchaseUpsertRequestDto;
import org.example.dto.purchase.response.PurchaseCommandResponseDto;
import org.example.dto.purchase.response.PurchaseOptionDto;
import org.example.repository.PurchaseMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseManageService {

    private static final String ALL = "ALL";

    private final PurchaseMapper purchaseMapper;


    /** 저장/수정 폼용 제품 옵션 조회 */
    public List<PurchaseOptionDto> getProducts(String largeCategory, String mediumCategory, String smallCategory) {
        String large = normalizeFilter(largeCategory);
        String medium = normalizeFilter(mediumCategory);
        String small = normalizeFilter(smallCategory);

        if (isAll(large) || isAll(medium) || isAll(small)) return Collections.emptyList();
        return purchaseMapper.selectDistinctProductsByCategory(large, medium, small);
    }

    /** 매입 저장 */
    @Transactional
    public PurchaseCommandResponseDto createPurchase(PurchaseUpsertRequestDto req) {
        String large = normalizeValue(req.getLargeCategory());
        String medium = normalizeValue(req.getMediumCategory());
        String small = normalizeValue(req.getSmallCategory());
        String productName = normalizeValue(req.getProductName());
        String supplier = normalizeValue(req.getSupplierName());

        if (!isValidCategoryValue(large) || !isValidCategoryValue(medium) || !isValidCategoryValue(small))
            return fail("대/중/소분류는 필수이며 '전체'는 저장값으로 사용할 수 없습니다.", null);
        if (productName.isEmpty()) return fail("제품명은 필수입니다.", null);
        if (req.getPurchaseDate() == null) return fail("매입일은 필수입니다.", null);
        if (supplier.isEmpty()) return fail("매입처는 필수입니다.", null);
        if (req.getUnitCost() == null || req.getUnitCost() < 0) return fail("단가는 0 이상이어야 합니다.", null);
        if (req.getQuantity() == null || req.getQuantity() < 0) return fail("수량은 0 이상이어야 합니다.", null);
        if (req.getTotalCost() == null || req.getTotalCost() < 0) return fail("총금액은 0 이상이어야 합니다.", null);

        Long categoryId = purchaseMapper.selectCategoryIdByPath(large, medium, small);
        if (categoryId == null) return fail("선택한 분류 조합을 찾을 수 없습니다.", null);

        Long productId = purchaseMapper.selectProductIdByCategoryAndName(categoryId, productName);
        if (productId == null) return fail("선택한 분류에 해당하는 제품을 찾을 수 없습니다.", null);

        int duplicated = purchaseMapper.countByUniqueKey(req.getPurchaseDate(), req.getPurchaseTime(), req.getTotalCost());
        if (duplicated > 0) return fail("동일한 (매입일, 매입시간, 총금액) 데이터가 이미 존재합니다.", null);

        int inserted = purchaseMapper.insertPurchase(
                productId, req.getPurchaseDate(), req.getPurchaseTime(),
                req.getQuantity(), req.getUnitCost(), req.getTotalCost(), supplier
        );
        if (inserted < 1) return fail("저장에 실패했습니다.", null);

        Long id = purchaseMapper.findPurchaseIdByUniqueKey(req.getPurchaseDate(), req.getPurchaseTime(), req.getTotalCost());
        return success("저장 완료", id);
    }



    /** 매입 수정 */
    @Transactional
    public PurchaseCommandResponseDto updatePurchase(Long id, PurchaseUpsertRequestDto req) {
        if (id == null || id < 1) return fail("유효하지 않은 ID입니다.", id);
        if (purchaseMapper.existsPurchaseById(id) < 1) return fail("수정 대상이 없습니다.", id);

        String large = normalizeValue(req.getLargeCategory());
        String medium = normalizeValue(req.getMediumCategory());
        String small = normalizeValue(req.getSmallCategory());
        String productName = normalizeValue(req.getProductName());
        String supplier = normalizeValue(req.getSupplierName());

        if (!isValidCategoryValue(large) || !isValidCategoryValue(medium) || !isValidCategoryValue(small))
            return fail("대/중/소분류는 필수이며 '전체'는 수정값으로 사용할 수 없습니다.", id);
        if (productName.isEmpty()) return fail("제품명은 필수입니다.", id);

        Long categoryId = purchaseMapper.selectCategoryIdByPath(large, medium, small);
        if (categoryId == null) return fail("선택한 분류 조합을 찾을 수 없습니다.", id);

        Long productId = purchaseMapper.selectProductIdByCategoryAndName(categoryId, productName);
        if (productId == null) return fail("선택한 분류에 해당하는 제품을 찾을 수 없습니다.", id);

        int duplicated = purchaseMapper.countByUniqueKeyExcludingId(id, req.getPurchaseDate(), req.getPurchaseTime(), req.getTotalCost());
        if (duplicated > 0) return fail("수정값과 동일한 (매입일, 매입시간, 총금액) 데이터가 이미 존재합니다.", id);

        int updated = purchaseMapper.updatePurchaseById(
                id, productId, req.getPurchaseDate(), req.getPurchaseTime(),
                req.getQuantity(), req.getUnitCost(), req.getTotalCost(), supplier
        );
        if (updated < 1) return fail("수정에 실패했습니다.", id);

        return success("수정 완료", id);
    }

    /** 매입 삭제 */
    @Transactional
    public PurchaseCommandResponseDto deletePurchase(Long id) {
        if (id == null || id < 1) return fail("유효하지 않은 ID입니다.", id);
        int deleted = purchaseMapper.deletePurchaseById(id);
        if (deleted < 1) return fail("삭제 대상이 없습니다.", id);
        return success("삭제 완료", id);
    }

    /** 문자열 공백 제거 */
    private String normalizeValue(String v) { return v == null ? "" : v.trim(); }

    /** 분류값 검증 (빈값/ALL/전체 금지) */
    private boolean isValidCategoryValue(String value) {
        if (value == null || value.isBlank()) return false;
        if ("전체".equals(value)) return false;
        return !"ALL".equalsIgnoreCase(value);
    }

    /** 실패 응답 생성 */
    private PurchaseCommandResponseDto fail(String message, Long id) {
        return PurchaseCommandResponseDto.builder().success(false).message(message).id(id).build();
    }

    /** 성공 응답 생성 */
    private PurchaseCommandResponseDto success(String message, Long id) {
        return PurchaseCommandResponseDto.builder().success(true).message(message).id(id).build();
    }


    /**
     * null/blank/전체/ALL을 ALL로 정규화한다.
     *
     * @param value 원본 문자열
     * @return 정규화된 문자열
     */
    private String normalizeFilter(String value) {
        if (value == null) return ALL;
        String v = value.trim();
        if (v.isEmpty()) return ALL;
        if ("전체".equals(v)) return ALL;
        if ("ALL".equalsIgnoreCase(v)) return ALL;
        return v;
    }

    /**
     * ALL 여부를 검사한다.
     *
     * @param value 검사 문자열
     * @return ALL 여부
     */
    private boolean isAll(String value) {
        return ALL.equalsIgnoreCase(normalizeFilter(value));
    }
}
