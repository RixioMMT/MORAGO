package com.habsida.morago.resolver;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.habsida.morago.model.dto.CategoryDTO;
import com.habsida.morago.model.inputs.PagingInput;
import com.habsida.morago.model.results.PageOutput;
import com.habsida.morago.serviceImpl.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryQueryResolver implements GraphQLQueryResolver {
    private final CategoryService categoryService;

    public PageOutput<CategoryDTO> getAllCategoriesByPaging(PagingInput input) {
        return categoryService.getAllCategories(input);
    }

    public CategoryDTO getCategoryById(Long id) {
        return categoryService.getCategoryById(id);
    }

    public CategoryDTO getCategoryByName(String name) {
        return categoryService.getCategoryByName(name);
    }

    public PageOutput<CategoryDTO> getCategoryByStatusIsActiveByPaging(PagingInput input) {
        return categoryService.getCategoryByStatusIsActive(input);
    }
}
