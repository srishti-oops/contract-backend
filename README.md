# Contract Management Backend

Java + Spring Boot backend service for uploading, storing, updating contract status (`DRAFT -> REVIEW -> APPROVED`), and asking questions on **PDF** (`.pdf`) and **MS Word** (`.docx`) contracts.

## Features
- **Upload & Parse (`POST /api/contracts`)**: Accepts `.pdf` and `.docx` files, extracts text using Apache PDFBox and Apache POI, and saves the contract in `DRAFT` status.
- **Fetch Contract (`GET /api/contracts/{id}` & `GET /api/contracts`)**: Retrieves stored contract details and extracted text.
- **Update Status (`PATCH /api/contracts/{id}/status`)**: Transitions status along `DRAFT -> REVIEW -> APPROVED` (and allows `REVIEW -> DRAFT`).
- **Simple Q&A (`POST /api/contracts/{id}/ask`)**: Matches question keywords against contract sentences and returns top matching excerpts.
- **Swagger UI**: Available at `/swagger-ui.html`.

## Run Locally
```bash
mvn spring-boot:run
