package org.example.Client.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.Client.DTO.ClientResponse;
import org.example.Client.DTO.CreateClientRequest;
import org.example.Client.Service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{slug}")
public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping("/check-email")
    public ResponseEntity<EmailCheckResponse> checkEmail(
            @RequestParam
            @NotBlank
            @Email
            @Size(max = 100)
            String email
    ) {
        boolean exists = clientService.emailExists(email);
        return ResponseEntity.ok(new EmailCheckResponse(exists));
    }

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            @Valid @RequestBody CreateClientRequest request
    ) {
        ClientResponse response = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public record EmailCheckResponse(boolean exists) {}
}
