package com.example.contracts.service;

import com.example.contracts.model.Contract;
import com.example.contracts.model.ContractStatus;
import com.example.contracts.repository.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final DocumentParserService documentParserService;

    public ContractService(ContractRepository contractRepository, DocumentParserService documentParserService) {
        this.contractRepository = contractRepository;
        this.documentParserService = documentParserService;
    }

    @Transactional
    public Contract createContract(String title, MultipartFile file) throws IOException {
        String extractedText = documentParserService.extractText(file);
        if (extractedText.isBlank()) {
            throw new IllegalArgumentException("Could not extract any readable text from the document.");
        }

        Contract contract = new Contract();
        contract.setTitle((title != null && !title.isBlank()) ? title.trim() : file.getOriginalFilename());
        contract.setFileName(file.getOriginalFilename());
        contract.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        contract.setStatus(ContractStatus.DRAFT);
        contract.setExtractedText(extractedText);

        return contractRepository.save(contract);
    }

    @Transactional(readOnly = true)
    public Contract getContractById(Long id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Contract not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Contract> getAllContracts() {
        return contractRepository.findAll();
    }

    @Transactional
    public Contract updateStatus(Long id, ContractStatus nextStatus) {
        Contract contract = getContractById(id);
        ContractStatus currentStatus = contract.getStatus();

        if (!currentStatus.canTransitionTo(nextStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition from " + currentStatus + " to " + nextStatus +
                    ". Allowed flow: DRAFT -> REVIEW -> APPROVED."
            );
        }

        contract.setStatus(nextStatus);
        return contractRepository.save(contract);
    }
}
