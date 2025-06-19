package app.controller;

import app.dto.email.EmailRequest;
import app.dto.email.SendAnnounceRequest;
import app.dto.email.SendCodeRequest;
import app.service.EmailService;
import app.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/email")
public class EmailController {
    @Autowired
    private EmailService emailService;
    private final MessageHelper messageHelper;

    @PostMapping("/send-announce")
    public ResponseEntity<?> sendAnnounce(@RequestBody SendAnnounceRequest request) {
        emailService.sendAnnounce(request);
        return ResponseEntity.ok(messageHelper.get("send.mail.success"));
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendToRecoverPassword(@RequestBody EmailRequest request) {
        emailService.sendToRecoverPassword(request);
        return ResponseEntity.ok(messageHelper.get("send.mail.success"));
    }

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestBody SendCodeRequest request) {
        emailService.sendCode(request);
        return ResponseEntity.ok(messageHelper.get("send.mail.success"));
    }
}