package opm.example.opm.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final StringRedisTemplate redisTemplate;

    // 메서드 파라미터에 수신자(email)와 인증번호(authCode)를 추가합니다.
    public void sendMimeMessage(String email, String authCode) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject("OnePageMe 인증번호 안내");

            Context context = new Context();
            context.setVariable("code", authCode);

            String html = templateEngine.process("mail/auth", context);
            mimeMessageHelper.setText(html, true);

            javaMailSender.send(mimeMessage);
            // Redis에 인증번호 저장 (Key: 이메일, Value: 인증번호, 유효시간: 3분)
            redisTemplate.opsForValue().set(email, authCode, Duration.ofMinutes(3));
            log.info("Redis에 인증번호 저장 완료: {}", email);
        } catch (Exception e) {
            log.error("메일 발송 실패!", e);
            throw new RuntimeException(e);
        }
    }

    // 인증번호 검증 메서드
    public boolean verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(email);
        return savedCode != null && savedCode.equals(code);
    }

}