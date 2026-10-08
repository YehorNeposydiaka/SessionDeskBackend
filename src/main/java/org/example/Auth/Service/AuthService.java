package org.example.Auth.Service;

import org.example.Auth.DTO.AuthResponse;
import org.example.Auth.DTO.LoginRequest;
import org.example.Auth.DTO.RegisterRequest;
import org.example.Security.Entity.RefreshToken;
import org.example.Security.Repository.RefreshTokenRepository;
import org.example.Security.UserPrincipal;
import org.example.Security.jwt.JwtProperties;
import org.example.Security.jwt.JwtTokenProvider;
import org.example.Security.jwt.TokenHashUtil;
import org.example.Specialist.Entity.Specialist;
import org.example.Specialist.Repository.SpecialistRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final SpecialistRepository specialistRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final long refreshTokenExpirationMs;

    public AuthService(SpecialistRepository specialistRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuthenticationManager authenticationManager,
                       JwtProperties jwtProperties) {
        this.specialistRepository = specialistRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.refreshTokenExpirationMs = jwtProperties.refreshTokenExpirationMs();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (specialistRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        Specialist specialist = Specialist.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .build();
        specialistRepository.save(specialist);

        UserPrincipal principal = new UserPrincipal(specialist);
        return issueTokens(principal);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return issueTokens(principal);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        if (!tokenProvider.validateToken(rawRefreshToken)) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        String hash = TokenHashUtil.hash(rawRefreshToken);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token not recognized"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token is no longer valid");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        UserPrincipal principal = new UserPrincipal(stored.getUser());
        return issueTokens(principal);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        String hash = TokenHashUtil.hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private AuthResponse issueTokens(UserPrincipal principal) {
        String accessToken = tokenProvider.generateAccessToken(principal);
        String rawRefreshToken = tokenProvider.generateRefreshToken(principal);

        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setUser(specialistRepository.getReferenceById(principal.getId()));
        refreshTokenEntity.setTokenHash(TokenHashUtil.hash(rawRefreshToken));
        refreshTokenEntity.setExpiresAt(Instant.now().plusMillis(refreshTokenExpirationMs));
        refreshTokenRepository.save(refreshTokenEntity);

        return new AuthResponse(accessToken, rawRefreshToken);
    }
}