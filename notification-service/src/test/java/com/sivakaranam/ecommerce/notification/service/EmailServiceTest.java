package com.sivakaranam.ecommerce.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, "no-reply@ecommerce.test");
    }

    @Test
    void send_withRecipient_sendsMailWithExpectedFields() {
        emailService.send("customer@example.com", "Order confirmed", "Thanks!");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sent = captor.getValue();
        assertThat(sent.getTo()).containsExactly("customer@example.com");
        assertThat(sent.getSubject()).isEqualTo("Order confirmed");
        assertThat(sent.getFrom()).isEqualTo("no-reply@ecommerce.test");
    }

    @Test
    void send_withoutRecipient_doesNotCallMailSender() {
        emailService.send(null, "Order confirmed", "Thanks!");

        verifyNoInteractions(mailSender);
    }
}
