package com.dgsw.chwigo.dto.response;

import com.dgsw.chwigo.domain.enums.Category;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public record CategoryResponse(String code, String label) {

    private static final Map<Category, String> LABELS = Map.of(
            Category.FOOD, "식품",
            Category.DAILY, "생활용품",
            Category.ELECTRONICS, "전자제품",
            Category.CLOTHING, "의류",
            Category.OTHER, "기타"
    );

    public static List<CategoryResponse> all() {
        return Arrays.stream(Category.values())
                .map(c -> new CategoryResponse(c.name(), LABELS.get(c)))
                .toList();
    }
}
