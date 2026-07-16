package org.example.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.order.request.OrderChunkRequestDto;
import org.example.dto.order.response.OrderChunkResponseDto;
import org.example.service.order.OrderChunkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Python crawler가 전송한 주문 chunk를 수신하는 API 컨트롤러.
 */
@Slf4j
@RestController
@RequestMapping("/renewal/orders")
@RequiredArgsConstructor
public class OrderChunkController {

    private final OrderChunkService orderChunkService;

    /**
     * 주문 chunk 1건을 수신하여 batch insert를 수행한다.
     *
     * @param request chunk 요청 DTO
     * @return 저장 결과 응답 DTO
     */
    @PostMapping("/chunk")
    public ResponseEntity<OrderChunkResponseDto> saveOrderChunk(
            @RequestBody OrderChunkRequestDto request
    ) {
        log.info(
                "saveOrderChunk request received: chunkId={}, chunkSeq={}, totalChunks={}",
                request != null ? request.getChunkId() : null,
                request != null ? request.getChunkSeq() : null,
                request != null ? request.getTotalChunks() : null
        );

        try {
            OrderChunkResponseDto response = orderChunkService.saveChunk(request);

            log.info(
                    "saveOrderChunk completed: chunkId={}, receivedOrderCount={}",
                    response.getChunkId(),
                    response.getReceivedOrderCount()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("saveOrderChunk bad request: error={}", e.getMessage());

            return ResponseEntity.badRequest().body(
                    OrderChunkResponseDto.builder()
                            .success(false)
                            .message(e.getMessage())
                            .chunkId(request != null ? request.getChunkId() : null)
                            .chunkSeq(request != null ? request.getChunkSeq() : null)
                            .totalChunks(request != null ? request.getTotalChunks() : null)
                            .receivedOrderCount(
                                    request != null && request.getOrders() != null
                                            ? request.getOrders().size()
                                            : 0
                            )
                            .savedSalesCount(0)
                            .savedDeliveryCount(0)
                            .build()
            );
        }
    }
}
