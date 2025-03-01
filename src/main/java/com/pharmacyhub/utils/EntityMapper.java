package com.pharmacyhub.utils;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized mapper for converting between entities and DTOs
 * Uses ModelMapper for consistent mapping across the application
 */
@Component
public class EntityMapper {
    private final ModelMapper modelMapper;
    
    public EntityMapper() {
        this.modelMapper = new ModelMapper();
        configureMapper();
    }
    
    /**
     * Configure the model mapper with custom settings and type mappings
     */
    private void configureMapper() {
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        
        // Add specific type mappings here
        // For example:
        // modelMapper.createTypeMap(Entity.class, DTO.class)
        //     .addMappings(mapper -> mapper.skip(DTO::setPassword));
    }
    
    /**
     * Convert an entity to a DTO
     */
    public <D, T> D map(final T entity, Class<D> dtoClass) {
        return modelMapper.map(entity, dtoClass);
    }
    
    /**
     * Convert a DTO to an entity
     */
    public <D, T> T mapToEntity(final D dto, Class<T> entityClass) {
        return modelMapper.map(dto, entityClass);
    }
    
    /**
     * Convert a collection of entities to a list of DTOs
     */
    public <D, T> List<D> mapList(final Collection<T> entityList, Class<D> dtoClass) {
        return entityList.stream()
                .map(entity -> map(entity, dtoClass))
                .collect(Collectors.toList());
    }
    
    /**
     * Map specific fields from the source object to the destination object
     */
    public void mapProperties(Object source, Object destination) {
        modelMapper.map(source, destination);
    }
    
    /**
     * Get the configured ModelMapper instance for custom mapping
     */
    public ModelMapper getModelMapper() {
        return modelMapper;
    }
}
