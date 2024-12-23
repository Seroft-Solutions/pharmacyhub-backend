package com.pharmacy.hub.service;

import com.pharmacy.hub.entity.Entry;
import com.pharmacy.hub.repository.EntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EntryService {

    @Autowired
    private EntryRepository entryRepository;
    @Autowired
    private GoogleContactService googleContactService;

    public List<Entry> getAllEntries() {
        return entryRepository.findAll();
    }

    public Optional<Entry> getEntryById(Long id) {
        return entryRepository.findById(id);
    }

    public Entry saveEntry(Entry entry) {
        Entry savedEntry = entryRepository.save(entry);
        try {
            googleContactService.saveEntryToGoogleContacts(savedEntry);
        } catch (IOException e) {
            // Log the error and handle it appropriately
            e.printStackTrace();
        }
        return savedEntry;
    }

    public void deleteEntry(Long id) {
        entryRepository.deleteById(id);
    }

    public Entry updateEntry(Long id, Entry entryDetails) {
        Entry entry = entryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entry not found for this id :: " + id));

        entry.setName(entryDetails.getName());
        entry.setProfession(entryDetails.getProfession());
        entry.setPharmacyName(entryDetails.getPharmacyName());
        entry.setPotential(entryDetails.getPotential());
        entry.setExperience(entryDetails.getExperience());
        entry.setContactNumber(entryDetails.getContactNumber());
        entry.setCity(entryDetails.getCity());
        entry.setArea(entryDetails.getArea());
        entry.setNotes(entryDetails.getNotes());

        return entryRepository.save(entry);
    }

    public List<Entry> searchEntries(String query) {
        List<Entry> allEntries = entryRepository.findAll();
        return allEntries.stream()
                .filter(entry -> entry.getName().toLowerCase().contains(query.toLowerCase()) ||
                        entry.getPharmacyName().toLowerCase().contains(query.toLowerCase()) ||
                        entry.getCity().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    public long getEntryCount() {
        return entryRepository.count();
    }
}