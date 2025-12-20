package com.estc.mediatech_2.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String code) {
        String subject = "Vérification de compte Admin";
        // Note: Update the domain/port if running on a different environment
        String verificationUrl = "http://localhost:8090/api/auth/verify?code=" + code;

        String message = "Bonjour,\n\n" +
                "Veuillez vérifier votre compte administrateur en cliquant sur le lien ci-dessous :\n" +
                verificationUrl + "\n\n" +
                "Cordialement,\n" +
                "L'équipe MediaTech";

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(message);
        // Ensure you configure spring.mail.username in application.properties

        try {
            mailSender.send(email);
            System.out.println("Email envoyé à " + toEmail);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
}
