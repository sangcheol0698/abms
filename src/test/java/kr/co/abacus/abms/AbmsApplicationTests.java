package kr.co.abacus.abms;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import kr.co.abacus.abms.support.IntegrationTestBase;

class AbmsApplicationTests extends IntegrationTestBase {

    @Test
    void run() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            AbmsApplication.main(new String[0]);

            mocked.verify(() -> SpringApplication.run(AbmsApplication.class, new String[0]));
        }
    }

}
