package com.rashmi.cms_services.repository;

import com.rashmi.cms_services.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByNicNumber(String nicNumber);

    boolean existsByNicNumber(String nicNumber);

    boolean existsByNicNumberAndIdNot(String nicNumber, Long id);

    @Query("SELECT DISTINCT c FROM Customer c " +
            "LEFT JOIN FETCH c.mobiles " +
            "LEFT JOIN FETCH c.addresses a " +
            "LEFT JOIN FETCH a.city ct " +
            "LEFT JOIN FETCH ct.country " +
            "LEFT JOIN FETCH c.familyMembers f " +
            "LEFT JOIN FETCH f.familyMember " +
            "WHERE c.id = :id")
    Optional<Customer> findByIdWithDetails(@Param("id") Long id);
}