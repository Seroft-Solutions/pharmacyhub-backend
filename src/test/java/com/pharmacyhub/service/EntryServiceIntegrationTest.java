package com.pharmacyhub.service;

import com.pharmacyhub.config.BaseIntegrationTest;
import com.pharmacyhub.entity.Entry;
import com.pharmacyhub.repository.EntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EntryServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EntryService entryService;

    @Autowired
    private EntryRepository entryRepository;

    @MockBean
    private GoogleContactService googleContactService;

    private Entry testEntry1;
    private Entry testEntry2;

    @BeforeEach
    void setUp() throws Exception {
        // Clear repository
        entryRepository.deleteAll();
        
        // Create test entries
        testEntry1 = new Entry();
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
        testEntry2.setName("Test Entry 2");
        testEntry2.setProfession("Manager");
        testEntry2.setPharmacyName("Test Pharmacy 2");
        testEntry2.setPotential("Medium");
        testEntry2.setExperience(3);
        testEntry2.setContactNumber("03009876543");
        testEntry2.setCity("Karachi");
        testEntry2.setArea("Clifton");
        testEntry2.setNotes("Test notes 2");
        
        // Save test entries
        testEntry1 = entryRepository.save(testEntry1);
        testEntry2 = entryRepository.save(testEntry2);
        
        // Mock Google Contact Service to avoid external API calls
        doNothing().when(googleContactService).saveEntryToGoogleContacts(any(Entry.class));
    }

    @Test
    void testGetAllEntries() {
        // Get all entries
        List<Entry> entries = entryService.getAllEntries();
        
        // Verify entries
        assertEquals(2, entries.size());
        assertTrue(entries.contains(testEntry1));
        assertTrue(entries.contains(testEntry2));
    }

    @Test
    void testGetEntryById() {
        // Get entry by ID
        Optional<Entry> entry = entryService.getEntryById(testEntry1.getId());
        
        // Verify entry
        assertTrue(entry.isPresent());
        assertEquals(testEntry1.getName(), entry.get().getName());
        assertEquals(testEntry1.getPharmacyName(), entry.get().getPharmacyName());
    }

    @Test
    void testSaveEntry() throws Exception {
        // Create new entry
        Entry newEntry = new Entry();
        newEntry.setName("New Entry");
        newEntry.setProfession("Proprietor");
        newEntry.setPharmacyName("New Pharmacy");
        newEntry.setPotential("Low");
        newEntry.setExperience(2);
        newEntry.setContactNumber("03007654321");
        newEntry.setCity("Islamabad");
        newEntry.setArea("F-10");
        newEntry.setNotes("New entry notes");
        
        // Save entry
        Entry savedEntry = entryService.saveEntry(newEntry);
        
        // Verify entry was saved
        assertNotNull(savedEntry.getId());
        assertEquals("New Entry", savedEntry.getName());
        assertEquals("Proprietor", savedEntry.getProfession());
        
        // Verify Google Contact Service was called
        verify(googleContactService, times(1)).saveEntryToGoogleContacts(any(Entry.class));
    }

    @Test
    void testUpdateEntry() {
        // Update entry
        testEntry1.setName("Updated Entry");
        testEntry1.setProfession("Updated Profession");
        
        // Save updated entry
        Entry updatedEntry = entryService.updateEntry(testEntry1.getId(), testEntry1);
        
        // Verify entry was updated
        assertEquals("Updated Entry", updatedEntry.getName());
        assertEquals("Updated Profession", updatedEntry.getProfession());
        
        // Verify entry in database was updated
        Entry dbEntry = entryRepository.findById(testEntry1.getId()).get();
        assertEquals("Updated Entry", dbEntry.getName());
        assertEquals("Updated Profession", dbEntry.getProfession());
    }

    @Test
    void testDeleteEntry() {
        // Delete entry
        entryService.deleteEntry(testEntry1.getId());
        
        // Verify entry was deleted
        assertFalse(entryRepository.findById(testEntry1.getId()).isPresent());
    }

    @Test
    void testSearchEntries() {
        // Search entries by query
        List<Entry> searchResults = entryService.searchEntries("lahore");
        
        // Verify search results
        assertEquals(1, searchResults.size());
        assertEquals(testEntry1.getId(), searchResults.get(0).getId());
        
        // Search entries by another query
        searchResults = entryService.searchEntries("test pharmacy");
        
        // Verify search results
        assertEquals(2, searchResults.size());
    }

    @Test
    void testGetEntryCount() {
        // Get entry count
        long count = entryService.getEntryCount();
        
        // Verify count
        assertEquals(2, count);
    }
}
