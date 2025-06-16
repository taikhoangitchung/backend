package app.controller;

import app.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/send-email")
    public ResponseEntity<?> sendEmail() {
        emailService.sendEmail(
                "datletuan110621@gmail.com",
                "Test Subject",
                "This is a test email sent from Spring Boot!"
        );
        return ResponseEntity.ok("Email sent successfully!");
    }
}