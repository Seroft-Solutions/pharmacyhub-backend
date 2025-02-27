package com.pharmacyhub.controller;

import com.pharmacyhub.domain.entity.ExamPaper;
import com.pharmacyhub.dto.ExamPaperDTO;
import com.pharmacyhub.dto.ExamStatsDTO;
import com.pharmacyhub.service.ExamPaperService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/exams/papers")
public class ExamPaperController {

    private final ExamPaperService examPaperService;

    public ExamPaperController(ExamPaperService examPaperService) {
        this.examPaperService = examPaperService;
    }

    @GetMapping
    public ResponseEntity<List<ExamPaperDTO>> getAllPapers() {
        return ResponseEntity.ok(examPaperService.getAllPapers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/model")
    public ResponseEntity<List<ExamPaperDTO>> getModelPapers() {
        return ResponseEntity.ok(examPaperService.getModelPapers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/past")
    public ResponseEntity<List<ExamPaperDTO>> getPastPapers() {
        return ResponseEntity.ok(examPaperService.getPastPapers().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamPaperDTO> getPaperById(@PathVariable Long id) {
        return examPaperService.getPaperById(id)
                .map(paper -> ResponseEntity.ok(convertToDTO(paper)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam paper not found"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ExamStatsDTO> getExamStats() {
        return ResponseEntity.ok(examPaperService.getExamStats());
    }

    @PostMapping
    public ResponseEntity<ExamPaperDTO> createPaper(@Valid @RequestBody ExamPaperDTO examPaperDTO) {
        try {
            ExamPaper paper = convertToEntity(examPaperDTO);
            ExamPaper createdPaper = examPaperService.createPaper(paper);
            return new ResponseEntity<>(convertToDTO(createdPaper), HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExamPaperDTO> updatePaper(@PathVariable Long id, @Valid @RequestBody ExamPaperDTO examPaperDTO) {
        try {
            ExamPaper paper = convertToEntity(examPaperDTO);
            ExamPaper updatedPaper = examPaperService.updatePaper(id, paper);
            return ResponseEntity.ok(convertToDTO(updatedPaper));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaper(@PathVariable Long id) {
        try {
            examPaperService.deletePaper(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    private ExamPaperDTO convertToDTO(ExamPaper paper) {
        ExamPaperDTO dto = new ExamPaperDTO();
        dto.setId(paper.getId());
        dto.setTitle(paper.getTitle());
        dto.setDescription(paper.getDescription());
        dto.setDifficulty(paper.getDifficulty().toString().toLowerCase());
        dto.setQuestionCount(paper.getQuestionCount());
        dto.setDurationMinutes(paper.getDurationMinutes());
        dto.setTags(paper.getTags());
        dto.setPremium(paper.getPremium());
        dto.setAttemptCount(paper.getAttemptCount());
        dto.setSuccessRatePercent(paper.getSuccessRatePercent());
        dto.setLastUpdatedDate(paper.getLastUpdatedDate().format(DateTimeFormatter.ISO_DATE));
        dto.setType(paper.getType().toString());
        // Only take the ID of the exam entity to prevent recursion
        dto.setExamId(paper.getExam() != null ? paper.getExam().getId() : null);
        return dto;
    }

    private ExamPaper convertToEntity(ExamPaperDTO dto) {
        ExamPaper paper = new ExamPaper();
        paper.setTitle(dto.getTitle());
        paper.setDescription(dto.getDescription());
        paper.setDifficulty(ExamPaper.Difficulty.valueOf(dto.getDifficulty().toUpperCase()));
        paper.setQuestionCount(dto.getQuestionCount());
        paper.setDurationMinutes(dto.getDurationMinutes());
        paper.setTags(dto.getTags() != null ? dto.getTags() : new HashSet<>());
        paper.setPremium(dto.getPremium());
        paper.setType(ExamPaper.PaperType.valueOf(dto.getType().toUpperCase()));
        if (dto.getLastUpdatedDate() != null) {
            paper.setLastUpdatedDate(LocalDate.parse(dto.getLastUpdatedDate()));
        }
        // Note: Exam entity will be set by the service based on examId
        return paper;
    }
}
