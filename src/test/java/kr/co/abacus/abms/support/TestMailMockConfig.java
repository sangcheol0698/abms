package kr.co.abacus.abms.support;

import static org.mockito.Mockito.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import kr.co.abacus.abms.application.auth.outbound.RegistrationLinkSender;

@Profile("test")
@Configuration(proxyBeanMethods = false)
public class TestMailMockConfig {

    @Bean
    RegistrationLinkSender registrationLinkSender() {
        return mock(RegistrationLinkSender.class);
    }

}
