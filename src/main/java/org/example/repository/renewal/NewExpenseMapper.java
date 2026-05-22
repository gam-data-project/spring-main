package org.example.repository.renewal;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dto.expense.request.NewExpenseSearchRequestDto;
import org.example.dto.expense.response.NewExpenseListItemDto;
import org.example.dto.expense.response.NewExpenseOptionDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 비용 조회 셀렉트박스 옵션 MyBatis Mapper.
 */

@Mapper
public interface NewExpenseMapper {

    /**
     * 대분류(비용) 하위 중분류 DISTINCT 조회.
     */
    List<NewExpenseOptionDto> selectDistinctMediumCategoriesByLarge(
            @Param("largeCategory") String largeCategory
    );

    /**
     * 대분류(비용) + 중분류 조건으로 소분류 DISTINCT 조회.
     * mediumCategory가 ALL이면 중분류 조건 제외.
     */
    List<NewExpenseOptionDto> selectDistinctSmallCategoriesByLargeMedium(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory
    );

    /**
     * 대/중/소분류 조건으로 유형(expense_type) DISTINCT 조회.
     * medium/small이 ALL이면 해당 조건 제외.
     */
    List<NewExpenseOptionDto> selectDistinctExpenseTypesByCategory(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory,
            @Param("smallCategory") String smallCategory
    );


    /**
     * 비용 목록 총 건수 조회(페이징용).
     *
     * @param request 조회 조건/페이지 정보
     * @return 총 건수
     */
    long countExpensePage(@Param("req") NewExpenseSearchRequestDto request);

    /**
     * 비용 목록 페이지 조회.
     *
     * @param request 조회 조건/페이지 정보
     * @return 페이지 데이터
     */
    List<NewExpenseListItemDto> selectExpensePage(@Param("req") NewExpenseSearchRequestDto request);


    /**
     * 대/중/소 분류 조합으로 category_id를 조회한다.
     *
     * @param largeCategory 대분류
     * @param mediumCategory 중분류
     * @param smallCategory 소분류
     * @return category_id, 없으면 null
     */
    Long selectCategoryIdByPath(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory,
            @Param("smallCategory") String smallCategory
    );

    /**
     * 유니크 키(발생일/발생시간/총금액) 중복 건수를 조회한다.
     *
     * @param expenseDate 발생일
     * @param expenseTime 발생시간(null 가능)
     * @param totalCost 총금액
     * @return 중복 건수
     */
    int countByUniqueKey(
            @Param("expenseDate") LocalDate expenseDate,
            @Param("expenseTime") LocalTime expenseTime,
            @Param("totalCost") Integer totalCost
    );

    /**
     * 수정 시 자기 자신(id)을 제외하고 유니크 키 중복 건수를 조회한다.
     *
     * @param id 대상 ID
     * @param expenseDate 발생일
     * @param expenseTime 발생시간(null 가능)
     * @param totalCost 총금액
     * @return 중복 건수
     */
    int countByUniqueKeyExcludingId(
            @Param("id") Long id,
            @Param("expenseDate") LocalDate expenseDate,
            @Param("expenseTime") LocalTime expenseTime,
            @Param("totalCost") Integer totalCost
    );

    /**
     * 비용 정보를 신규 등록한다.
     *
     * @return 반영 건수
     */
    int insertExpense(
            @Param("categoryId") Long categoryId,
            @Param("expenseDate") LocalDate expenseDate,
            @Param("expenseTime") LocalTime expenseTime,
            @Param("expenseType") String expenseType,
            @Param("unitCost") Integer unitCost,
            @Param("quantity") Integer quantity,
            @Param("totalCost") Integer totalCost,
            @Param("description") String description
    );

    /**
     * 유니크 키로 방금 저장된 ID를 조회한다.
     *
     * @param expenseDate 발생일
     * @param expenseTime 발생시간(null 가능)
     * @param totalCost 총금액
     * @return 대상 ID
     */
    Long findExpenseIdByUniqueKey(
            @Param("expenseDate") LocalDate expenseDate,
            @Param("expenseTime") LocalTime expenseTime,
            @Param("totalCost") Integer totalCost
    );

    /**
     * ID 기준으로 비용 정보를 수정한다.
     *
     * @return 반영 건수
     */
    int updateExpenseById(
            @Param("id") Long id,
            @Param("categoryId") Long categoryId,
            @Param("expenseDate") LocalDate expenseDate,
            @Param("expenseTime") LocalTime expenseTime,
            @Param("expenseType") String expenseType,
            @Param("unitCost") Integer unitCost,
            @Param("quantity") Integer quantity,
            @Param("totalCost") Integer totalCost,
            @Param("description") String description
    );

    /**
     * ID 기준으로 비용 정보를 삭제한다.
     *
     * @param id 삭제 대상 ID
     * @return 반영 건수
     */
    int deleteExpenseById(@Param("id") Long id);
}
