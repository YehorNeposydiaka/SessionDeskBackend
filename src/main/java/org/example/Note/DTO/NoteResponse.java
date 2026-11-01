package org.example.Note.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoteResponse(UUID id, UUID clientId, String noteName, String body,
                           String attachmentUrl, LocalDateTime createdAt, LocalDateTime updatedAt) { }