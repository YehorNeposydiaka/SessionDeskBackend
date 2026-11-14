package org.example.Client.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.Client.DTO.ClientResponse;
import org.example.Client.DTO.CreateClientRequest;
import org.example.Client.Service.ClientService;
import org.example.Security.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/{slug}/check-email")
    public ResponseEntity<EmailCheckResponse> checkEmail(
            @RequestParam
            @NotBlank
            @Email
            @Size(max = 100)
            String email
    ) {
        boolean exists = clientService.emailExists(email);
        UUID clientId = null;
        if(exists)
            clientId =  clientService.getIdByEmail(email);
        return ResponseEntity.ok(new EmailCheckResponse(exists, clientId));
    }

    @PostMapping("/{slug}")
    public ResponseEntity<ClientResponse> createClient(
            @Valid @RequestBody CreateClientRequest request
    ) {
        ClientResponse response = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientResponse>> getAllSpecialistClients(@AuthenticationPrincipal UserPrincipal principal){
        List<ClientResponse> result = clientService.getAllSpecialistClients(principal.getId());

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    public record EmailCheckResponse(boolean exists, UUID clientId) {}
}
