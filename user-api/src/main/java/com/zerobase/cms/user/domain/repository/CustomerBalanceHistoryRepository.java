package com.zerobase.cms.user.domain.repository;

import com.zerobase.cms.user.domain.model.CustomerBalanceHistory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

@Repository
public interface CustomerBalanceHistoryRepository
    extends JpaRepository<CustomerBalanceHistory, Long> {

    Optional<CustomerBalanceHistory> findFirstByCustomer_IdOrderByDescription(
        @RequestParam("customer_id") Long customerId
    );
}
