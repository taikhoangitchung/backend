package app.controller;

import app.service.EmailService;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class EmailController {
    @Autowired
    private EmailService emailService;
    private final MessageHelper messageHelper;

    @GetMapping("/send-email")
    public ResponseEntity<?> sendEmail(@RequestParam String to,
                                       @RequestParam String subject,
                                       @RequestParam String text) {
        emailService.sendEmail(to, subject, text);
        return ResponseEntity.ok(messageHelper.get("send.mail.success"));
    }
}