package com.pharmacyhub.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.pharmacyhub.entity.Entry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleContactService {

    private static final String APPLICATION_NAME = "Pharmacy Hub";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList("https://www.googleapis.com/auth/contacts");

    @Autowired
    private ResourceLoader resourceLoader;

    private final NetHttpTransport HTTP_TRANSPORT;

    public GoogleContactService() throws GeneralSecurityException, IOException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    }

    private Credential getCredentials() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:credentials.json");
        InputStream in = resource.getInputStream();
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        // Here you would typically use a more robust way to get the user's credentials
        // This is a simplified version for demonstration purposes
        return flow.loadCredential("user");
    }

    public void saveEntryToGoogleContacts(Entry entry) throws IOException {
        Credential credential = getCredentials();
        PeopleService peopleService = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        Person contactToCreate = new Person();
        contactToCreate.setNames(Collections.singletonList(new Name().setGivenName(entry.getName())));
        contactToCreate.setPhoneNumbers(Collections.singletonList(new PhoneNumber().setValue(entry.getContactNumber())));

        Person createdContact = peopleService.people().createContact(contactToCreate).execute();
        System.out.println("Contact created: " + createdContact.getResourceName());
    }
}