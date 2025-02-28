package com.pharmacyhub.security.evaluator;

import com.pharmacyhub.domain.entity.ExamAttempt;
import com.pharmacyhub.domain.repository.ExamAttemptRepository;
import com.pharmacyhub.domain.repository.ExamRepository;
import com.pharmacyhub.service.ExamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
// Removed Component annotation as the bean is explicitly registered in AccessEvaluatorConfig

import java.util.Collection;
import java.util.List;

/**
 * Evaluator for determining if a user can access a specific exam.
 * Used in @PreAuthorize annotations to secure exam access.
 */
// Explicitly registered as a bean in AccessEvaluatorConfig
public class ExamAccessEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(ExamAccessEvaluator.class);
    
    private final ExamAttemptRepository examAttemptRepository;
    private final ExamRepository examRepository;
    
    public ExamAccessEvaluator(
            ExamAttemptRepository examAttemptRepository,
            ExamRepository examRepository) {
        this.examAttemptRepository = examAttemptRepository;
        this.examRepository = examRepository;
    }
    
    /**
     * Determines if the authenticated user can access the specified exam.
     * Access is granted if:
     * 1. The user has an ADMIN or INSTRUCTOR role
     * 2. The exam exists (simplified policy to allow all authenticated users to access any exam)
     *
     * @param authentication The current authentication context
     * @param examId The ID of the exam to check access for
     * @return True if the user can access the exam, false otherwise
     */
    public boolean canAccessExam(Authentication authentication, Long examId) {
        if (authentication == null) {
            logger.warn("Authentication is null when checking exam access for examId: {}", examId);
            return false;
        }
        
        // If user has ADMIN or INSTRUCTOR role, they automatically have access
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities.stream().anyMatch(a -> 
                a.getAuthority().equals("ROLE_ADMIN") || 
                a.getAuthority().equals("ROLE_INSTRUCTOR"))) {
            logger.debug("User has ADMIN or INSTRUCTOR role, granting access to exam: {}", examId);
            return true;
        }
        
        // Get the user ID from the authentication
        String userId = authentication.getName();
        logger.debug("Checking if user {} can access exam {}", userId, examId);
        
        // Check if the exam exists (we're simplifying by allowing access to any exam that exists)
        boolean examExists = examRepository.existsById(examId);
        
        if (!examExists) {
            logger.debug("Exam {} doesn't exist, denying access", examId);
            return false;
        }
        
        // Allow all authenticated users to access any exam that exists
        logger.debug("Exam {} exists, granting access to user {}", examId, userId);
        return true;
    }
}