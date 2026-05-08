package org.example.service.renewal;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.DataFormatter;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.example.dto.renewal.BankTransactionRowDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class NewPurchaseExcelReadService {

    @Value("${app.renewal.purchase-import.base-dir}")
    private String baseDir;


    private static final int HEADER_ROW_INDEX = 0;

    //private static final String BASE_DIR = "C:\\gamfile";

    private static final String HEADER_DATE = "거래일자";
    private static final String HEADER_TIME = "시간";
    private static final String HEADER_WITHDRAW = "찾으신금액";
    private static final String HEADER_RECIPIENT = "기록사항";

    private final DataFormatter formatter = new DataFormatter();


    public List<BankTransactionRowDto> readRows(String fileName) throws IOException {
        Path filePath = Paths.get(baseDir, fileName);

        try (InputStream inputStream = Files.newInputStream(filePath)) {
            return readRows(inputStream);
        }
    }
    /**
     * 지정된 폴더(C:\gamfile)에 있는 엑셀 파일을 읽어 DTO 리스트로 반환한다.
     *
     * 예:
     * readRows("351계좌입출금.xlsx")
     *
     * @param fileName C:\gamfile 아래에 있는 파일명
     * @return 거래 데이터 DTO 리스트
     * @throws IOException 파일 읽기 실패 시 발생
     */
//    public List<BankTransactionRowDto> readRows(String fileName) throws IOException {
//        log.info("readrow(fileName");
//        Path filePath = Paths.get(BASE_DIR, fileName);
//        return readRows(filePath);
//    }

    /**
     * 지정된 파일 경로의 엑셀 파일을 읽어 DTO 리스트로 반환한다.
     *
     * @param filePath 엑셀 파일 경로
     * @return 거래 데이터 DTO 리스트
     * @throws IOException 파일 읽기 실패 시 발생
     */
//    public List<BankTransactionRowDto> readRows(Path filePath) throws IOException {
//        log.info("readrow(filePath)");
//        try (InputStream inputStream = Files.newInputStream(filePath)) {
//            return readRows(inputStream);
//        }
//    }

    /**
     * InputStream으로 전달된 엑셀 파일을 읽어
     * 매입 분류에 필요한 최소 거래 정보만 DTO 리스트로 반환한다.
     *
     * 추출 대상:
     * - 거래일자
     * - 시간
     * - 찾으신금액
     * - 기록사항
     *
     * @param inputStream 엑셀 파일 입력 스트림
     * @return 엑셀의 각 거래 행을 BankTransactionRowDto로 변환한 리스트
     * @throws IOException 파일 읽기 실패 시 발생
     */
    public List<BankTransactionRowDto> readRows(InputStream inputStream) throws IOException {
        log.info("readrow(inputstream)");
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            Row headerRow = sheet.getRow(HEADER_ROW_INDEX);
            if (headerRow == null) {
                throw new IllegalArgumentException("헤더 행을 찾을 수 없습니다.");
            }

            Map<String, Integer> headerMap = createHeaderMap(headerRow);
            validateHeaders(headerMap);

            List<BankTransactionRowDto> result = new ArrayList<>();

            for (int rowIndex = HEADER_ROW_INDEX + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                Integer withdrawAmount = getAmount(row.getCell(headerMap.get(HEADER_WITHDRAW)));
                LocalDate transactionDate = getLocalDate(row.getCell(headerMap.get(HEADER_DATE)));
                LocalTime transactionTime = getLocalTime(row.getCell(headerMap.get(HEADER_TIME)));
                String recipientName = getCellString(row.getCell(headerMap.get(HEADER_RECIPIENT)));

                if (isEmptyRow(transactionDate, transactionTime, withdrawAmount, recipientName)) {
                    continue;
                }

                result.add(BankTransactionRowDto.builder()
                        .transactionDate(transactionDate)
                        .transactionTime(transactionTime)
                        .withdrawAmount(withdrawAmount)
                        .recipientName(recipientName)
                        .build());
            }

            return result;

        } catch (InvalidFormatException e) {
            throw new IllegalArgumentException("올바른 엑셀 파일 형식이 아닙니다.", e);
        } catch (EncryptedDocumentException e) {
            throw new IllegalArgumentException("암호화된 엑셀 파일은 처리할 수 없습니다.", e);
        }
    }


    /**
     * 헤더 행을 읽어
     * "헤더명 -> 컬럼 인덱스" 형태의 맵으로 변환한다.
     *
     * @param headerRow 엑셀의 헤더 행
     * @return 헤더명과 컬럼 위치를 담은 맵
     */
    private Map<String, Integer> createHeaderMap(Row headerRow) {
        Map<String, Integer> headerMap = new HashMap<>();

        for (Cell cell : headerRow) {
            String headerName = formatter.formatCellValue(cell).trim();
            if (!headerName.isBlank()) {
                headerMap.put(headerName, cell.getColumnIndex());
            }
        }

        return headerMap;
    }

    /**
     * 엑셀 파일에 필요한 필수 헤더가 모두 존재하는지 검증한다.
     *
     * 필수 헤더:
     * - 거래일자
     * - 시간
     * - 찾으신금액
     * - 기록사항
     *
     * @param headerMap 헤더명과 컬럼 위치를 담은 맵
     */
    private void validateHeaders(Map<String, Integer> headerMap) {
        List<String> requiredHeaders = List.of(
                HEADER_DATE,
                HEADER_TIME,
                HEADER_WITHDRAW,
                HEADER_RECIPIENT
        );

        for (String header : requiredHeaders) {
            if (!headerMap.containsKey(header)) {
                throw new IllegalArgumentException("필수 헤더가 없습니다: " + header);
            }
        }
    }


    private boolean isEmptyRow(LocalDate transactionDate,
                               LocalTime transactionTime,
                               Integer withdrawAmount,
                               String recipientName) {
        return transactionDate == null
                || transactionTime == null
                || withdrawAmount == null
                || recipientName == null;
    }

    /**
     * 셀 값을 문자열로 안전하게 읽는다.
     *
     * @param cell 읽을 셀
     * @return trim 처리된 문자열 값, 없으면 빈 문자열
     */
    private String getCellString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return formatter.formatCellValue(cell).trim();
    }

    /**
     * 금액 셀을 읽어 Integer 금액으로 변환한다.
     *
     * 예:
     * - "43,500" -> 43500
     *
     * @param cell 금액 셀
     * @return 숫자로 변환된 금액, 값이 없으면 null
     */
    private Integer getAmount(Cell cell) {
        String raw = getCellString(cell);
        if (raw.isBlank()) {
            return null;
        }

        String numeric = raw.replace(",", "").replace("원", "").trim();
        if (numeric.isBlank()) {
            return null;
        }

        return Integer.parseInt(numeric);
    }

    /**
     * 거래일자 셀을 LocalDate로 변환한다.
     *
     * 지원 형식:
     * - 엑셀 날짜 셀
     * - yyyy-MM-dd
     * - yyyy.MM.dd
     * - yyyy/MM/dd
     *
     * @param cell 거래일자 셀
     * @return 변환된 LocalDate, 값이 없으면 null
     */
    private LocalDate getLocalDate(Cell cell) {
        String text = getCellString(cell);

        if (text.isBlank()) {
            return null;
        }

        return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 시간 셀을 LocalTime으로 변환한다.
     *
     * 지원 형식:
     * - 엑셀 시간 셀
     * - HH:mm:ss
     * - H:mm:ss
     * - HH:mm
     * - H:mm
     *
     * @param cell 시간 셀
     * @return 변환된 LocalTime, 값이 없으면 null
     */
    private LocalTime getLocalTime(Cell cell) {
        String text = getCellString(cell);

        if (text.isBlank()) {
            return null;
        }

        return LocalTime.parse(text, DateTimeFormatter.ofPattern("HH:mm:ss"));
    }


}
