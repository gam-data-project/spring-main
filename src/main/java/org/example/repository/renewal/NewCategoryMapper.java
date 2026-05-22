package org.example.repository.renewal;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.dto.category.response.NewCategoryListItemDto;
import org.example.dto.category.response.NewCategoryOptionDto;
import org.example.dto.category.request.NewCategorySearchRequestDto;

import java.util.List;

/**
 * new_category 조회 전용 MyBatis 매퍼 인터페이스.
 */
@Mapper
public interface NewCategoryMapper {
    /**
     * 대분류 유니크 목록 조회.
     *
     * @return 대분류 옵션 리스트
     */
    List<NewCategoryOptionDto> selectDistinctLargeCategories();

    /**
     * 대분류로 중분류 유니크 목록 조회.
     *
     * @param largeCategory 대분류
     * @return 중분류 옵션 리스트
     */
    List<NewCategoryOptionDto> selectDistinctMediumCategoriesByLarge(
            @Param("largeCategory") String largeCategory
    );

    /**
     * 대분류+중분류로 소분류 유니크 목록 조회.
     *
     * @param largeCategory 대분류
     * @param mediumCategory 중분류
     * @return 소분류 옵션 리스트
     */
    List<NewCategoryOptionDto> selectDistinctSmallCategoriesByLargeMedium(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory
    );




    /**
     * 카테고리 목록 전체 건수 조회(페이징용).
     */
    long countCategoryPage(@Param("req") NewCategorySearchRequestDto request);

    /**
     * 카테고리 목록 페이지 조회.
     */
    List<NewCategoryListItemDto> selectCategoryPage(@Param("req") NewCategorySearchRequestDto request);




    /**
     * 유니크 키(대/중/소) 중복 건수를 조회한다.
     */
    int countByUniqueKey(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory,
            @Param("smallCategory") String smallCategory
    );

    /**
     * 수정 시 자기 자신(id)을 제외하고 유니크 키 중복 건수를 조회한다.
     */
    int countByUniqueKeyExcludingId(
            @Param("id") Long id,
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory,
            @Param("smallCategory") String smallCategory
    );

    /**
     * 카테고리를 신규 등록한다.
     */
    int insertCategory(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory,
            @Param("smallCategory") String smallCategory
    );

    /**
     * 유니크 키로 등록된 ID를 조회한다.
     */
    Long findIdByUniqueKey(
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory,
            @Param("smallCategory") String smallCategory
    );

    /**
     * ID 기준으로 카테고리를 수정한다.
     */
    int updateCategoryById(
            @Param("id") Long id,
            @Param("largeCategory") String largeCategory,
            @Param("mediumCategory") String mediumCategory,
            @Param("smallCategory") String smallCategory
    );

    /**
     * ID 기준으로 카테고리를 삭제한다.
     */
    int deleteCategoryById(@Param("id") Long id);


}
