package com.example.contracts.controller;

import com.example.contracts.model.Contract;
import com.example.contracts.model.ContractStatus;
import com.example.contracts.service.ContractQaService;
import com.example.contracts.service.ContractService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;
    private final ContractQaService contractQaService;

    public ContractController(ContractService contractService, ContractQaService contractQaService) {
        this.contractService = contractService;
        this.contractQaService = contractQaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Contract> uploadContract(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "contractDescription", required = false) String contractDescription,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Contract created = contractService.createContract(title, contractDescription, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Contract>> getAllContracts() {
        return ResponseEntity.ok(contractService.getAllContracts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contract> getContract(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.getContractById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Contract> updateContractStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload
    ) {
        String rawStatus = payload.get("status");
        if (rawStatus == null) {
            throw new IllegalArgumentException("Missing 'status' field in request body.");
        }
        ContractStatus nextStatus = ContractStatus.valueOf(rawStatus.trim().toUpperCase());
        Contract updated = contractService.updateStatus(id, nextStatus);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/ask")
    public ResponseEntity<Map<String, Object>> askQuestion(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload
    ) {
        Contract contract = contractService.getContractById(id);
        String question = payload.get("question");
        return ResponseEntity.ok(contractQaService.answerQuestion(contract, question));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
