package com.pharmacyhub.controller.base;

import com.pharmacyhub.dto.response.ApiResponse;
import com.pharmacyhub.utils.EntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Base controller providing common functionality for all controllers
 */
public abstract class BaseController {
    
    @Autowired
    protected EntityMapper entityMapper;
    
    /**
     * Create a success response with OK status
     */
    protected <T> ResponseEntity<ApiResponse<T>> successResponse(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    /**
     * Create a success response with a custom status
     */
    protected <T> ResponseEntity<ApiResponse<T>> successResponse(T data, HttpStatus status) {
        return new ResponseEntity<>(ApiResponse.success(data, status.value()), status);
    }
    
    /**
     * Create a success response with metadata
     */
    protected <T> ResponseEntity<ApiResponse<T>> successResponse(T data, Map<String, Object> metadata) {
        return ResponseEntity.ok(ApiResponse.success(data, HttpStatus.OK.value(), metadata));
    }
    
    /**
     * Create a created response (201)
     */
    protected <T> ResponseEntity<ApiResponse<T>> createdResponse(T data) {
        return new ResponseEntity<>(
                ApiResponse.success(data, HttpStatus.CREATED.value()),
                HttpStatus.CREATED
        );
    }
    
    /**
     * Create a no content response (204)
     */
    protected <T> ResponseEntity<ApiResponse<T>> noContentResponse() {
        return new ResponseEntity<>(
                ApiResponse.success(null, HttpStatus.NO_CONTENT.value()),
                HttpStatus.NO_CONTENT
        );
    }
    
    /**
     * Create an error response
     */
    protected <T> ResponseEntity<ApiResponse<T>> errorResponse(HttpStatus status, String message) {
        return new ResponseEntity<>(ApiResponse.error(status.value(), message), status);
    }
    
    /**
     * Map an entity to a DTO
     */
    protected <D, T> D mapToDTO(T entity, Class<D> dtoClass) {
        return entityMapper.map(entity, dtoClass);
    }
    
    /**
     * Map a collection of entities to a list of DTOs
     */
    protected <D, T> List<D> mapToDTO(Collection<T> entities, Class<D> dtoClass) {
        return entityMapper.mapList(entities, dtoClass);
    }
    
    /**
     * Map a DTO to an entity
     */
    protected <D, T> T mapToEntity(D dto, Class<T> entityClass) {
        return entityMapper.mapToEntity(dto, entityClass);
    }
}
