package com.demir.ecommerce.productservice.unit.service.impl;


import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.commonlib.security.SecurityUtils;
import com.demir.ecommerce.productservice.dto.product.*;
import com.demir.ecommerce.productservice.dto.productdetail.ProductDetailCreateRequest;
import com.demir.ecommerce.productservice.dto.productdetail.ProductDetailUpdateRequest;
import com.demir.ecommerce.productservice.entity.Category;
import com.demir.ecommerce.productservice.entity.Product;
import com.demir.ecommerce.productservice.entity.ProductDetail;
import com.demir.ecommerce.productservice.repository.CategoryRepository;
import com.demir.ecommerce.productservice.repository.ProductRepository;
import com.demir.ecommerce.productservice.service.StorageService;
import com.demir.ecommerce.productservice.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Unit Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private MultipartFile image;

    @InjectMocks
    private ProductServiceImpl productService;

    private static final Long PRODUCT_ID = 10L;
    private static final Long CATEGORY_ID = 1L;
    private static final Long SELLER_ID = 5L;

    private Category category() {
        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName("Electronics");
        category.setSlug("electronics");
        category.setActive(true);
        category.setSortOrder(1);
        return category;
    }

    private Product product() {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setSellerId(SELLER_ID);
        product.setName("Test Product");
        product.setSlug("test-product");
        product.setPrice(new BigDecimal("99.90"));
        product.setStock(10);
        product.setActive(true);
        product.setSuspendedBySellerStatus(false);
        product.setCategory(category());

        ProductDetail detail = new ProductDetail();
        detail.setId(100L);
        detail.setShortDescription("Short description");
        detail.setLongDescription("Long description");
        detail.setBrand("Brand");
        detail.setModel("Model");
        detail.setWarrantyPeriod("24 Ay");
        detail.setSpecifications("Specs");
        detail.setProduct(product);
        product.setDetail(detail);

        return product;
    }

    private ProductCreateRequest createRequest() {
        return new ProductCreateRequest(
                "Test Product",
                new BigDecimal("99.90"),
                10,
                true,
                CATEGORY_ID,
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

    private ProductUpdateRequest updateRequest() {
        return new ProductUpdateRequest(
                "Updated Product",
                new BigDecimal("149.90"),
                20,
                true,
                CATEGORY_ID,
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

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("Should create product when user is seller")
        void create_sellerUser_createsProduct() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.hasRole("SELLER")).thenReturn(true);
                sec.when(SecurityUtils::getUserId).thenReturn(SELLER_ID);

                when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));
                when(productRepository.findBySlug("test-product")).thenReturn(Optional.empty());
                when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                    Product p = invocation.getArgument(0);
                    p.setId(PRODUCT_ID);
                    return p;
                });

                ProductResponse response = productService.create(createRequest(), null);

                assertThat(response).isNotNull();
                assertThat(response.id()).isEqualTo(PRODUCT_ID);
                assertThat(response.sellerId()).isEqualTo(SELLER_ID);
                assertThat(response.name()).isEqualTo("Test Product");
                assertThat(response.slug()).isEqualTo("test-product");
                assertThat(response.detail()).isNotNull();
                verify(productRepository).save(any(Product.class));
                verifyNoInteractions(storageService);
            }
        }

        @Test
        @DisplayName("Should upload image when image exists")
        void create_withImage_uploadsImage() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.hasRole("SELLER")).thenReturn(true);
                sec.when(SecurityUtils::getUserId).thenReturn(SELLER_ID);

                when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));
                when(productRepository.findBySlug("test-product")).thenReturn(Optional.empty());
                when(image.isEmpty()).thenReturn(false);
                when(storageService.uploadProductImage(image, PRODUCT_ID)).thenReturn("http://img.test/product.jpg");
                when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                    Product p = invocation.getArgument(0);
                    p.setId(PRODUCT_ID);
                    return p;
                });

                ProductResponse response = productService.create(createRequest(), image);

                assertThat(response.imageUrl()).isEqualTo("http://img.test/product.jpg");
                verify(storageService).uploadProductImage(image, PRODUCT_ID);
                verify(productRepository, times(2)).save(any(Product.class));
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not seller or admin")
        void create_notSellerOrAdmin_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.hasRole("SELLER")).thenReturn(false);
                sec.when(SecurityUtils::isAdmin).thenReturn(false);

                assertThatThrownBy(() -> productService.create(createRequest(), null))
                        .isInstanceOf(BusinessException.class);

                verifyNoInteractions(categoryRepository, productRepository, storageService);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when category is not found")
        void create_categoryNotFound_throwsException() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.hasRole("SELLER")).thenReturn(true);
                sec.when(SecurityUtils::getUserId).thenReturn(SELLER_ID);
                when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

                assertThatThrownBy(() -> productService.create(createRequest(), null))
                        .isInstanceOf(BusinessException.class);

                verify(productRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("Should update product when owner requests")
        void update_ownerRequest_updatesProduct() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(SELLER_ID)).thenReturn(true);

                Product product = product();
                when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
                when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));
                when(productRepository.findBySlug("updated-product")).thenReturn(Optional.empty());
                when(productRepository.save(product)).thenReturn(product);

                ProductResponse response = productService.update(PRODUCT_ID, updateRequest(), null);

                assertThat(response.name()).isEqualTo("Updated Product");
                assertThat(response.price()).isEqualByComparingTo("149.90");
                assertThat(response.stock()).isEqualTo(20);
                assertThat(response.detail().brand()).isEqualTo("Updated Brand");
                verify(productRepository).save(product);
            }
        }

        @Test
        @DisplayName("Should delete old image and upload new image")
        void update_withImage_replacesImage() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(SELLER_ID)).thenReturn(true);

                Product product = product();
                product.setImageUrl("http://img.test/old.jpg");

                when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
                when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));
                when(productRepository.findBySlug("updated-product")).thenReturn(Optional.empty());
                when(image.isEmpty()).thenReturn(false);
                when(storageService.uploadProductImage(image, PRODUCT_ID)).thenReturn("http://img.test/new.jpg");
                when(productRepository.save(product)).thenReturn(product);

                ProductResponse response = productService.update(PRODUCT_ID, updateRequest(), image);

                assertThat(response.imageUrl()).isEqualTo("http://img.test/new.jpg");
                verify(storageService).deleteFile("http://img.test/old.jpg");
                verify(storageService).uploadProductImage(image, PRODUCT_ID);
            }
        }

        @Test
        @DisplayName("Should throw BusinessException when product is not found")
        void update_productNotFound_throwsException() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(PRODUCT_ID, updateRequest(), null))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not owner or admin")
        void update_notOwner_throwsAccessDenied() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(SELLER_ID)).thenReturn(false);
                when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));

                assertThatThrownBy(() -> productService.update(PRODUCT_ID, updateRequest(), null))
                        .isInstanceOf(BusinessException.class);

                verify(productRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("Should return product response when product exists")
        void getById_existingProduct_returnsResponse() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));

            ProductResponse response = productService.getById(PRODUCT_ID);

            assertThat(response.id()).isEqualTo(PRODUCT_ID);
            assertThat(response.name()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("Should throw BusinessException when product is not found")
        void getById_productNotFound_throwsException() {
            when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getById(PRODUCT_ID))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("getBySlug()")
    class GetBySlug {

        @Test
        @DisplayName("Should return product response when slug exists")
        void getBySlug_existingSlug_returnsResponse() {
            when(productRepository.findBySlug("test-product")).thenReturn(Optional.of(product()));

            ProductResponse response = productService.getBySlug("test-product");

            assertThat(response.slug()).isEqualTo("test-product");
        }

        @Test
        @DisplayName("Should throw BusinessException when slug is not found")
        void getBySlug_notFound_throwsException() {
            when(productRepository.findBySlug("missing")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getBySlug("missing"))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("search()")
    class Search {

        @Test
        @DisplayName("Should return paginated product list")
        void search_withProducts_returnsPage() {
            Page<Product> page = new PageImpl<>(List.of(product()));

            JpaSpecificationExecutor<Product> specExecutor =
                    (JpaSpecificationExecutor<Product>) productRepository;

            when(specExecutor.findAll(
                    ArgumentMatchers.<Specification<Product>>any(),
                    any(Pageable.class)
            )).thenReturn(page);

            Page<ProductListResponse> response = productService.search(
                    new ProductFilterRequest(null, null, null, null, null, true, null, null),
                    PageRequest.of(0, 10)
            );

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().get(0).id()).isEqualTo(PRODUCT_ID);
        }
    }


    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Should delete product when owner requests")
        void delete_ownerRequest_deletesProduct() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(SELLER_ID)).thenReturn(true);
                Product product = product();
                product.setImageUrl("http://img.test/product.jpg");
                when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

                productService.delete(PRODUCT_ID);

                verify(storageService).deleteFile("http://img.test/product.jpg");
                verify(productRepository).delete((Product) product);

            }
        }

        @Test
        @DisplayName("Should throw BusinessException when user is not owner or admin")
        void delete_notOwner_throwsAccessDenied() {
            try (MockedStatic<SecurityUtils> sec = mockStatic(SecurityUtils.class)) {
                sec.when(() -> SecurityUtils.isOwnerOrAdmin(SELLER_ID)).thenReturn(false);
                when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product()));

                assertThatThrownBy(() -> productService.delete(PRODUCT_ID))
                        .isInstanceOf(BusinessException.class);

                verify(productRepository, never()).delete(any(Product.class));

            }
        }
    }

    @Nested
    @DisplayName("getByIds()")
    class GetByIds {

        @Test
        @DisplayName("Should return internal responses for given ids")
        void getByIds_existingProducts_returnsInternalResponses() {
            when(productRepository.findAllByIdIn(List.of(PRODUCT_ID))).thenReturn(List.of(product()));

            List<ProductInternalResponse> response = productService.getByIds(List.of(PRODUCT_ID));

            assertThat(response).hasSize(1);
            assertThat(response.get(0).id()).isEqualTo(PRODUCT_ID);
            assertThat(response.get(0).active()).isTrue();
        }

        @Test
        @DisplayName("Should return empty list when ids is null or empty")
        void getByIds_emptyIds_returnsEmptyList() {
            assertThat(productService.getByIds(null)).isEmpty();
            assertThat(productService.getByIds(List.of())).isEmpty();

            verifyNoInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("deactivateProductsBySellerId()")
    class DeactivateProductsBySellerId {

        @Test
        @DisplayName("Should deactivate active seller products")
        void deactivateProductsBySellerId_activeProducts_deactivatesProducts() {
            Product product = product();
            when(productRepository.findBySellerIdAndActiveTrue(SELLER_ID)).thenReturn(List.of(product));

            productService.deactivateProductsBySellerId(SELLER_ID);

            assertThat(product.getActive()).isFalse();
            assertThat(product.getSuspendedBySellerStatus()).isTrue();
            verify(productRepository).saveAll(List.of(product));
        }
    }

    @Nested
    @DisplayName("activateProductsBySellerId()")
    class ActivateProductsBySellerId {

        @Test
        @DisplayName("Should activate suspended seller products")
        void activateProductsBySellerId_suspendedProducts_activatesProducts() {
            Product product = product();
            product.setActive(false);
            product.setSuspendedBySellerStatus(true);
            when(productRepository.findBySellerIdAndSuspendedBySellerStatusTrue(SELLER_ID)).thenReturn(List.of(product));

            productService.activateProductsBySellerId(SELLER_ID);

            assertThat(product.getActive()).isTrue();
            assertThat(product.getSuspendedBySellerStatus()).isFalse();
            verify(productRepository).saveAll(List.of(product));
        }
    }
}
