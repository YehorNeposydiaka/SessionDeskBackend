package org.example.Note.Service;

import org.example.Appointment.Repository.AppointmentRepository;
import org.example.Client.Entity.Client;
import org.example.Client.Repository.ClientRepository;
import org.example.Common.Exception.ApiException;
import org.example.Note.DTO.CreateNoteRequest;
import org.example.Note.DTO.NoteResponse;
import org.example.Note.DTO.UpdateNoteRequest;
import org.example.Note.Entity.Note;
import org.example.Note.Repository.NoteRepository;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock private NoteRepository noteRepository;
    @Mock private SpecialistRepository specialistRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private AppointmentRepository appointmentRepository;

    private NoteService noteService;

    private final UUID specialistId = UUID.randomUUID();
    private final UUID clientId = UUID.randomUUID();
    private final UUID noteId = UUID.randomUUID();

    private Specialist specialist;
    private Client client;

    @BeforeEach
    void setUp() {
        specialist = mock(Specialist.class);
        client = mock(Client.class);
        lenient().when(client.getId()).thenReturn(clientId);

        noteService = new NoteService(noteRepository, specialistRepository,
                clientRepository, appointmentRepository);
    }

    // ---------- helpers ----------

    private Note existingNote() {
        return Note.builder()
                .id(noteId)
                .client(client)
                .specialist(specialist)
                .noteName("Old name")
                .body("Old body")
                .attachmentUrl("http://old.url")
                .build();
    }

    private void stubCreateHappyPath() {
        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(appointmentRepository.existsBySpecialistIdAndClientId(specialistId, clientId)).thenReturn(true);
    }

    // ---------- createNote ----------

    @Test
    void createNote_success_trimsNameAndSaves() {
        stubCreateHappyPath();
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        NoteResponse response = noteService.createNote(specialistId, clientId,
                new CreateNoteRequest("  Session 1  ", "  some text  ", "http://file.url"));

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        Note saved = captor.getValue();

        assertEquals("Session 1", saved.getNoteName());
        assertEquals("some text", saved.getBody());
        assertEquals("http://file.url", saved.getAttachmentUrl());
        assertEquals("Session 1", response.noteName());
        assertEquals(clientId, response.clientId());
    }

    @Test
    void createNote_emptyBodyAndAttachment_savedAsNull() {
        stubCreateHappyPath();
        when(noteRepository.save(any(Note.class))).thenAnswer(inv -> inv.getArgument(0));

        noteService.createNote(specialistId, clientId, new CreateNoteRequest("Name", "   ", ""));

        ArgumentCaptor<Note> captor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(captor.capture());
        assertNull(captor.getValue().getBody());
        assertNull(captor.getValue().getAttachmentUrl());
    }

    @Test
    void createNote_blankName_throwsAndDoesNotSave() {
        stubCreateHappyPath();

        assertThrows(ApiException.class, () -> noteService.createNote(specialistId, clientId,
                new CreateNoteRequest("   ", "body", null)));

        verify(noteRepository, never()).save(any());
    }

    @Test
    void createNote_specialistNotFound_throws() {
        when(specialistRepository.findById(specialistId)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> noteService.createNote(specialistId, clientId,
                new CreateNoteRequest("Name", null, null)));

        verify(noteRepository, never()).save(any());
    }

    @Test
    void createNote_clientNotFound_throws() {
        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> noteService.createNote(specialistId, clientId,
                new CreateNoteRequest("Name", null, null)));

        verify(noteRepository, never()).save(any());
    }

    @Test
    void createNote_clientWithoutAppointments_throwsAndDoesNotSave() {
        when(specialistRepository.findById(specialistId)).thenReturn(Optional.of(specialist));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(appointmentRepository.existsBySpecialistIdAndClientId(specialistId, clientId)).thenReturn(false);

        assertThrows(ApiException.class, () -> noteService.createNote(specialistId, clientId,
                new CreateNoteRequest("Name", null, null)));

        verify(noteRepository, never()).save(any());
    }

    // ---------- updateNote ----------

    @Test
    void updateNote_success_updatesAllFields() {
        Note note = existingNote();
        when(noteRepository.findByIdAndSpecialistId(noteId, specialistId)).thenReturn(Optional.of(note));

        NoteResponse response = noteService.updateNote(specialistId, noteId,
                new UpdateNoteRequest("  New name ", " New body ", " http://new.url "));

        assertEquals("New name", note.getNoteName());
        assertEquals("New body", note.getBody());
        assertEquals("http://new.url", note.getAttachmentUrl());
        assertEquals("New name", response.noteName());
    }

    @Test
    void updateNote_nullFields_leftUnchanged() {
        Note note = existingNote();
        when(noteRepository.findByIdAndSpecialistId(noteId, specialistId)).thenReturn(Optional.of(note));

        noteService.updateNote(specialistId, noteId, new UpdateNoteRequest(null, null, null));

        assertEquals("Old name", note.getNoteName());
        assertEquals("Old body", note.getBody());
        assertEquals("http://old.url", note.getAttachmentUrl());
    }

    @Test
    void updateNote_emptyStringBodyAndAttachment_clearsFields() {
        Note note = existingNote();
        when(noteRepository.findByIdAndSpecialistId(noteId, specialistId)).thenReturn(Optional.of(note));

        noteService.updateNote(specialistId, noteId, new UpdateNoteRequest(null, "", "  "));

        assertEquals("Old name", note.getNoteName());
        assertNull(note.getBody());
        assertNull(note.getAttachmentUrl());
    }

    @Test
    void updateNote_blankName_throwsAndKeepsOldName() {
        Note note = existingNote();
        when(noteRepository.findByIdAndSpecialistId(noteId, specialistId)).thenReturn(Optional.of(note));

        assertThrows(ApiException.class, () -> noteService.updateNote(specialistId, noteId,
                new UpdateNoteRequest("   ", null, null)));

        assertEquals("Old name", note.getNoteName());
    }

    @Test
    void updateNote_notFoundOrForeign_throws() {
        when(noteRepository.findByIdAndSpecialistId(noteId, specialistId)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> noteService.updateNote(specialistId, noteId,
                new UpdateNoteRequest("Name", null, null)));
    }

    // ---------- deleteNote ----------

    @Test
    void deleteNote_success_deletesNote() {
        Note note = existingNote();
        when(noteRepository.findByIdAndSpecialistId(noteId, specialistId)).thenReturn(Optional.of(note));

        noteService.deleteNote(specialistId, noteId);

        verify(noteRepository).delete(note);
    }

    @Test
    void deleteNote_notFoundOrForeign_throwsAndDeletesNothing() {
        when(noteRepository.findByIdAndSpecialistId(noteId, specialistId)).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> noteService.deleteNote(specialistId, noteId));

        verify(noteRepository, never()).delete(any());
    }
}