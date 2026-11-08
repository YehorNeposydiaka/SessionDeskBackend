package org.example.Client.Repository;

import org.example.Client.Entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<Client> findByEmailIgnoreCase(String email);
}