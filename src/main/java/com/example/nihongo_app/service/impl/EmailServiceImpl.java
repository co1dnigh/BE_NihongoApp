package com.example.nihongo_app.service.impl;

import com.example.nihongo_app.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.rank.mail-from:}")
    private String mailFrom;

    @Override
    @Async
    public void sendRankReminder(String recipient, String displayName, int daysRemaining, int decayExp) {
        if (!mailEnabled || mailFrom == null || mailFrom.isBlank()) {
            log.warn("Rank reminder email skipped because mail is not configured");
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("Rank reminder email skipped because spring.mail.host is not configured");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(recipient);
        message.setSubject("Nhắc nhở: hạng của bạn sắp bị trừ điểm");
        message.setText(String.format(
                "Xin chào %s,\n\nBạn còn %d ngày trước khi hệ thống trừ %d EXP do không học bài. "
                        + "Hãy hoàn thành một bài học để duy trì hạng của bạn.\n\nNihongo App",
                displayName == null || displayName.isBlank() ? "bạn" : displayName,
                daysRemaining,
                decayExp));
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send rank reminder email to {}", recipient, ex);
        }
    }
}