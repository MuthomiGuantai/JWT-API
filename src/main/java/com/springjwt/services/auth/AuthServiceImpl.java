package com.springjwt.services.auth;

import com.springjwt.dto.SignupDTO;
import com.springjwt.dto.UserDTO;
import com.springjwt.entities.PasswordResetToken;
import com.springjwt.entities.User;
import com.springjwt.repositories.PasswordResetTokenRepository;
import com.springjwt.repositories.UserRepository;
import com.springjwt.services.Email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;


    @Autowired
    private EmailService emailService;

    @Autowired
    private JmsTemplate jmsTemplate;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public UserDTO createUser(SignupDTO signupDTO) {
        User user = new User();
        user.setName(signupDTO.getName());
        user.setEmail(signupDTO.getEmail());
        user.setPassword(passwordEncoder.encode(signupDTO.getPassword()));
        String role = signupDTO.getRole() != null ? signupDTO.getRole() : "ROLE_USER";
        user.setRole(role);
        User createdUser = userRepository.save(user);

        String message = String.format("Created user: %s with email: %s", createdUser.getName(), createdUser.getEmail());
        jmsTemplate.convertAndSend("user-queue", message);
        logger.info("Sent message to user-queue: {}", message);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(createdUser.getId());
        userDTO.setEmail(createdUser.getEmail());
        userDTO.setName(createdUser.getName());
        return userDTO;
    }

    @Override
    public String generatePasswordResetToken(String email) {
        User user = userRepository.findFirstByEmail(email);
        if (user == null) {
            return null;
        }
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        tokenRepository.save(resetToken);
        emailService.sendResetPasswordEmail(email, token); // Use the interface
        return token;
    }

    @Override
    public boolean resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token);
        if (resetToken == null || resetToken.isExpired()) {
            return false;
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken); // Invalidate token after use
        return true;
    }
}
