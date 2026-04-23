package com.snackoverflow.snack.service;

import com.snackoverflow.snack.dto.CreateSnackPurchaseRequest;
import com.snackoverflow.snack.dto.SnackPurchaseResponse;
import com.snackoverflow.snack.dto.UpdateSnackPurchaseRequest;
import com.snackoverflow.snack.dto.UpdateStatusRequest;
import com.snackoverflow.snack.entity.SnackPurchase;
import com.snackoverflow.snack.entity.SnackPurchaseStatus;
import com.snackoverflow.snack.repository.SnackPurchaseRepository;
import com.snackoverflow.user.entity.User;
import com.snackoverflow.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SnackPurchaseService {

    private final SnackPurchaseRepository snackPurchaseRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<SnackPurchaseResponse> getAll(Pageable pageable) {
        return snackPurchaseRepository.findAll(pageable).map(SnackPurchaseResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<SnackPurchaseResponse> getByStatus(SnackPurchaseStatus status, Pageable pageable) {
        return snackPurchaseRepository.findByStatus(status, pageable).map(SnackPurchaseResponse::from);
    }

    @Transactional(readOnly = true)
    public SnackPurchaseResponse getOne(UUID id) {
        return SnackPurchaseResponse.from(findOrThrow(id));
    }

    @Transactional
    public SnackPurchaseResponse create(UUID adminId, CreateSnackPurchaseRequest request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        SnackPurchase snack = SnackPurchase.builder()
                .name(request.name())
                .createdBy(admin)
                .build();

        return SnackPurchaseResponse.from(snackPurchaseRepository.save(snack));
    }

    @Transactional
    public SnackPurchaseResponse updateName(UUID id, UpdateSnackPurchaseRequest request) {
        SnackPurchase snack = findOrThrow(id);
        snack.updateName(request.name());
        return SnackPurchaseResponse.from(snack);
    }

    @Transactional
    public SnackPurchaseResponse updateStatus(UUID id, UpdateStatusRequest request) {
        SnackPurchase snack = findOrThrow(id);
        snack.updateStatus(request.status());
        return SnackPurchaseResponse.from(snack);
    }

    @Transactional
    public void delete(UUID id) {
        SnackPurchase snack = findOrThrow(id);
        snackPurchaseRepository.delete(snack);
    }

    private SnackPurchase findOrThrow(UUID id) {
        return snackPurchaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과자를 찾을 수 없습니다."));
    }
}
