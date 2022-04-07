package co.featureflags.spring.demo.greeting;

import co.featureflags.spring.demo.TestRequestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class GreetingControllerTest {

    @LocalServerPort
    private int randomServerPort;

    @Autowired
    private TestRestTemplate restTemplate;

    final String identity = "cn-user-1";
    final String helloMsg = "你好";
    final String hiMsg = "你好 :)";

    @Test
    void hello() {
        final String url = String.format("http://localhost:%s/hello", randomServerPort);
        HttpEntity<String> request = TestRequestFactory.New(identity);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(helloMsg);
    }

    @Test
    void hi() {
        final String url = String.format("http://localhost:%s/hi", randomServerPort);
        HttpEntity<String> request = TestRequestFactory.New(identity);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody())
                .isNotNull()
                .isEqualTo(hiMsg);
    }
}