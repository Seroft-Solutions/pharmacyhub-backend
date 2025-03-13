package com.pharmacyhub.security.constants;

/**
 * Constants for exam-related permissions
 * These constants are shared between frontend and backend to ensure naming consistency
 */
public final class ExamPermissionConstants {
    /**
     * Basic Exam Access
     */
    public static final String VIEW_EXAMS = "exams:view";
    public static final String TAKE_EXAM = "exams:take";
    
    /**
     * Exam Creation & Management
     */
    public static final String CREATE_EXAM = "exams:create";
    public static final String EDIT_EXAM = "exams:edit";
    public static final String DELETE_EXAM = "exams:delete";
    public static final String DUPLICATE_EXAM = "exams:duplicate";
    
    /**
     * Question Management
     */
    public static final String MANAGE_QUESTIONS = "exams:manage-questions";
    
    /**
     * Exam Administration
     */
    public static final String PUBLISH_EXAM = "exams:publish";
    public static final String UNPUBLISH_EXAM = "exams:unpublish";
    public static final String ASSIGN_EXAM = "exams:assign";
    
    /**
     * Results & Grading
     */
    public static final String GRADE_EXAM = "exams:grade";
    public static final String VIEW_RESULTS = "exams:view-results";
    public static final String EXPORT_RESULTS = "exams:export-results";
    
    /**
     * Analytics
     */
    public static final String VIEW_ANALYTICS = "exams:view-analytics";
    
    // Role-based permission groupings
    public static final String[] ADMIN_PERMISSIONS = {
        CREATE_EXAM, EDIT_EXAM, DELETE_EXAM, PUBLISH_EXAM, 
        UNPUBLISH_EXAM, MANAGE_QUESTIONS, ASSIGN_EXAM,
        GRADE_EXAM, VIEW_RESULTS, EXPORT_RESULTS, VIEW_ANALYTICS
    };
    
    public static final String[] INSTRUCTOR_PERMISSIONS = {
        CREATE_EXAM, EDIT_EXAM, PUBLISH_EXAM, UNPUBLISH_EXAM,
        MANAGE_QUESTIONS, ASSIGN_EXAM, GRADE_EXAM, 
        VIEW_RESULTS, VIEW_ANALYTICS
    };
    
    public static final String[] STUDENT_PERMISSIONS = {
        VIEW_EXAMS, TAKE_EXAM, VIEW_RESULTS
    };
    
    private ExamPermissionConstants() {
        // Private constructor to prevent instantiation
    }
}
