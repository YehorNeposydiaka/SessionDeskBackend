package org.example.Note.Service;

import jakarta.transaction.Transactional;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final SpecialistRepository specialistRepository;
    private final ClientRepository clientRepository;
    private final AppointmentRepository appointmentRepository;

    public NoteService(NoteRepository noteRepository,
                       SpecialistRepository specialistRepository,
                       ClientRepository clientRepository,
                       AppointmentRepository appointmentRepository) {
        this.noteRepository = noteRepository;
        this.specialistRepository = specialistRepository;
        this.clientRepository = clientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional
    public NoteResponse createNote(UUID specialistId, UUID clientId, CreateNoteRequest request) {
        Specialist specialist = specialistRepository.findById(specialistId)
                .orElseThrow(() -> new ApiException("Specialist not found", HttpStatus.NOT_FOUND));
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ApiException("Client not found", HttpStatus.NOT_FOUND));

        if (!appointmentRepository.existsBySpecialistIdAndClientId(specialistId, clientId))
            throw new ApiException("Client not found", HttpStatus.NOT_FOUND);

        String name = request.noteName().trim();
        if (name.isEmpty())
            throw new ApiException("Note name must not be blank", HttpStatus.BAD_REQUEST);

        Note note = Note.builder()
                .specialist(specialist)
                .client(client)
                .noteName(name)
                .body(normalizeText(request.body()))
                .attachmentUrl(normalizeText(request.attachmentUrl()))
                .build();

        return toResponse(noteRepository.save(note));
    }

    @Transactional
    public NoteResponse updateNote(UUID specialistId, UUID noteId, UpdateNoteRequest request) {
        Note note = noteRepository.findByIdAndSpecialistId(noteId, specialistId)
                .orElseThrow(() -> new ApiException("Note not found", HttpStatus.NOT_FOUND));

        if (request.noteName() != null) {
            String name = request.noteName().trim();
            if (name.isEmpty())
                throw new ApiException("Note name must not be blank", HttpStatus.BAD_REQUEST);
            note.setNoteName(name);
        }
        if (request.body() != null)
            note.setBody(normalizeText(request.body()));
        if (request.attachmentUrl() != null)
            note.setAttachmentUrl(normalizeText(request.attachmentUrl()));

        return toResponse(note);
    }

    @Transactional
    public void deleteNote(UUID specialistId, UUID noteId) {
        Note note = noteRepository.findByIdAndSpecialistId(noteId, specialistId)
                .orElseThrow(() -> new ApiException("Note not found", HttpStatus.NOT_FOUND));
        noteRepository.delete(note);
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getClient().getId(),
                note.getNoteName(),
                note.getBody(),
                note.getAttachmentUrl(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}