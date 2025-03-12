package com.pharmacyhub.deprecated.controller;

// DEPRECATED: This controller functionality is now covered by ExamController
// Use ExamController for all new development

import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.dto.response.ExamResponseDTO;
import com.pharmacyhub.dto.request.ExamFilterRequestDTO;
import com.pharmacyhub.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams/papers")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Exam Papers", description = "API endpoints for exam papers management")
public class ExamPaperController {

    private static final Logger logger = LoggerFactory.getLogger(ExamPaperController.class);
    private final ExamService examService;

    public ExamPaperController(ExamService examService) {
        this.examService = examService;
    }

    @GetMapping("/model")
    @Operation(summary = "Get all model papers")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getModelPapers(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String topic) {
        
        logger.info("Fetching model papers with filters: difficulty={}, topic={}", difficulty, topic);
        
        ExamFilterRequestDTO filters = new ExamFilterRequestDTO();
        filters.setType("MODEL");
        filters.setDifficulty(difficulty);
        filters.setTopic(topic);
        
        List<ExamResponseDTO> papers = examService.findPapersByFilter(filters);
        logger.info("Found {} model papers matching criteria", papers.size());
        
        return ResponseEntity.ok(ApiResponse.success(papers));
    }

    @GetMapping("/past")
    @Operation(summary = "Get all past papers")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getPastPapers(
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String topic) {
        
        logger.info("Fetching past papers with filters: difficulty={}, topic={}", difficulty, topic);
        
        ExamFilterRequestDTO filters = new ExamFilterRequestDTO();
        filters.setType("PAST");
        filters.setDifficulty(difficulty);
        filters.setTopic(topic);
        
        List<ExamResponseDTO> papers = examService.findPapersByFilter(filters);
        logger.info("Found {} past papers matching criteria", papers.size());
        
        return ResponseEntity.ok(ApiResponse.success(papers));
    }

    @GetMapping("/subject")
    @Operation(summary = "Get papers by subject")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> getSubjectPapers(
            @RequestParam(required = true) String subject,
            @RequestParam(required = false) String difficulty) {
        
        logger.info("Fetching subject papers: subject={}, difficulty={}", subject, difficulty);
        
        ExamFilterRequestDTO filters = new ExamFilterRequestDTO();
        filters.setTopic(subject);
        filters.setDifficulty(difficulty);
        
        List<ExamResponseDTO> papers = examService.findPapersByFilter(filters);
        logger.info("Found {} subject papers matching criteria", papers.size());
        
        return ResponseEntity.ok(ApiResponse.success(papers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get paper by ID")
    public ResponseEntity<ApiResponse<ExamResponseDTO>> getPaperById(@PathVariable Long id) {
        logger.info("Fetching paper with ID: {}", id);
        
        ExamResponseDTO paper = examService.findPaperById(id);
        
        return ResponseEntity.ok(ApiResponse.success(paper));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get exam statistics")
    public ResponseEntity<ApiResponse<Object>> getExamStats() {
        logger.info("Fetching exam statistics");
        
        Object stats = examService.getExamStats();
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/search")
    @Operation(summary = "Search for papers")
    public ResponseEntity<ApiResponse<List<ExamResponseDTO>>> searchPapers(
            @RequestBody @Valid ExamFilterRequestDTO filters) {
        
        logger.info("Searching papers with filters: {}", filters);
        
        List<ExamResponseDTO> papers = examService.findPapersByFilter(filters);
        logger.info("Found {} papers matching search criteria", papers.size());
        
        return ResponseEntity.ok(ApiResponse.success(papers));
    }
}