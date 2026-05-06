package com.cobip.infra.mail;

import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;

    public EmailService(
        ObjectProvider<JavaMailSender> mailSenderProvider,
        @Value("${app.mail.from:}") String from
    ) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
    }

    public void sendVerificationCode(String email, String code) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED);
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(email);
        message.setSubject("COBIP 이메일 인증 코드");
        message.setText("COBIP 이메일 인증 코드입니다.\n\n인증 코드: " + code + "\n\n5분 안에 입력해 주세요.");

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new CustomException(ErrorCode.EMAIL_SEND_FAILED, e);
        }
    }
}
