package com.possessor.loginapi.service;

import com.possessor.loginapi.constants.ErrorMessages;
import com.possessor.loginapi.exception.EmailServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;
    
    @Value("${app.email.retry.attempts:3}")
    private int retryAttempts;
    
    @Value("${app.email.timeout.seconds:30}")
    private int timeoutSeconds;
    
    public Mono<Void> sendPasswordResetEmail(String toEmail, String resetToken) {
        if (toEmail == null || toEmail.trim().isEmpty() || resetToken == null || resetToken.trim().isEmpty()) {
            return Mono.error(new EmailServiceException("Invalid email or token parameters"));
        }
        
        return Mono.fromRunnable(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject("Password Reset Request");
                message.setText(buildPasswordResetEmailBody(resetToken));
                
                mailSender.send(message);
                log.info("Password reset email sent to: {}", toEmail);
            } catch (MailException e) {
                log.error("Mail service error sending password reset email to: {}", toEmail, e);
                throw new EmailServiceException(ErrorMessages.EMAIL_SERVICE_UNAVAILABLE_ERROR, e);
            } catch (Exception e) {
                log.error("Unexpected error sending password reset email to: {}", toEmail, e);
                throw new EmailServiceException(ErrorMessages.EMAIL_SEND_FAILED_ERROR, e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .timeout(Duration.ofSeconds(timeoutSeconds))
        .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(5)))
        .then();
    }
    
    public Mono<Void> sendVerificationEmail(String toEmail, String verificationToken) {
        if (toEmail == null || toEmail.trim().isEmpty() || verificationToken == null || verificationToken.trim().isEmpty()) {
            return Mono.error(new EmailServiceException("Invalid email or token parameters"));
        }
        
        return Mono.fromRunnable(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(toEmail);
                message.setSubject("Email Verification");
                message.setText(buildVerificationEmailBody(verificationToken));
                
                mailSender.send(message);
                log.info("Verification email sent to: {}", toEmail);
            } catch (MailException e) {
                log.error("Mail service error sending verification email to: {}", toEmail, e);
                throw new EmailServiceException(ErrorMessages.EMAIL_SERVICE_UNAVAILABLE_ERROR, e);
            } catch (Exception e) {
                log.error("Unexpected error sending verification email to: {}", toEmail, e);
                throw new EmailServiceException(ErrorMessages.EMAIL_SEND_FAILED_ERROR, e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .timeout(Duration.ofSeconds(timeoutSeconds))
        .retryWhen(Retry.backoff(retryAttempts, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(5)))
        .then();
    }
    
    private String buildPasswordResetEmailBody(String resetToken) {
        return String.format("""
            Hello,
            
            You have requested to reset your password. Please click the link below to reset your password:
            
            %s/reset-password?token=%s
            
            This link will expire in 1 hour.
            
            If you did not request this password reset, please ignore this email.
            
            Best regards,
            Login API Team
            """, frontendUrl, resetToken);
    }
    
    private String buildVerificationEmailBody(String verificationToken) {
        return String.format("""
            Hello,
            
            Thank you for registering! Please click the link below to verify your email address:
            
            %s/verify-email?token=%s
            
            If you did not create this account, please ignore this email.
            
            Best regards,
            Login API Team
            """, frontendUrl, verificationToken);
    }
}