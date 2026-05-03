package com.demir.ecommerce.productservice.unit.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import com.demir.ecommerce.productservice.dto.category.request.CategoryCreateRequest;
import com.demir.ecommerce.productservice.dto.category.request.CategoryUpdateRequest;
import com.demir.ecommerce.productservice.dto.category.response.CategoryFilterResponse;
import com.demir.ecommerce.productservice.dto.category.response.CategoryResponse;
import com.demir.ecommerce.productservice.dto.category.response.CategoryTreeResponse;
import com.demir.ecommerce.productservice.entity.Category;
import com.demir.ecommerce.productservice.repository.CategoryRepository;
import com.demir.ecommerce.productservice.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Unit Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private static final Long CATEGORY_ID = 1L;
    private static final Long PARENT_ID = 2L;

    private Category category() {
        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setActive(true);
        category.setSortOrder(1);
        category.setChildren(new ArrayList<>());
        return category;
    }

    private Category parentCategory() {
        Category parent = new Category();
        parent.setId(PARENT_ID);
        parent.setName("Main Category");
        parent.setSlug("main-category");
        parent.setActive(true);
        parent.setSortOrder(1);
        parent.setChildren(new ArrayList<>());
        return parent;
    }

    private CategoryCreateRequest createRequest() {
        return new CategoryCreateRequest("Electronics", null, true, 1);
    }

    private CategoryUpdateRequest updateRequest() {
        return new CategoryUpdateRequest("Updated Electronics", null, false, 2);
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Should create category when user is admin")
        void create_adminUser_createsCategory() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.empty());
                when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                    Category c = invocation.getArgument(0);
                    c.setId(CATEGORY_ID);
                    return c;
                });

                CategoryResponse response = categoryService.create(createRequest());

                assertThat(response).isNotNull();
                assertThat(response.id()).isEqualTo(CATEGORY_ID);
                assertThat(response.name()).isEqualTo("Electronics");
                assertThat(response.slug()).isEqualTo("electronics");
                assertThat(response.active()).isTrue();
                verify(categoryRepository).save(any(Category.class));
            }
        }

        @Test
        @DisplayName("Should create category with parent when parentId exists")
        void create_withParent_createsChildCategory() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                CategoryCreateRequest request = new CategoryCreateRequest("Phones", PARENT_ID, true, 1);

                when(categoryRepository.findBySlug("phones")).thenReturn(Optional.empty());
                when(categoryRepository.findById(PARENT_ID)).thenReturn(Optional.of(parentCategory()));
                when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                    Category c = invocation.getArgument(0);
                    c.setId(CATEGORY_ID);
                    return c;
                });

                CategoryResponse response = categoryService.create(request);

                assertThat(response.name()).isEqualTo("Phones");
                assertThat(response.parentId()).isEqualTo(PARENT_ID);
            }
        }

        @Test
        @DisplayName("Should create unique slug when slug already exists")
        void create_duplicateSlug_createsUniqueSlug() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                Category existing = category();
                when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(existing));
                when(categoryRepository.findBySlug("electronics-2")).thenReturn(Optional.empty());
                when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                    Category c = invocation.getArgument(0);
                    c.setId(10L);
                    return c;
                });

                CategoryResponse response = categoryService.create(createRequest());

                assertThat(response.slug()).isEqualTo("electronics-2");
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not admin")
        void create_notAdmin_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(false);

                assertThatThrownBy(() -> categoryService.create(createRequest()))
                        .isInstanceOf(BusinessException.class);

                verifyNoInteractions(categoryRepository);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when parent category is not found")
        void create_parentNotFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                CategoryCreateRequest request = new CategoryCreateRequest("Phones", PARENT_ID, true, 1);

                when(categoryRepository.findBySlug("phones")).thenReturn(Optional.empty());
                when(categoryRepository.findById(PARENT_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> categoryService.create(request))
                        .isInstanceOf(BusinessException.class);

                verify(categoryRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Should update category when user is admin")
        void update_adminUser_updatesCategory() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                Category category = category();

                when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
                when(categoryRepository.findBySlug("updated-electronics")).thenReturn(Optional.empty());
                when(categoryRepository.save(category)).thenReturn(category);

                CategoryResponse response = categoryService.update(CATEGORY_ID, updateRequest());

                assertThat(response.name()).isEqualTo("Updated Electronics");
                assertThat(response.slug()).isEqualTo("updated-electronics");
                assertThat(response.active()).isFalse();
                assertThat(response.sortOrder()).isEqualTo(2);
                verify(categoryRepository).save(category);
            }
        }

        @Test
        @DisplayName("Should update parent when parentId exists")
        void update_withParent_updatesParent() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                Category category = category();
                Category parent = parentCategory();
                CategoryUpdateRequest request = new CategoryUpdateRequest("Electronics", PARENT_ID, true, 1);

                when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
                when(categoryRepository.findById(PARENT_ID)).thenReturn(Optional.of(parent));
                when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(category));
                when(categoryRepository.save(category)).thenReturn(category);

                CategoryResponse response = categoryService.update(CATEGORY_ID, request);

                assertThat(response.parentId()).isEqualTo(PARENT_ID);
                assertThat(response.parentName()).isEqualTo("Main Category");
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when category is not found")
        void update_categoryNotFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);
                when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> categoryService.update(CATEGORY_ID, updateRequest()))
                        .isInstanceOf(BusinessException.class);

                verify(categoryRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when parent is same category")
        void update_parentSameAsCategory_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                CategoryUpdateRequest request = new CategoryUpdateRequest("Electronics", CATEGORY_ID, true, 1);
                when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));
                when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(category()));

                assertThatThrownBy(() -> categoryService.update(CATEGORY_ID, request))
                        .isInstanceOf(BusinessException.class);

                verify(categoryRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not admin")
        void update_notAdmin_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(false);

                assertThatThrownBy(() -> categoryService.update(CATEGORY_ID, updateRequest()))
                        .isInstanceOf(BusinessException.class);

                verifyNoInteractions(categoryRepository);
            }
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("Should return category when category exists")
        void getById_existingCategory_returnsResponse() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));

            CategoryResponse response = categoryService.getById(CATEGORY_ID);

            assertThat(response.id()).isEqualTo(CATEGORY_ID);
            assertThat(response.name()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("Should throw BusinessException when category is not found")
        void getById_categoryNotFound_throwsException() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getById(CATEGORY_ID))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getBySlug()")
    class GetBySlug {

        @Test
        @DisplayName("Should return category when slug exists")
        void getBySlug_existingSlug_returnsResponse() {
            when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(category()));

            CategoryResponse response = categoryService.getBySlug("electronics");

            assertThat(response.slug()).isEqualTo("electronics");
        }

        @Test
        @DisplayName("Should throw BusinessException when slug is not found")
        void getBySlug_notFound_throwsException() {
            when(categoryRepository.findBySlug("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getBySlug("missing"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("Should return all categories")
        void getAll_withCategories_returnsCategoryList() {
            when(categoryRepository.findAll()).thenReturn(List.of(category(), parentCategory()));

            List<CategoryResponse> response = categoryService.getAll();

            assertThat(response).hasSize(2);
        }

        @Test
        @DisplayName("Should return empty list when no categories exist")
        void getAll_noCategories_returnsEmptyList() {
            when(categoryRepository.findAll()).thenReturn(List.of());

            List<CategoryResponse> response = categoryService.getAll();

            assertThat(response).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTree()")
    class GetTree {

        @Test
        @DisplayName("Should return active root categories as tree")
        void getTree_activeRootCategories_returnsTree() {
            Category parent = parentCategory();
            Category child = category();
            child.setParent(parent);
            parent.setChildren(new ArrayList<>(List.of(child)));

            when(categoryRepository.findByParentIsNullAndActiveTrueOrderBySortOrderAscNameAsc())
                    .thenReturn(List.of(parent));

            List<CategoryTreeResponse> response = categoryService.getTree();

            assertThat(response).hasSize(1);
            assertThat(response.get(0).children()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getFilters()")
    class GetFilters {

        @Test
        @DisplayName("Should return active categories as filters")
        void getFilters_activeCategories_returnsFilters() {
            when(categoryRepository.findByActiveTrueOrderBySortOrderAscNameAsc())
                    .thenReturn(List.of(category()));

            List<CategoryFilterResponse> response = categoryService.getFilters();

            assertThat(response).hasSize(1);
            assertThat(response.get(0).id()).isEqualTo(CATEGORY_ID);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Should delete category when user is admin")
        void delete_adminUser_deletesCategory() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);
                Category category = category();

                when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category));

                categoryService.delete(CATEGORY_ID);

                verify(categoryRepository).delete(category);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when category is not found")
        void delete_categoryNotFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);
                when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> categoryService.delete(CATEGORY_ID))
                        .isInstanceOf(BusinessException.class);

                verify(categoryRepository, never()).delete(any(Category.class));
            }
        }
    }
}
