package com.pharmacyhub.service.geo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for IP-based geolocation
 */
@Service
public class GeoIpService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeoIpService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${pharmacyhub.geo.ip.api-url:http://ip-api.com/json/}")
    private String geoApiUrl;
    
    // Simple in-memory cache to avoid excessive API calls
    private final Map<String, String> ipCountryCache = new HashMap<>();
    
    public GeoIpService() {
        this.restTemplate = new RestTemplate();
    }
    
    /**
     * Get country from IP address
     * 
     * @param ipAddress IP address
     * @return Country name or null if not found
     */
    public String getCountryFromIp(String ipAddress) {
        // Check for invalid IP
        if (ipAddress == null || ipAddress.isBlank() || 
            "localhost".equals(ipAddress) || "127.0.0.1".equals(ipAddress) ||
            "0.0.0.0".equals(ipAddress) || "::1".equals(ipAddress)) {
            return "Local";
        }
        
        // Check cache first
        if (ipCountryCache.containsKey(ipAddress)) {
            return ipCountryCache.get(ipAddress);
        }
        
        try {
            // Placeholder - in production, replace with actual IP geolocation API call
            // For now, using a simple approach with ip-api.com
            String url = geoApiUrl + ipAddress;
            GeoIpResponse response = restTemplate.getForObject(url, GeoIpResponse.class);
            
            if (response != null && "success".equals(response.getStatus())) {
                // Cache the result
                ipCountryCache.put(ipAddress, response.getCountry());
                return response.getCountry();
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error getting country from IP: {}", ipAddress, e);
            return null;
        }
    }
    
    /**
     * Response class for IP geolocation API
     */
    private static class GeoIpResponse {
        private String status;
        private String country;
        private String countryCode;
        private String city;
        private String region;
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getCountry() {
            return country;
        }
        
        public void setCountry(String country) {
            this.country = country;
        }
        
        public String getCountryCode() {
            return countryCode;
        }
        
        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }
        
        public String getCity() {
            return city;
        }
        
        public void setCity(String city) {
            this.city = city;
        }
        
        public String getRegion() {
            return region;
        }
        
        public void setRegion(String region) {
            this.region = region;
        }
    }
}
