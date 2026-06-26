package com.email.writer.app;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins="*")
public class EmailGeneratorController {

    private final EmailGeneratorService emailGeneratorService;
    private final JavaMailSender mailSender;

    public EmailGeneratorController(EmailGeneratorService emailGeneratorService, JavaMailSender mailSender) {
        this.emailGeneratorService = emailGeneratorService;
        this.mailSender = mailSender;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateEmail(@RequestBody EmailRequest emailRequest) {
        if (emailRequest.getEmailContent() == null || emailRequest.getEmailContent().isBlank()) {
            return ResponseEntity.badRequest().body(
                    "emailContent is required. Example: {\"emailContent\":\"Hello\",\"tone\":\"friendly\"}"
            );
        }
        String reply = emailGeneratorService.generateEmailReply(emailRequest);
        return ResponseEntity.ok(reply);
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendEmail(@RequestBody SendRequest sendRequest) {
        Map<String, String> response = new HashMap<>();
        try {
            String fromEmail = sendRequest.getFrom();
            if (fromEmail == null || fromEmail.isBlank()) {
                fromEmail = "agarwalshikha0709@gmail.com";
            }
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setReplyTo(fromEmail);
            message.setTo(sendRequest.getTo());
            message.setSubject(sendRequest.getSubject());
            message.setText(sendRequest.getBody());

            mailSender.send(message);
            response.put("status", "success");
            response.put("message", "Email sent successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
