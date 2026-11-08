package org.example.Client.Service;

import org.springframework.transaction.annotation.Transactional;
import org.example.Client.DTO.ClientResponse;
import org.example.Client.DTO.CreateClientRequest;
import org.example.Client.Entity.Client;
import org.example.Client.Repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return clientRepository.existsByEmailIgnoreCase(normalizeEmail(email));
    }

    @Transactional
    public ClientResponse createClient(CreateClientRequest request) {
        String email = normalizeEmail(request.email());

        if (clientRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Client with this email already exists");
        }

        Client client = Client.builder()
                .email(email)
                .name(request.name())
                .nickname(request.nickname())
                .build();

        return toResponse(clientRepository.save(client));
    }
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getEmail(),
                client.getName(),
                client.getNickname()
        );
    }
}
