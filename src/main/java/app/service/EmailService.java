package app.service;

import app.dto.EmailRequest;
import app.dto.SendAnnounceRequest;
import app.dto.SendCodeRequest;
import app.entity.PasswordRecoverToken;
import app.entity.User;
import app.exception.EmailException;
import app.exception.NotFoundException;
import app.repository.TokenRepository;
import app.repository.UserRepository;
import app.util.MessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Autowired
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    @Autowired
    private final JavaMailSender mailSender;
    private final MessageHelper messageHelper;

    public void sendToRecoverPassword(EmailRequest request) {
        try {
            User user = userRepository.findByEmail(request.getTo())
                    .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));

            if (tokenRepository.existsByUser(user)) {
                PasswordRecoverToken resetToken = tokenRepository.findByUser(user);
                System.err.println(resetToken.getId());
                tokenRepository.deleteById(resetToken.getId());
            }

            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);
            PasswordRecoverToken resetToken = new PasswordRecoverToken();
            resetToken.setToken(request.getToken());
            resetToken.setUser(user);
            resetToken.setExpiryDate(expiryDate);

            tokenRepository.save(resetToken);

            send(request.getTo(), request.getSubject(), request.getHtml());
        } catch (Exception e) {
            throw new EmailException("Lỗi khi gửi Email: " + e.getMessage());
        }
    }

    public void sendCode(SendCodeRequest request) {
        try {
            send(request.getTo(), request.getSubject(), request.getHtml());
        } catch (Exception e) {
            throw new EmailException("Lỗi khi gửi Code: " + e.getMessage());
        }
    }

    public void sendAnnounce(SendAnnounceRequest request) {
        try {
            userRepository.findByEmail(request.getTo()).orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
            send(request.getTo(), request.getSubject(), request.getHtml());
        } catch (Exception e) {
            throw new EmailException("Lỗi khi gửi thông báo: " + e.getMessage());
        }
    }

    private void send(String to, String subject, String html) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        mailSender.send(mimeMessage);
    }
}
