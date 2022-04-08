package co.featureflags.spring.demo.traffic;

import co.featureflags.spring.demo.TestRequestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SplittingControllerTest {

    @LocalServerPort
    private int randomServerPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void randomSplit() {
        final String url = String.format("http://localhost:%s/random-split", randomServerPort);

        // requests from a thousand different users
        int requestCount = 1000;
        Stream<HttpEntity<String>> oneThousandRequests = Stream
                .iterate(1, i -> i + 1)
                .limit(requestCount)
                .map(i -> TestRequestFactory.New(String.format("random-user-%d", i)));

        final HashMap<String, Integer> counter = new HashMap<>();

        oneThousandRequests.forEach(request -> {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            String value = response.getBody();
            Integer count = counter.getOrDefault(value, 0);
            counter.put(value, count + 1);
        });

        // all variation call should be successful
        assertThat(counter.keySet()).doesNotContain("fallback-result");

        // print real number
        counter.keySet().forEach(key -> {
            String msg = String.format("%s: %d", key, counter.get(key));
            System.out.println(msg);
        });

        // v2-result : v1-result should close to 1:4
        float proportion = (float) counter.get("v2-result") / counter.get("v1-result");

        // close to 0.25 within 0.03 offset
        assertThat(proportion).isCloseTo(0.25f, within(0.03f));
    }
}