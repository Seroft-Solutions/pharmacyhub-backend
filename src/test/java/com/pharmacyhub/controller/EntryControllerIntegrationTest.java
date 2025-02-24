package com.pharmacyhub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.entity.Entry;
import com.pharmacyhub.service.EntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class EntryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EntryService entryService;

    private Entry testEntry1;
    private Entry testEntry2;

    @BeforeEach
    void setUp() {
        // Create test entries
        testEntry1 = new Entry();
        testEntry1.setId(1L);
        testEntry1.setName("Test Entry 1");
        testEntry1.setProfession("Pharmacist");
        testEntry1.setPharmacyName("Test Pharmacy 1");
        testEntry1.setPotential("High");
        testEntry1.setExperience(5);
        testEntry1.setContactNumber("03001234567");
        testEntry1.setCity("Lahore");
        testEntry1.setArea("DHA");
        testEntry1.setNotes("Test notes 1");
        
        testEntry2 = new Entry();
        testEntry2.setId(2L);
        testEntry2.setName("Test Entry 2");
        testEntry2.setProfession("Manager");
        testEntry2.setPharmacyName("Test Pharmacy 2");
        testEntry2.setPotential("Medium");
        testEntry2.setExperience(3);
        testEntry2.setContactNumber("03009876543");
        testEntry2.setCity("Karachi");
        testEntry2.setArea("Clifton");
        testEntry2.setNotes("Test notes 2");
        
        // Mock service methods
        when(entryService.getAllEntries()).thenReturn(Arrays.asList(testEntry1, testEntry2));
        when(entryService.getEntryById(1L)).thenReturn(Optional.of(testEntry1));
        when(entryService.getEntryById(2L)).thenReturn(Optional.of(testEntry2));
        when(entryService.getEntryById(999L)).thenReturn(Optional.empty());
        when(entryService.saveEntry(any(Entry.class))).thenAnswer(i -> {
            Entry entry = i.getArgument(0);
            entry.setId(3L);
            return entry;
        });
        when(entryService.updateEntry(eq(1L), any(Entry.class))).thenReturn(testEntry1);
        when(entryService.updateEntry(eq(999L), any(Entry.class))).thenThrow(new RuntimeException("Entry not found"));
        doNothing().when(entryService).deleteEntry(anyLong());
        when(entryService.searchEntries(anyString())).thenReturn(Arrays.asList(testEntry1));
        when(entryService.getEntryCount()).thenReturn(2L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetAllEntries() throws Exception {
        mockMvc.perform(get("/api/admin/entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Entry 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Test Entry 2"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEntryById() throws Exception {
        mockMvc.perform(get("/api/admin/entries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Entry 1"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEntryByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/admin/entries/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testCreateEntry() throws Exception {
        Entry newEntry = new Entry();
        newEntry.setName("New Entry");
        newEntry.setProfession("New Profession");
        
        mockMvc.perform(post("/api/admin/entries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEntry)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("New Entry"));
        
        verify(entryService, times(1)).saveEntry(any(Entry.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateEntry() throws Exception {
        Entry updatedEntry = new Entry();
        updatedEntry.setName("Updated Entry");
        updatedEntry.setProfession("Updated Profession");
        
        mockMvc.perform(put("/api/admin/entries/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEntry)))
                .andExpect(status().isOk());
        
        verify(entryService, times(1)).updateEntry(eq(1L), any(Entry.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testUpdateEntryNotFound() throws Exception {
        Entry updatedEntry = new Entry();
        updatedEntry.setName("Updated Entry");
        
        mockMvc.perform(put("/api/admin/entries/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedEntry)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEntry() throws Exception {
        mockMvc.perform(delete("/api/admin/entries/1"))
                .andExpect(status().isOk());
        
        verify(entryService, times(1)).deleteEntry(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testDeleteEntryNotFound() throws Exception {
        mockMvc.perform(delete("/api/admin/entries/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testSearchEntries() throws Exception {
        mockMvc.perform(get("/api/admin/entries/search")
                .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Entry 1"));
        
        verify(entryService, times(1)).searchEntries("test");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void testGetEntryCount() throws Exception {
        mockMvc.perform(get("/api/admin/entries/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
        
        verify(entryService, times(1)).getEntryCount();
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(get("/api/admin/entries"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void testInsufficientPermissions() throws Exception {
        mockMvc.perform(get("/api/admin/entries"))
                .andExpect(status().isForbidden());
    }
}
