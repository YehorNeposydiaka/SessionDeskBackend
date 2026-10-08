package org.example.Security;

import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final SpecialistRepository specialistRepository;

    public CustomUserDetailsService(SpecialistRepository specialistRepository) {
        this.specialistRepository = specialistRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        Specialist specialist = specialistRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new UserPrincipal(specialist);
    }

    public UserDetails loadById(UUID id) {
        Specialist specialist = specialistRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));
        return new UserPrincipal(specialist);
    }
}