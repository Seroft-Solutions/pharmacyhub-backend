package com.pharmacyhub.controller;

import com.pharmacyhub.entity.Entry;
import com.pharmacyhub.service.EntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/entries")
public class EntryController {

    @Autowired
    private EntryService entryService;

    @GetMapping
    public ResponseEntity<List<Entry>> getAllEntries() {
        List<Entry> entries = entryService.getAllEntries();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entry> getEntryById(@PathVariable Long id) {
        return entryService.getEntryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Entry> createEntry(@RequestBody Entry entry) {
        Entry savedEntry = entryService.saveEntry(entry);
        return ResponseEntity.ok(savedEntry);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Entry> updateEntry(@PathVariable Long id, @RequestBody Entry entryDetails) {
        try {
            Entry updatedEntry = entryService.updateEntry(id, entryDetails);
            return ResponseEntity.ok(updatedEntry);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEntry(@PathVariable Long id) {
        return entryService.getEntryById(id)
                .map(entry -> {
                    entryService.deleteEntry(id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Additional endpoints can be added here as needed

    @GetMapping("/search")
    public ResponseEntity<List<Entry>> searchEntries(@RequestParam String query) {
        List<Entry> entries = entryService.searchEntries(query);
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getEntryCount() {
        long count = entryService.getEntryCount();
        return ResponseEntity.ok(count);
    }
}