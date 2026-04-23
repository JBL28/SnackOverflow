package com.snackoverflow.snack.repository;

import com.snackoverflow.snack.entity.SnackPurchase;
import com.snackoverflow.snack.entity.SnackPurchaseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SnackPurchaseRepository extends JpaRepository<SnackPurchase, UUID> {

    Page<SnackPurchase> findByStatus(SnackPurchaseStatus status, Pageable pageable);
}
