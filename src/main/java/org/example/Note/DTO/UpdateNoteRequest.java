package org.example.Note.DTO;

import jakarta.validation.constraints.Size;

public record UpdateNoteRequest(
        @Size(max = 100) String noteName,
        @Size(max = 500) String body,
        @Size(max = 500) String attachmentUrl
) { }
