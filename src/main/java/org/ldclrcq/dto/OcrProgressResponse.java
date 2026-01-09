package org.ldclrcq.dto;

import java.util.List;

public class OcrProgressResponse {

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    private Status status;
    private int currentPage;
    private int totalPages;
    private List<OcrResultDto> results;
    private String errorMessage;

    public OcrProgressResponse() {
    }

    public OcrProgressResponse(Status status, int currentPage, int totalPages, List<OcrResultDto> results, String errorMessage) {
        this.status = status;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.results = results;
        this.errorMessage = errorMessage;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<OcrResultDto> getResults() {
        return results;
    }

    public void setResults(List<OcrResultDto> results) {
        this.results = results;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
