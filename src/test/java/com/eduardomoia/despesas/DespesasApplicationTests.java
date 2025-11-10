package com.eduardomoia.despesas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "security.jwt.secret=test-secret",
        "security.jwt.expiration-ms=3600000"
})
class DespesasApplicationTests {

    @Test
    void contextLoads() {
    }

}
