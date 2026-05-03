package com.demir.ecommerce.productservice.integration.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import com.demir.ecommerce.productservice.dto.category.request.CategoryCreateRequest;
import com.demir.ecommerce.productservice.dto.category.request.CategoryUpdateRequest;
import com.demir.ecommerce.productservice.dto.category.response.CategoryFilterResponse;
import com.demir.ecommerce.productservice.dto.category.response.CategoryResponse;
import com.demir.ecommerce.productservice.dto.category.response.CategoryTreeResponse;
import com.demir.ecommerce.productservice.entity.Category;
import com.demir.ecommerce.productservice.messaging.StockEventPublisher;
import com.demir.ecommerce.productservice.repository.CategoryRepository;
import com.demir.ecommerce.productservice.repository.ProductRepository;
import com.demir.ecommerce.productservice.service.StorageService;
import com.demir.ecommerce.productservice.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "aws.s3.bucket-name=test-bucket",
        "aws.s3.region=eu-central-1",
        "spring.cloud.aws.credentials.access-key=test",
        "spring.cloud.aws.credentials.secret-key=test"
})
@ActiveProfiles("test")
@DisplayName("CategoryServiceImpl Integration Tests")
class CategoryServiceImplIntegrationTest {

    @Autowired
    private CategoryServiceImpl categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockitoBean
    private StorageService storageService;

    @MockitoBean
    private StockEventPublisher stockEventPublisher;

    @AfterEach
    void cleanup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private CategoryCreateRequest createRequest(String name) {
        return new CategoryCreateRequest(name, null, true, 1);
    }

    private Category savedCategory(String name, String slug) {
        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setActive(true);
        category.setSortOrder(1);
        return categoryRepository.save(category);
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Should persist category to database when user is admin")
        void create_adminUser_persistsCategoryToDatabase() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                CategoryResponse response = categoryService.create(createRequest("Electronics"));

                Optional<Category> fromDb = categoryRepository.findById(response.id());
                assertThat(fromDb).isPresent();
                assertThat(fromDb.get().getName()).isEqualTo("Electronics");
                assertThat(fromDb.get().getSlug()).isEqualTo("electronics");
                assertThat(fromDb.get().getActive()).isTrue();
            }
        }

        @Test
        @DisplayName("Should persist child category when parent exists")
        void create_withParent_persistsChildCategory() {
            Category parent = savedCategory("Main Category", "main-category");

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                CategoryCreateRequest request = new CategoryCreateRequest("Phones", parent.getId(), true, 2);
                CategoryResponse response = categoryService.create(request);

                Category fromDb = categoryRepository.findById(response.id()).orElseThrow();
                assertThat(fromDb.getParent().getId()).isEqualTo(parent.getId());
            }
        }

        @Test
        @DisplayName("Should create unique slug when category name already exists")
        void create_duplicateName_createsUniqueSlug() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                categoryService.create(createRequest("Electronics"));
                CategoryResponse second = categoryService.create(createRequest("Electronics"));

                assertThat(second.slug()).isEqualTo("electronics-2");
                assertThat(categoryRepository.findAll()).hasSize(2);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException and not persist when user is not admin")
        void create_notAdmin_doesNotPersist() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(false);

                assertThatThrownBy(() -> categoryService.create(createRequest("Electronics")))
                        .isInstanceOf(BusinessException.class);

                assertThat(categoryRepository.findAll()).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Should update category in database")
        void update_adminUser_updatesCategoryInDatabase() {
            Category category = savedCategory("Electronics", "electronics");

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                CategoryUpdateRequest request = new CategoryUpdateRequest("Updated Electronics", null, false, 5);
                CategoryResponse response = categoryService.update(category.getId(), request);

                Category fromDb = categoryRepository.findById(category.getId()).orElseThrow();
                assertThat(response.name()).isEqualTo("Updated Electronics");
                assertThat(fromDb.getSlug()).isEqualTo("updated-electronics");
                assertThat(fromDb.getActive()).isFalse();
                assertThat(fromDb.getSortOrder()).isEqualTo(5);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when category does not exist")
        void update_notFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                CategoryUpdateRequest request = new CategoryUpdateRequest("Updated", null, true, 1);

                assertThatThrownBy(() -> categoryService.update(999L, request))
                        .isInstanceOf(BusinessException.class);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when parent is same category")
        void update_parentSameAsCategory_throwsException() {
            Category category = savedCategory("Electronics", "electronics");

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                CategoryUpdateRequest request = new CategoryUpdateRequest(
                        "Electronics",
                        category.getId(),
                        true,
                        1
                );

                assertThatThrownBy(() -> categoryService.update(category.getId(), request))
                        .isInstanceOf(BusinessException.class);
            }
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("Should return category from database")
        void getById_existingCategory_returnsCategory() {
            Category category = savedCategory("Electronics", "electronics");

            CategoryResponse response = categoryService.getById(category.getId());

            assertThat(response.id()).isEqualTo(category.getId());
            assertThat(response.name()).isEqualTo("Electronics");
        }

        @Test
        @DisplayName("Should throw BusinessException when category does not exist")
        void getById_notFound_throwsException() {
            assertThatThrownBy(() -> categoryService.getById(999L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getBySlug()")
    class GetBySlug {

        @Test
        @DisplayName("Should return category by slug from database")
        void getBySlug_existingSlug_returnsCategory() {
            savedCategory("Electronics", "electronics");

            CategoryResponse response = categoryService.getBySlug("electronics");

            assertThat(response.slug()).isEqualTo("electronics");
        }

        @Test
        @DisplayName("Should throw BusinessException when slug does not exist")
        void getBySlug_notFound_throwsException() {
            assertThatThrownBy(() -> categoryService.getBySlug("missing"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("Should return all categories from database")
        void getAll_withCategories_returnsAllCategories() {
            savedCategory("Electronics", "electronics");
            savedCategory("Books", "books");

            List<CategoryResponse> response = categoryService.getAll();

            assertThat(response).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getTree()")
    class GetTree {

        @Test
        @DisplayName("Should return active root categories with active children")
        void getTree_withParentAndChild_returnsTree() {
            Category parent = savedCategory("Electronics", "electronics");

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);
                categoryService.create(new CategoryCreateRequest("Phones", parent.getId(), true, 2));
            }

            List<CategoryTreeResponse> response = categoryService.getTree();

            assertThat(response).hasSize(1);
            assertThat(response.get(0).children()).hasSize(1);
            assertThat(response.get(0).children().get(0).name()).isEqualTo("Phones");
        }
    }

    @Nested
    @DisplayName("getFilters()")
    class GetFilters {

        @Test
        @DisplayName("Should return active categories as filters")
        void getFilters_activeCategories_returnsFilters() {
            savedCategory("Electronics", "electronics");
            savedCategory("Books", "books");

            List<CategoryFilterResponse> response = categoryService.getFilters();

            assertThat(response).hasSize(2);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Should delete category from database")
        void delete_adminUser_deletesCategoryFromDatabase() {
            Category category = savedCategory("Electronics", "electronics");

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(true);

                categoryService.delete(category.getId());

                assertThat(categoryRepository.findById(category.getId())).isEmpty();
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not admin")
        void delete_notAdmin_throwsException() {
            Category category = savedCategory("Electronics", "electronics");

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(SecurityUtils::isAdmin).thenReturn(false);

                assertThatThrownBy(() -> categoryService.delete(category.getId()))
                        .isInstanceOf(BusinessException.class);

                assertThat(categoryRepository.findById(category.getId())).isPresent();
            }
        }
    }
}
