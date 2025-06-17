package app.controller;

import app.dto.EmailRequest;
import app.dto.SendEmailResponse;
import app.service.EmailService;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {
    @Autowired
    private EmailService emailService;
    private final MessageHelper messageHelper;

    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody EmailRequest request) {
        emailService.sendEmail(request.getTo(), request.getSubject(), request.getHtml(), request.getToken());
        return ResponseEntity.ok(messageHelper.get("send.mail.success"));
    }
}