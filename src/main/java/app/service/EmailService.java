package app.service;

import app.entity.PasswordResetToken;
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

    public void sendToRecoverPassword(String to, String subject, String html, String token) {
        try {
            User user = userRepository.findByEmail(to)
                    .orElseThrow(() -> new NotFoundException(messageHelper.get("user.not.found")));
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(expiryDate);

            tokenRepository.save(resetToken);

            send(to, subject, html);
        } catch (Exception e) {
            throw new EmailException("Lỗi khi gửi Email \n" + e);
        }
    }

    public void sendCode(String to, String subject, String html) {
        try {
            send(to, subject, html);
        } catch (Exception e) {
            throw new EmailException("Lỗi khi gửi Code \n" + e);
        }
    }

    public void sendAnnounce(String to, String subject, String html) {
        try {
            send(to, subject, html);
        } catch (Exception e) {
            throw new EmailException("Lỗi khi gửi thông báo \n" + e);
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
