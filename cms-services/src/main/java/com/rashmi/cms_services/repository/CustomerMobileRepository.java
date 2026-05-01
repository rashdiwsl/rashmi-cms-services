package com.rashmi.cms_services.repository;

import com.rashmi.cms_services.entity.CustomerMobile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerMobileRepository extends JpaRepository<CustomerMobile, Long> {
    void deleteByCustomerId(Long customerId);
}