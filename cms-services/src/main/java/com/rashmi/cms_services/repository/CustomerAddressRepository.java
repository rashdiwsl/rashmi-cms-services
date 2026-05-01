package com.rashmi.cms_services.repository;

import com.rashmi.cms_services.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    void deleteByCustomerId(Long customerId);
}