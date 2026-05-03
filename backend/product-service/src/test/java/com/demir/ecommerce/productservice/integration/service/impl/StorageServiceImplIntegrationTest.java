package com.demir.ecommerce.productservice.integration.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.productservice.service.impl.StorageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(
        classes = StorageServiceImpl.class,
        properties = {
                "aws.s3.bucket-name=test-bucket",
                "aws.s3.region=eu-central-1"
        }
)
@ActiveProfiles("test")
@DisplayName("StorageServiceImpl Integration Tests")
class StorageServiceImplIntegrationTest {

    @Autowired
    private StorageServiceImpl storageService;

    @MockitoBean
    private S3Client s3Client;

    private static final Long PRODUCT_ID = 10L;

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
        @DisplayName("Should upload valid image and return generated S3 url")
        void uploadProductImage_validImage_returnsGeneratedUrl() {
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(PutObjectResponse.builder().build());

            String response = storageService.uploadProductImage(
                    image("product.jpg", "image/jpeg"),
                    PRODUCT_ID
            );

            assertThat(response).startsWith("https://test-bucket.s3.eu-central-1.amazonaws.com/product-images/10/");
            assertThat(response).endsWith(".jpg");
            verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("Should throw BusinessException and not call S3 when file type is invalid")
        void uploadProductImage_invalidType_doesNotCallS3() {
            assertThatThrownBy(() ->
                    storageService.uploadProductImage(image("file.pdf", "application/pdf"), PRODUCT_ID)
            ).isInstanceOf(BusinessException.class);

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
        @DisplayName("Should throw BusinessException when S3 upload fails")
        void uploadProductImage_s3Fails_throwsException() {
            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenThrow(S3Exception.builder().message("S3 failed").build());

            assertThatThrownBy(() ->
                    storageService.uploadProductImage(image("product.png", "image/png"), PRODUCT_ID)
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
        @DisplayName("Should do nothing when file url is blank")
        void deleteFile_blankUrl_doesNothing() {
            storageService.deleteFile(null);
            storageService.deleteFile("");

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
