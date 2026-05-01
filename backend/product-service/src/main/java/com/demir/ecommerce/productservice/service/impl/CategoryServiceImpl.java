package com.demir.ecommerce.productservice.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.productservice.dto.category.request.CategoryCreateRequest;
import com.demir.ecommerce.productservice.dto.category.request.CategoryUpdateRequest;
import com.demir.ecommerce.productservice.dto.category.response.CategoryFilterResponse;
import com.demir.ecommerce.productservice.dto.category.response.CategoryResponse;
import com.demir.ecommerce.productservice.dto.category.response.CategoryTreeResponse;
import com.demir.ecommerce.productservice.entity.Category;
import com.demir.ecommerce.productservice.exception.message.CategoryErrorMessage;
import com.demir.ecommerce.productservice.repository.CategoryRepository;
import com.demir.ecommerce.productservice.service.CategoryService;
import com.demir.ecommerce.productservice.util.SlugUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private static final int DEFAULT_SORT_ORDER = 0;

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponse create(CategoryCreateRequest request) {
        Category category = new Category();

        category.setName(request.name());
        category.setSlug(generateUniqueSlug(request.name(), null));
        category.setActive(request.active() != null ? request.active() : true);
        category.setSortOrder(request.sortOrder() != null ? request.sortOrder() : DEFAULT_SORT_ORDER);

        if (request.parentId() != null) {
            category.setParent(findCategoryById(request.parentId()));
        }

        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse update(Long id, CategoryUpdateRequest request) {
        Category category = findCategoryById(id);

        if (request.name() != null && !request.name().isBlank()) {
            category.setName(request.name());
            category.setSlug(generateUniqueSlug(request.name(), id));
        }

        if (request.parentId() != null) {
            validateParent(category.getId(), request.parentId());
            category.setParent(findCategoryById(request.parentId()));
        }

        if (request.active() != null) {
            category.setActive(request.active());
        }

        if (request.sortOrder() != null) {
            category.setSortOrder(request.sortOrder());
        }

        return toCategoryResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return toCategoryResponse(findCategoryById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .map(this::toCategoryResponse)
                .orElseThrow(() -> new BusinessException(CategoryErrorMessage.CATEGORY_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryTreeResponse> getTree() {
        return categoryRepository.findByParentIsNullAndActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(this::toCategoryTreeResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryFilterResponse> getFilters() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc()
                .stream()
                .map(this::toCategoryFilterResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }

    private Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(CategoryErrorMessage.CATEGORY_NOT_FOUND));
    }

    private void validateParent(Long categoryId, Long parentId) {
        if (Objects.equals(categoryId, parentId)) {
            throw new BusinessException(CategoryErrorMessage.CATEGORY_PARENT_CANNOT_BE_SELF);
        }
    }

    private String generateUniqueSlug(String name, Long currentCategoryId) {
        String baseSlug = SlugUtil.toSlug(name);
        String slug = baseSlug;
        int counter = 2;

        while (isSlugUsedByAnotherCategory(slug, currentCategoryId)) {
            slug = baseSlug + "-" + counter++;
        }

        return slug;
    }

    private boolean isSlugUsedByAnotherCategory(String slug, Long currentCategoryId) {
        return categoryRepository.findBySlug(slug)
                .map(category -> !Objects.equals(category.getId(), currentCategoryId))
                .orElse(false);
    }

    private CategoryResponse toCategoryResponse(Category category) {
        List<CategoryResponse> children = category.getChildren()
                .stream()
                .sorted(categoryComparator())
                .map(this::toCategoryResponse)
                .toList();

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getParent() != null ? category.getParent().getName() : null,
                category.getActive(),
                category.getSortOrder(),
                children,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    private CategoryTreeResponse toCategoryTreeResponse(Category category) {
        List<CategoryTreeResponse> children = category.getChildren()
                .stream()
                .filter(Category::getActive)
                .sorted(categoryComparator())
                .map(this::toCategoryTreeResponse)
                .toList();

        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getSortOrder(),
                children
        );
    }

    private CategoryFilterResponse toCategoryFilterResponse(Category category) {
        return new CategoryFilterResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getParent() != null ? category.getParent().getId() : null,
                0L
        );
    }

    private Comparator<Category> categoryComparator() {
        return Comparator
                .comparing(
                        Category::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo)
                )
                .thenComparing(Category::getName, String.CASE_INSENSITIVE_ORDER);
    }
}