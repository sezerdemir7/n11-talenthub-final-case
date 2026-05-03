package com.demir.ecommerce.productservice.integration.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import com.demir.ecommerce.productservice.dto.product.*;
import com.demir.ecommerce.productservice.dto.productdetail.ProductDetailCreateRequest;
import com.demir.ecommerce.productservice.dto.productdetail.ProductDetailUpdateRequest;
import com.demir.ecommerce.productservice.entity.Category;
import com.demir.ecommerce.productservice.entity.Product;
import com.demir.ecommerce.productservice.messaging.StockEventPublisher;
import com.demir.ecommerce.productservice.repository.CategoryRepository;
import com.demir.ecommerce.productservice.repository.ProductRepository;
import com.demir.ecommerce.productservice.service.StorageService;
import com.demir.ecommerce.productservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ProductServiceImpl Integration Tests")
class ProductServiceImplIntegrationTest {

    @Autowired
    private ProductServiceImpl productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @MockitoBean
    private StorageService storageService;

    @MockitoBean
    private StockEventPublisher stockEventPublisher;

    private static final Long SELLER_ID = 5L;

    @AfterEach
    void cleanup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Category savedCategory() {
        Category category = new Category();
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setActive(true);
        category.setSortOrder(1);
        return categoryRepository.save(category);
    }

    private ProductCreateRequest createRequest(Long categoryId) {
        return new ProductCreateRequest(
                "Test Product",
                new BigDecimal("99.90"),
                10,
                true,
                categoryId,
                new ProductDetailCreateRequest(
                        "Short description",
                        "Long description",
                        "Brand",
                        "Model",
                        "24 Ay",
                        "Specs"
                )
        );
    }

    private ProductUpdateRequest updateRequest(Long categoryId) {
        return new ProductUpdateRequest(
                "Updated Product",
                new BigDecimal("149.90"),
                20,
                false,
                categoryId,
                new ProductDetailUpdateRequest(
                        "Updated short",
                        "Updated long",
                        "Updated Brand",
                        "Updated Model",
                        "12 Ay",
                        "Updated specs"
                )
        );
    }

    private Product savedProduct() {
        Category category = savedCategory();

        Product product = new Product();
        product.setSellerId(SELLER_ID);
        product.setName("Test Product");
        product.setSlug("test-product");
        product.setPrice(new BigDecimal("99.90"));
        product.setStock(10);
        product.setActive(true);
        product.setSuspendedBySellerStatus(false);
        product.setCategory(category);

        return productRepository.save(product);
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Should persist product to database when user is seller")
        void create_sellerUser_persistsProductToDatabase() {
            Category category = savedCategory();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.hasRole("SELLER")).thenReturn(true);
                sec.when(SecurityUtils::getUserId).thenReturn(SELLER_ID);

                ProductResponse response = productService.create(createRequest(category.getId()), null);

                Optional<Product> fromDb = productRepository.findById(response.id());
                assertThat(fromDb).isPresent();
                assertThat(fromDb.get().getSellerId()).isEqualTo(SELLER_ID);
                assertThat(fromDb.get().getName()).isEqualTo("Test Product");
                assertThat(fromDb.get().getSlug()).isEqualTo("test-product");
                assertThat(fromDb.get().getDetail()).isNotNull();
            }
        }

        @Test
        @DisplayName("Should persist image url when image is uploaded")
        void create_withImage_persistsImageUrl() {
            Category category = savedCategory();
            MockMultipartFile image = new MockMultipartFile(
                    "image",
                    "product.jpg",
                    "image/jpeg",
                    "test".getBytes()
            );

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.hasRole("SELLER")).thenReturn(true);
                sec.when(SecurityUtils::getUserId).thenReturn(SELLER_ID);
                when(storageService.uploadProductImage(any(), anyLong()))
                        .thenReturn("http://img.test/product.jpg");

                ProductResponse response = productService.create(createRequest(category.getId()), image);

                Product fromDb = productRepository.findById(response.id()).orElseThrow();
                assertThat(fromDb.getImageUrl()).isEqualTo("http://img.test/product.jpg");
            }
        }

        @Test
        @DisplayName("Should throw BusinessException and not persist when user is not seller or admin")
        void create_notSellerOrAdmin_doesNotPersist() {
            Category category = savedCategory();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.hasRole("SELLER")).thenReturn(false);
                sec.when(SecurityUtils::isAdmin).thenReturn(false);

                assertThatThrownBy(() -> productService.create(createRequest(category.getId()), null))
                        .isInstanceOf(BusinessException.class);

                assertThat(productRepository.findAll()).isEmpty();
            }
        }

        @Test
        @DisplayName("Should create unique slug when slug already exists")
        void create_duplicateName_createsUniqueSlug() {
            Category category = savedCategory();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.hasRole("SELLER")).thenReturn(true);
                sec.when(SecurityUtils::getUserId).thenReturn(SELLER_ID);

                productService.create(createRequest(category.getId()), null);
                ProductResponse second = productService.create(createRequest(category.getId()), null);

                assertThat(second.slug()).isEqualTo("test-product-2");
                assertThat(productRepository.findAll()).hasSize(2);
            }
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Should update product in database when owner requests")
        void update_ownerRequest_updatesProductInDatabase() {
            Product product = savedProduct();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(SELLER_ID)).thenReturn(true);

                ProductResponse response = productService.update(
                        product.getId(),
                        updateRequest(product.getCategory().getId()),
                        null
                );

                Product fromDb = productRepository.findById(product.getId()).orElseThrow();
                assertThat(response.name()).isEqualTo("Updated Product");
                assertThat(fromDb.getPrice()).isEqualByComparingTo("149.90");
                assertThat(fromDb.getStock()).isEqualTo(20);
                assertThat(fromDb.getActive()).isFalse();
                assertThat(fromDb.getDetail().getBrand()).isEqualTo("Updated Brand");
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not owner or admin")
        void update_notOwner_throwsAccessDenied() {
            Product product = savedProduct();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(SELLER_ID)).thenReturn(false);

                assertThatThrownBy(() ->
                        productService.update(product.getId(), updateRequest(product.getCategory().getId()), null)
                ).isInstanceOf(BusinessException.class);

                Product fromDb = productRepository.findById(product.getId()).orElseThrow();
                assertThat(fromDb.getName()).isEqualTo("Test Product");
            }
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("Should return product from database")
        void getById_existingProduct_returnsProduct() {
            Product product = savedProduct();

            ProductResponse response = productService.getById(product.getId());

            assertThat(response.id()).isEqualTo(product.getId());
            assertThat(response.name()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("Should throw BusinessException when product does not exist")
        void getById_notFound_throwsException() {
            assertThatThrownBy(() -> productService.getById(999L))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getBySlug()")
    class GetBySlug {

        @Test
        @DisplayName("Should return product by slug from database")
        void getBySlug_existingSlug_returnsProduct() {
            savedProduct();

            ProductResponse response = productService.getBySlug("test-product");

            assertThat(response.slug()).isEqualTo("test-product");
        }

        @Test
        @DisplayName("Should throw BusinessException when slug does not exist")
        void getBySlug_notFound_throwsException() {
            assertThatThrownBy(() -> productService.getBySlug("missing"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("search()")
    class Search {

        @Test
        @DisplayName("Should return paginated products from database")
        void search_withProducts_returnsPage() {
            savedProduct();

            Page<ProductListResponse> response = productService.search(
                    new ProductFilterRequest(null, null, null, null, null, true, null, null),
                    PageRequest.of(0, 10)
            );

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Should delete product from database when owner requests")
        void delete_ownerRequest_deletesProductFromDatabase() {
            Product product = savedProduct();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(SELLER_ID)).thenReturn(true);

                productService.delete(product.getId());

                assertThat(productRepository.findById(product.getId())).isEmpty();
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not owner or admin")
        void delete_notOwner_throwsAccessDenied() {
            Product product = savedProduct();

            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(SELLER_ID)).thenReturn(false);

                assertThatThrownBy(() -> productService.delete(product.getId()))
                        .isInstanceOf(BusinessException.class);

                assertThat(productRepository.findById(product.getId())).isPresent();
            }
        }
    }

    @Nested
    @DisplayName("getByIds()")
    class GetByIds {

        @Test
        @DisplayName("Should return internal responses for persisted products")
        void getByIds_existingProducts_returnsInternalResponses() {
            Product product = savedProduct();

            List<ProductInternalResponse> response = productService.getByIds(List.of(product.getId()));

            assertThat(response).hasSize(1);
            assertThat(response.get(0).id()).isEqualTo(product.getId());
            assertThat(response.get(0).stock()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should return empty list when ids is empty")
        void getByIds_emptyIds_returnsEmptyList() {
            assertThat(productService.getByIds(List.of())).isEmpty();
        }
    }

    @Nested
    @DisplayName("deactivateProductsBySellerId()")
    class DeactivateProductsBySellerId {

        @Test
        @DisplayName("Should deactivate active products for seller")
        void deactivateProductsBySellerId_activeProducts_deactivatesProducts() {
            Product product = savedProduct();

            productService.deactivateProductsBySellerId(SELLER_ID);

            Product fromDb = productRepository.findById(product.getId()).orElseThrow();
            assertThat(fromDb.getActive()).isFalse();
            assertThat(fromDb.getSuspendedBySellerStatus()).isTrue();
        }
    }

    @Nested
    @DisplayName("activateProductsBySellerId()")
    class ActivateProductsBySellerId {

        @Test
        @DisplayName("Should activate suspended products for seller")
        void activateProductsBySellerId_suspendedProducts_activatesProducts() {
            Product product = savedProduct();
            productService.deactivateProductsBySellerId(SELLER_ID);

            productService.activateProductsBySellerId(SELLER_ID);

            Product fromDb = productRepository.findById(product.getId()).orElseThrow();
            assertThat(fromDb.getActive()).isTrue();
            assertThat(fromDb.getSuspendedBySellerStatus()).isFalse();
        }
    }
}
