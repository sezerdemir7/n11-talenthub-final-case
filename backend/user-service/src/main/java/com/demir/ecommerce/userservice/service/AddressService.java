package com.demir.ecommerce.userservice.service;

import com.demir.ecommerce.userservice.dto.address.request.AddressRequest;
import com.demir.ecommerce.userservice.dto.address.response.AddressInternalResponse;
import com.demir.ecommerce.userservice.dto.address.response.AddressResponse;

import java.util.List;

public interface AddressService {

    AddressResponse create(Long userId, AddressRequest request);

    List<AddressResponse> getMyAddresses(Long userId);

    AddressResponse update(Long userId, Long addressId, AddressRequest request);

    void delete(Long userId, Long addressId);

    AddressResponse setDefault(Long userId, Long addressId);

    AddressInternalResponse getInternalAddress(Long userId, Long addressId);

}
