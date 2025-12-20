package com.estc.mediatech_2.config;

import com.estc.mediatech_2.dao.UserDao;
import com.estc.mediatech_2.models.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserDao userDao;
    private final com.estc.mediatech_2.dao.ClientDao clientDao;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_USERNAME = "fatihaa";
    private static final String ADMIN_PASSWORD = "fatiha1233";
    private static final String VENDEUR_USERNAME = "vendeur";
    private static final String VENDEUR_PASSWORD = "vendeur123";
    private static final String CLIENT_USERNAME = "c1";
    private static final String CLIENT_PASSWORD = "1234567";

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            log.info("🔧 Initialisation des utilisateurs par défaut...");
            log.info("🔑 Les mots de passe PLAINS sont :");
            log.info("   - fatihaa : {}", ADMIN_PASSWORD);
            log.info("   - vendeur : {}", VENDEUR_PASSWORD);
            log.info("   - c1      : {}", CLIENT_PASSWORD);
            log.info("==========================================");

            // Mise à jour ou Création
            updateOrCreateUser(ADMIN_USERNAME, ADMIN_PASSWORD, UserEntity.ROLE_ADMIN);
            updateOrCreateUser(VENDEUR_USERNAME, VENDEUR_PASSWORD, UserEntity.ROLE_VENDEUR);
            UserEntity clientUser = updateOrCreateUser(CLIENT_USERNAME, CLIENT_PASSWORD, UserEntity.ROLE_CLIENT);
            createClientProfileIfNotExists(clientUser);

            log.info("✅ Initialisation terminée !");
            log.info("📋 Récapitulatif :");
            log.info("   👤 ADMIN    : {} / {}", ADMIN_USERNAME, ADMIN_PASSWORD);
            log.info("   👤 VENDEUR  : {} / {}", VENDEUR_USERNAME, VENDEUR_PASSWORD);
            log.info("   👤 CLIENT   : {} / {}", CLIENT_USERNAME, CLIENT_PASSWORD);
        };
    }

    private UserEntity updateOrCreateUser(String username, String password, String role) {
        UserEntity user = userDao.findByUsername(username).orElse(new UserEntity());
        user.setUsername(username);

        // ============ LOGS DES HASHs ============
        log.info("   🔓 Mot de passe PLAIN pour {} : {}", username, password);

        String encodedPassword = passwordEncoder.encode(password);
        log.info("   🔐 Hash BCrypt pour {} : {}", username, encodedPassword);
        log.info("   📏 Longueur du hash : {} caractères", encodedPassword.length());

        // Vérifie le format BCrypt
        if (!encodedPassword.startsWith("$2a$")) {
            log.error("   ❌ ERREUR : Hash ne commence pas par $2a$ !");
        }
        // ============ FIN DES LOGS ============

        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setEnabled(true);

        UserEntity saved = userDao.save(user);
        log.info("   ✅ Utilisateur {} sauvegardé en base", username);

        return saved;
    }

    private void createClientProfileIfNotExists(UserEntity user) {
        if (clientDao.findByUser_Username(user.getUsername()).isEmpty()) {
            com.estc.mediatech_2.models.ClientEntity clientSpec = new com.estc.mediatech_2.models.ClientEntity();
            clientSpec.setNom_client("Client Demo");
            clientSpec.setPrenom_client("Test");
            clientSpec.setTelephone("0600000000");
            clientSpec.setUser(user);
            clientDao.save(clientSpec);
            log.info("   ✅ Profil Client créé pour {}", user.getUsername());
        }
    }
}