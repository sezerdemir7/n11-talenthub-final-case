package com.demir.ecommerce.productservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    String uploadProductImage(MultipartFile file, Long productId);

    void deleteFile(String fileUrl);
}