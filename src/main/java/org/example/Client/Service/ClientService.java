package org.example.Client.Service;

import org.example.Appointment.Entity.SpecialistClient;
import org.example.Appointment.Repository.SpecialistClientRepository;
import org.springframework.transaction.annotation.Transactional;
import org.example.Client.DTO.ClientResponse;
import org.example.Client.DTO.CreateClientRequest;
import org.example.Client.Entity.Client;
import org.example.Client.Repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final SpecialistClientRepository specialistClientRepository;

    public ClientService(ClientRepository clientRepository,
                         SpecialistClientRepository specialistClientRepository) {
        this.clientRepository = clientRepository;
        this.specialistClientRepository = specialistClientRepository;
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return clientRepository.existsByEmailIgnoreCase(normalizeEmail(email));
    }

    @Transactional(readOnly = true)
    public UUID getIdByEmail(String email) {
        return clientRepository.findByEmailIgnoreCase(email).get().getId();
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

        return toClientResponse(clientRepository.save(client));
    }

    @Transactional(readOnly = true)
    public List<ClientResponse> getAllSpecialistClients(UUID specialistId){
        List<SpecialistClient> clients = specialistClientRepository.findAllBySpecialist_Id(specialistId);
        List<Client> result = new ArrayList<>();
        for(SpecialistClient client : clients){
            result.add(client.getClient());
        }
        return result.stream().map(this::toClientResponse).toList();
    }
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ClientResponse toClientResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getEmail(),
                client.getName(),
                client.getNickname()
        );
    }
}
