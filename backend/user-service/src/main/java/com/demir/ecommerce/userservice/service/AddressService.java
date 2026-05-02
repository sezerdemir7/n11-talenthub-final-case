package com.demir.ecommerce.userservice.service;

import com.demir.ecommerce.userservice.dto.address.request.AddressRequest;
import com.demir.ecommerce.userservice.dto.address.response.AddressInternalResponse;
import com.demir.ecommerce.userservice.dto.address.response.AddressResponse;

import java.util.List;

public interface AddressService {

    AddressResponse create(AddressRequest request);

    List<AddressResponse> getMyAddresses();

    AddressResponse update(Long addressId, AddressRequest request);

    void delete(Long addressId);

    AddressResponse setDefault(Long addressId);

    AddressInternalResponse getInternalAddress(Long userId, Long addressId);
}