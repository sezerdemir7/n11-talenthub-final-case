package com.demir.ecommerce.userservice.service.impl;

import com.demir.ecommerce.commonlib.excepption.BusinessException;
import com.demir.ecommerce.userservice.dto.address.request.AddressRequest;
import com.demir.ecommerce.userservice.dto.address.response.AddressInternalResponse;
import com.demir.ecommerce.userservice.dto.address.response.AddressResponse;
import com.demir.ecommerce.userservice.entity.Address;
import com.demir.ecommerce.userservice.entity.User;
import com.demir.ecommerce.userservice.exception.message.UserErrorMessage;
import com.demir.ecommerce.userservice.repository.AddressRepository;
import com.demir.ecommerce.userservice.repository.UserRepository;
import com.demir.ecommerce.userservice.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressServiceImpl(AddressRepository addressRepository,
                              UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AddressResponse create(Long userId, AddressRequest request) {

        User user = findUserById(userId);

        Address address = new Address();
        address.setUser(user);
        address.setTitle(request.title());
        address.setCity(request.city());
        address.setDistrict(request.district());
        address.setFullAddress(request.fullAddress());
        address.setPostalCode(request.postalCode());

        boolean shouldBeDefault = Boolean.TRUE.equals(request.isDefault())
                || addressRepository.findByUserIdAndIsDefaultTrue(userId).isEmpty();

        address.setIsDefault(shouldBeDefault);

        if (shouldBeDefault) {
            clearDefaultAddress(userId);
        }

        Address savedAddress = addressRepository.save(address);

        return mapToResponse(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getMyAddresses(Long userId) {
        return addressRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse update(Long userId, Long addressId, AddressRequest request) {

        Address address = findAddressByIdAndUserId(addressId, userId);

        address.setTitle(request.title());
        address.setCity(request.city());
        address.setDistrict(request.district());
        address.setFullAddress(request.fullAddress());
        address.setPostalCode(request.postalCode());

        if (Boolean.TRUE.equals(request.isDefault())) {
            clearDefaultAddress(userId);
            address.setIsDefault(true);
        }

        Address updatedAddress = addressRepository.save(address);

        return mapToResponse(updatedAddress);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long addressId) {

        Address address = findAddressByIdAndUserId(addressId, userId);

        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressResponse setDefault(Long userId, Long addressId) {

        Address address = findAddressByIdAndUserId(addressId, userId);

        clearDefaultAddress(userId);

        address.setIsDefault(true);

        Address updatedAddress = addressRepository.save(address);

        return mapToResponse(updatedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public AddressInternalResponse getInternalAddress(Long userId, Long addressId) {
        Address address = findAddressByIdAndUserId(addressId, userId);

        return new AddressInternalResponse(
                address.getId(),
                address.getUser().getId(),
                address.getTitle(),
                address.getCity(),
                address.getDistrict(),
                address.getFullAddress(),
                address.getPostalCode(),
                Boolean.TRUE.equals(address.getIsDefault())
        );
    }


    private void clearDefaultAddress(Long userId) {
        addressRepository.findAllByUserId(userId)
                .forEach(address -> {
                    address.setIsDefault(false);
                    addressRepository.save(address);
                });
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorMessage.USER_NOT_FOUND));
    }

    private Address findAddressByIdAndUserId(Long addressId, Long userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new BusinessException(UserErrorMessage.ADDRESS_NOT_FOUND));
    }

    private AddressResponse mapToResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getTitle(),
                address.getCity(),
                address.getDistrict(),
                address.getFullAddress(),
                address.getPostalCode(),
                Boolean.TRUE.equals(address.getIsDefault())
        );
    }
}
