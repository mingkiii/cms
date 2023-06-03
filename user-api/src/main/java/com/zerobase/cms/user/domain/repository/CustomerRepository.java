package com.zerobase.cms.user.domain.repository;

import com.zerobase.cms.user.domain.model.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);
    Optional<Customer> findByEmail(String email);
}
