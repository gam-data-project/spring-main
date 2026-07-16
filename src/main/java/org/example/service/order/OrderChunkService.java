package org.example.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.delivery.request.DeliveryFeeRowDto;
import org.example.dto.order.request.OrderChunkRequestDto;
import org.example.dto.order.request.OrderDeliveryDto;
import org.example.dto.order.request.OrderRequestDto;
import org.example.dto.order.request.OrderSalesItemDto;
import org.example.dto.order.response.OrderChunkResponseDto;
import org.example.dto.sales.request.SalesRequestDto;
import org.example.repository.DeliveryFeeMapper;
import org.example.repository.SalesMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Python crawler가 보낸 주문 chunk를
 * sales / delivery row로 펼쳐 일괄 저장하는 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderChunkService {

    private final SalesMapper salesMapper;
    private final DeliveryFeeMapper deliveryFeeMapper;

    /**
     * chunk 요청 1건을 검증하고 sales / delivery 테이블에 일괄 저장한다.
     * sales와 delivery 저장은 하나의 트랜잭션으로 묶는다.
     *
     * @param request chunk 저장 요청 DTO
     * @return 저장 결과 응답 DTO
     */

    @Transactional
    public OrderChunkResponseDto saveChunk(OrderChunkRequestDto request) {
        log.info(
                "saveChunk started: chunkId={}, chunkSeq={}, totalChunks={}",
                request != null ? request.getChunkId() : null,
                request != null ? request.getChunkSeq() : null,
                request != null ? request.getTotalChunks() : null
        );
        //chunk 요청의 필수값을 검증
        validateRequest(request);
        //chunk 요청을 sales row 목록으로 펼친다.
        List<SalesRequestDto> salesRows = toSalesRows(request);
        //chunk 요청을 delivery row 목록으로 변환한다.
        List<DeliveryFeeRowDto> deliveryRows = toDeliveryRows(request);

        log.info(
                "saveChunk parse completed: chunkId={}, salesRowCount={}, deliveryRowCount={}",
                request.getChunkId(),
                salesRows.size(),
                deliveryRows.size()
        );
        // 매출 정보 저장
        int savedSalesCount = 0;
        if (!salesRows.isEmpty()) {
            savedSalesCount = salesMapper.insertSalesList(salesRows);
            log.info(
                    "sales batch insert completed: chunkId={}, savedSalesCount={}",
                    request.getChunkId(),
                    savedSalesCount
            );
        } else {
            log.info("sales batch insert skipped: chunkId={}, reason=no sales rows", request.getChunkId());
        }
        // 배송 정보 저장
        int savedDeliveryCount = 0;
        if (!deliveryRows.isEmpty()) {
            savedDeliveryCount = deliveryFeeMapper.insertDeliveryFeeList(deliveryRows);
            log.info(
                    "delivery batch insert completed: chunkId={}, savedDeliveryCount={}",
                    request.getChunkId(),
                    savedDeliveryCount
            );
        } else {
            log.info("delivery batch insert skipped: chunkId={}, reason=no delivery rows", request.getChunkId());
        }
        // Chunk 저장 응답 DTO
        OrderChunkResponseDto response = buildSuccessResponse(
                request,
                savedSalesCount,
                savedDeliveryCount
        );

        log.info(
                "saveChunk completed: chunkId={}, receivedOrderCount={}, savedSalesCount={}, savedDeliveryCount={}",
                response.getChunkId(),
                response.getReceivedOrderCount(),
                response.getSavedSalesCount(),
                response.getSavedDeliveryCount()
        );

        return response;
    }

    /**
     * chunk 요청의 필수값을 검증한다.
     * 잘못된 요청은 400 응답 대상으로 처리할 수 있도록 IllegalArgumentException을 발생시킨다.
     *
     * @param request chunk 저장 요청 DTO
     */
    private void validateRequest(OrderChunkRequestDto request) {
        log.info("validateRequest started");

        if (request == null) {
            throw new IllegalArgumentException("request body is null");
        }
        if (isBlank(request.getChunkId())) {
            throw new IllegalArgumentException("chunk_id is required");
        }
        if (request.getChunkSeq() == null || request.getChunkSeq() < 1) {
            throw new IllegalArgumentException("chunk_seq must be greater than 0");
        }
        if (request.getTotalChunks() == null || request.getTotalChunks() < 1) {
            throw new IllegalArgumentException("total_chunks must be greater than 0");
        }
        if (request.getOrders() == null || request.getOrders().isEmpty()) {
            throw new IllegalArgumentException("orders must not be empty");
        }

        for (int i = 0; i < request.getOrders().size(); i++) {
            OrderRequestDto order = request.getOrders().get(i);

            if (order == null) {
                throw new IllegalArgumentException("orders[" + i + "] is null");
            }
            if (isBlank(order.getOrderNumber())) {
                throw new IllegalArgumentException("orders[" + i + "].order_number is required");
            }
            if (isBlank(order.getPlatform())) {
                throw new IllegalArgumentException("orders[" + i + "].platform is required");
            }
            if (isBlank(order.getOrderDate())) {
                throw new IllegalArgumentException("orders[" + i + "].order_date is required");
            }
            if (order.getSalesItems() == null || order.getSalesItems().isEmpty()) {
                throw new IllegalArgumentException("orders[" + i + "].sales_items must not be empty");
            }
            if (order.getDelivery() == null) {
                throw new IllegalArgumentException("orders[" + i + "].delivery is required");
            }
        }

        log.info("validateRequest completed: orderCount={}", request.getOrders().size());
    }


    /**
     * chunk 요청을 sales row 목록으로 펼친다.
     *
     * @param request chunk 저장 요청 DTO
     * @return sales 저장용 row 목록
     */
    private List<SalesRequestDto> toSalesRows(OrderChunkRequestDto request) {
        log.info("toSalesRows started: chunkId={}", request.getChunkId());

        List<SalesRequestDto> salesRows = new ArrayList<>();

        for (OrderRequestDto order : request.getOrders()) {
            for (OrderSalesItemDto item : order.getSalesItems()) {
                salesRows.add(toSalesRow(order, item));
            }
        }

        log.info("toSalesRows completed: chunkId={}, salesRowCount={}", request.getChunkId(), salesRows.size());
        return salesRows;
    }

    /**
     * 주문 1건 + 상품 1건을 sales row 1건으로 변환한다.
     *
     * @param order 주문 DTO
     * @param item 상품 DTO
     * @return sales 저장용 DTO
     */
    private SalesRequestDto toSalesRow(OrderRequestDto order, OrderSalesItemDto item) {
        SalesRequestDto row = new SalesRequestDto();
        row.setOrderNumber(order.getOrderNumber());
        row.setPlatform(order.getPlatform());
        row.setProductNameRaw(item.getProductNameRaw());
        row.setProductId(null);
        row.setQuantity(item.getQuantity() != null ? item.getQuantity() : 0);
        row.setProductTotal(item.getProductTotal() != null ? item.getProductTotal() : 0);
        row.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice() : 0);
        row.setShippingIncluded(Boolean.TRUE.equals(item.getShippingIncluded()));
        row.setOrderDate(order.getOrderDate());
        return row;
    }

    /**
     * chunk 요청을 delivery row 목록으로 변환한다.
     *
     * @param request chunk 저장 요청 DTO
     * @return delivery 저장용 row 목록
     */
    private List<DeliveryFeeRowDto> toDeliveryRows(OrderChunkRequestDto request) {
        log.info("toDeliveryRows started: chunkId={}", request.getChunkId());

        List<DeliveryFeeRowDto> deliveryRows = new ArrayList<>();

        for (OrderRequestDto order : request.getOrders()) {
            deliveryRows.add(toDeliveryRow(order));
        }

        log.info(
                "toDeliveryRows completed: chunkId={}, deliveryRowCount={}",
                request.getChunkId(),
                deliveryRows.size()
        );
        return deliveryRows;
    }


    /**
     * 주문 1건을 delivery row 1건으로 변환한다.
     *
     * @param order 주문 DTO
     * @return delivery 저장용 DTO
     */
    private DeliveryFeeRowDto toDeliveryRow(OrderRequestDto order) {
        OrderDeliveryDto delivery = order.getDelivery();

        DeliveryFeeRowDto row = new DeliveryFeeRowDto();
        row.setOrderNumber(order.getOrderNumber());
        row.setPlatform(order.getPlatform());
        row.setShippingIncluded(Boolean.TRUE.equals(delivery.getShippingIncluded()));
        row.setTotalDeliveryFee(delivery.getTotalDeliveryFee());
        row.setShippingCount(delivery.getShippingCount());
        row.setUnitPrice(delivery.getUnitPrice());
        row.setOrderDate(order.getOrderDate());

        return row;
    }


    /**
     * chunk 저장 성공 응답 DTO를 생성한다.
     *
     * @param request chunk 저장 요청 DTO
     * @param savedSalesCount 저장된 sales row 수
     * @param savedDeliveryCount 저장된 delivery row 수
     * @return 성공 응답 DTO
     */
    private OrderChunkResponseDto buildSuccessResponse(
            OrderChunkRequestDto request,
            int savedSalesCount,
            int savedDeliveryCount
    ) {
        return OrderChunkResponseDto.builder()
                .success(true)
                .message("chunk saved successfully")
                .chunkId(request.getChunkId())
                .chunkSeq(request.getChunkSeq())
                .totalChunks(request.getTotalChunks())
                .receivedOrderCount(request.getOrders().size())
                .savedSalesCount(savedSalesCount)
                .savedDeliveryCount(savedDeliveryCount)
                .build();
    }

    /**
     * 빈 문자열 여부를 확인한다.
     *
     * @param value 검사할 문자열
     * @return blank 여부
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
