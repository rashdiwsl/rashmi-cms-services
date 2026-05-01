package com.rashmi.cms_services.repository;

import com.rashmi.cms_services.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNicNumber(String nicNumber);

    boolean existsByNicNumber(String nicNumber);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.mobiles LEFT JOIN FETCH c.addresses LEFT JOIN FETCH c.familyMembers WHERE c.id = :id")
    Optional<Customer> findByIdWithDetails(Long id);
}