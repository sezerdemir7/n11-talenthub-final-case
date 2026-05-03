package com.demir.ecommerce.productservice.unit.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.productservice.service.impl.StorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StorageServiceImpl Unit Tests")
class StorageServiceImplTest {

    @Mock
    private S3Client s3Client;

    @InjectMocks
    private StorageServiceImpl storageService;

    private static final Long PRODUCT_ID = 10L;
    private static final String BUCKET_NAME = "test-bucket";
    private static final String REGION = "eu-central-1";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(storageService, "region", REGION);
    }

    private MockMultipartFile image(String filename, String contentType) {
        return new MockMultipartFile(
                "image",
                filename,
                contentType,
                "test-image".getBytes()
        );
    }

    @Nested
    @DisplayName("uploadProductImage()")
    class UploadProductImage {

        @Test
        @DisplayName("Should upload image and return file url")
        void uploadProductImage_validImage_uploadsAndReturnsUrl() {
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            String result = storageService.uploadProductImage(
                    image("product.jpg", "image/jpeg"),
                    PRODUCT_ID
            );

            assertThat(result).startsWith("https://test-bucket.s3.eu-central-1.amazonaws.com/product-images/10/");
            assertThat(result).endsWith(".jpg");
            verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("Should upload png image")
        void uploadProductImage_pngImage_uploadsImage() {
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            String result = storageService.uploadProductImage(
                    image("product.png", "image/png"),
                    PRODUCT_ID
            );

            assertThat(result).endsWith(".png");
        }

        @Test
        @DisplayName("Should upload webp image")
        void uploadProductImage_webpImage_uploadsImage() {
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            String result = storageService.uploadProductImage(
                    image("product.webp", "image/webp"),
                    PRODUCT_ID
            );

            assertThat(result).endsWith(".webp");
        }

        @Test
        @DisplayName("Should throw BusinessException when file is null")
        void uploadProductImage_nullFile_throwsException() {
            assertThatThrownBy(() -> storageService.uploadProductImage(null, PRODUCT_ID))
                    .isInstanceOf(BusinessException.class);

            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("Should throw BusinessException when file is empty")
        void uploadProductImage_emptyFile_throwsException() {
            MockMultipartFile empty = new MockMultipartFile(
                    "image",
                    "empty.jpg",
                    "image/jpeg",
                    new byte[0]
            );

            assertThatThrownBy(() -> storageService.uploadProductImage(empty, PRODUCT_ID))
                    .isInstanceOf(BusinessException.class);

            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("Should throw BusinessException when file type is invalid")
        void uploadProductImage_invalidFileType_throwsException() {
            MockMultipartFile file = image("document.pdf", "application/pdf");

            assertThatThrownBy(() -> storageService.uploadProductImage(file, PRODUCT_ID))
                    .isInstanceOf(BusinessException.class);

            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("Should throw BusinessException when filename has no extension")
        void uploadProductImage_filenameWithoutExtension_throwsException() {
            MockMultipartFile file = image("product", "image/jpeg");

            assertThatThrownBy(() -> storageService.uploadProductImage(file, PRODUCT_ID))
                    .isInstanceOf(BusinessException.class);

            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("Should throw BusinessException when S3 upload fails")
        void uploadProductImage_s3Fails_throwsException() {
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(S3Exception.builder().message("S3 failed").build());

            assertThatThrownBy(() ->
                    storageService.uploadProductImage(image("product.jpg", "image/jpeg"), PRODUCT_ID)
            ).isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("deleteFile()")
    class DeleteFile {

        @Test
        @DisplayName("Should delete file from S3")
        void deleteFile_validUrl_deletesFile() {
            when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .thenReturn(DeleteObjectResponse.builder().build());

            storageService.deleteFile(
                    "https://test-bucket.s3.eu-central-1.amazonaws.com/product-images/10/product.jpg"
            );

            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("Should do nothing when file url is null or blank")
        void deleteFile_nullOrBlankUrl_doesNothing() {
            storageService.deleteFile(null);
            storageService.deleteFile("");
            storageService.deleteFile("   ");

            verifyNoInteractions(s3Client);
        }

        @Test
        @DisplayName("Should throw BusinessException when S3 delete fails")
        void deleteFile_s3Fails_throwsException() {
            when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                    .thenThrow(S3Exception.builder().message("S3 failed").build());

            assertThatThrownBy(() ->
                    storageService.deleteFile("https://test-bucket.s3.eu-central-1.amazonaws.com/product-images/10/product.jpg")
            ).isInstanceOf(BusinessException.class);
        }
    }
}
