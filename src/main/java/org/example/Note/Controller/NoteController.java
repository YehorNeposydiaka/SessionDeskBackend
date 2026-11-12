package org.example.Note.Controller;

import jakarta.validation.Valid;
import org.example.Note.DTO.CreateNoteRequest;
import org.example.Note.DTO.NoteResponse;
import org.example.Note.DTO.UpdateNoteRequest;
import org.example.Note.Service.NoteService;
import org.example.Security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService){
        this.noteService = noteService;
    }

    @PostMapping("/clients/{clientId}/notes")
    public ResponseEntity<NoteResponse> createNote(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable UUID clientId,
                                                   @Valid @RequestBody CreateNoteRequest request){
        NoteResponse response = noteService.createNote(principal.getId(), clientId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/notes/{noteId}")
    public ResponseEntity<NoteResponse> updateNote(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable UUID noteId,
                                                   @Valid @RequestBody UpdateNoteRequest request){
        NoteResponse response = noteService.updateNote(principal.getId(), noteId, request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@AuthenticationPrincipal UserPrincipal principal,
                                           @PathVariable UUID noteId){
        noteService.deleteNote(principal.getId(), noteId);

        return ResponseEntity.noContent().build();
    }
}