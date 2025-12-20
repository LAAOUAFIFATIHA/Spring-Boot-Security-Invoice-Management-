package com.estc.mediatech_2.service;

import com.estc.mediatech_2.dao.UserDao;
import com.estc.mediatech_2.dto.UserRequestDto;
import com.estc.mediatech_2.dto.UserResponseDto;
import com.estc.mediatech_2.models.UserEntity;
import com.estc.mediatech_2.dao.ClientDao;
import com.estc.mediatech_2.models.ClientEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final ClientDao clientDao;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public UserResponseDto createUser(UserRequestDto request) {
        // Vérifier que le username n'existe pas déjà
        if (userDao.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Le nom d'utilisateur existe déjà");
        }

        // Valider le rôle
        String role = request.getRole();
        if (role == null || (!role.equals(UserEntity.ROLE_ADMIN)
                && !role.equals(UserEntity.ROLE_VENDEUR)
                && !role.equals(UserEntity.ROLE_CLIENT))) {
            throw new IllegalArgumentException("Rôle invalide. Les rôles valides sont: ADMIN, VENDEUR, CLIENT");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);

        if (UserEntity.ROLE_ADMIN.equals(role)) {
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                throw new IllegalArgumentException("L'email est requis pour les comptes administrateur.");
            }
            user.setEnabled(false);
            String code = java.util.UUID.randomUUID().toString();
            user.setVerificationCode(code);
            emailService.sendVerificationEmail(request.getEmail(), code);
        } else {
            user.setEnabled(true);
        }

        UserEntity saved = userDao.save(user);

        // Création automatique du profil Client
        if (UserEntity.ROLE_CLIENT.equals(role)) {
            ClientEntity client = new ClientEntity();
            client.setNom_client(saved.getUsername());
            client.setPrenom_client("Nouveau");
            client.setTelephone("");
            client.setUser(saved);
            clientDao.save(client);
        }

        return mapToDto(saved);
    }

    @Override
    public UserResponseDto getUser(Long id) {
        return userDao.findById(id).map(this::mapToDto).orElse(null);
    }

    private UserResponseDto mapToDto(UserEntity entity) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId_user(entity.getId_user());
        dto.setUsername(entity.getUsername());
        dto.setRole(entity.getRole());
        return dto;
    }

    @Override
    public boolean verifyUser(String token) {
        java.util.Optional<UserEntity> userOptional = userDao.findByVerificationCode(token);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            if (user.isEnabled()) {
                return true; // Déjà vérifié
            }
            user.setEnabled(true);
            user.setVerificationCode(null);
            userDao.save(user);
            return true;
        }
        return false;
    }
}
