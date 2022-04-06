package co.featureflags.spring.demo;

import co.featureflags.server.exterior.FFCClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    FFCClient client;

    @Test
    void contextLoads() {
        assertThat(client).isNotNull();
    }

}
