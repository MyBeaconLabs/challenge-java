package com.challenge.controller;

import com.challenge.dto.TransferRequest;
import com.challenge.dto.TransferResponse;
import com.challenge.entity.Transfer;
import com.challenge.service.TransferService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private static final Logger log = LoggerFactory.getLogger(TransferController.class);

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(@Valid @RequestBody TransferRequest request) {
        try {
            Transfer transfer = transferService.createTransfer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(TransferResponse.from(transfer));
        } catch (Exception e) {
            log.error("Transfer failed", e);
            return ResponseEntity.ok(TransferResponse.failed(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public TransferResponse getTransfer(@PathVariable Long id) {
        return TransferResponse.from(transferService.getTransfer(id));
    }
}
