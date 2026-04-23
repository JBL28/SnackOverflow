package com.snackoverflow.snack.controller;

import com.snackoverflow.common.ApiResponse;
import com.snackoverflow.snack.dto.CreateSnackPurchaseRequest;
import com.snackoverflow.snack.dto.SnackPurchaseResponse;
import com.snackoverflow.snack.dto.UpdateSnackPurchaseRequest;
import com.snackoverflow.snack.dto.UpdateStatusRequest;
import com.snackoverflow.snack.entity.SnackPurchaseStatus;
import com.snackoverflow.snack.service.SnackPurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/snack-purchases")
@RequiredArgsConstructor
public class SnackPurchaseController {

    private final SnackPurchaseService snackPurchaseService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SnackPurchaseResponse>>> getAll(
            @RequestParam(required = false) SnackPurchaseStatus status,
            @PageableDefault(size = 5, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<SnackPurchaseResponse> page = (status != null)
                ? snackPurchaseService.getByStatus(status, pageable)
                : snackPurchaseService.getAll(pageable);

        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SnackPurchaseResponse>> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(snackPurchaseService.getOne(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SnackPurchaseResponse>> create(
            Authentication auth,
            @RequestBody @Valid CreateSnackPurchaseRequest request) {

        UUID adminId = UUID.fromString(auth.getName());
        SnackPurchaseResponse response = snackPurchaseService.create(adminId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SnackPurchaseResponse>> updateName(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateSnackPurchaseRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(snackPurchaseService.updateName(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SnackPurchaseResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.ok(snackPurchaseService.updateStatus(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        snackPurchaseService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
