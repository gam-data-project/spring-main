package org.example.service.purchase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.purchase.request.PurchaseProductMappingSearchPageRequestDto;
import org.example.dto.purchase.response.PurchaseProductMappingListItemDto;
import org.example.dto.purchase.response.PurchaseProductMappingPageResponseDto;
import org.example.repository.PurchaseMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseProductSearchService {

    private final PurchaseMapper purchaseMapper;

    /**
     * 제품 매핑 조회(발생일 + 키워드) 페이지 처리
     *
     * @param request 조회 요청
     * @return 페이징 응답
     */
    public PurchaseProductMappingPageResponseDto getProductMappingPage(
            PurchaseProductMappingSearchPageRequestDto request
    ) {
        PurchaseProductMappingSearchPageRequestDto normalized = normalizeProductMappingRequest(request);

        if (normalized.getOccurredDate() == null) {
            throw new IllegalArgumentException("발생일은 필수입니다.");
        }

        long totalCount = purchaseMapper.countProductMappingPage(normalized);
        List<PurchaseProductMappingListItemDto> items = (totalCount > 0)
                ? purchaseMapper.selectProductMappingPage(normalized)
                : Collections.emptyList();

        long totalPages = (totalCount == 0) ? 0 : (totalCount + normalized.getLimit() - 1) / normalized.getLimit();

        return PurchaseProductMappingPageResponseDto.builder()
                .page(normalized.getPageOrDefault())
                .size(normalized.getLimit())
                .totalCount(totalCount)
                .totalPages(totalPages)
                .items(items)
                .build();
    }

    /**
     * 제품 매핑 조회 요청값 정규화
     *
     * @param request 원본 요청
     * @return 정규화된 요청
     */
    private PurchaseProductMappingSearchPageRequestDto normalizeProductMappingRequest(
            PurchaseProductMappingSearchPageRequestDto request
    ) {
        PurchaseProductMappingSearchPageRequestDto src =
                (request == null) ? new PurchaseProductMappingSearchPageRequestDto() : request;

        String keyword = (src.getKeyword() == null) ? null : src.getKeyword().trim();
        if (keyword != null && keyword.isEmpty()) keyword = null;

        return PurchaseProductMappingSearchPageRequestDto.builder()
                .occurredDate(src.getOccurredDate())
                .keyword(keyword)
                .page(src.getPageOrDefault())
                .size(src.getSizeOrDefault())
                .build();
    }


}
