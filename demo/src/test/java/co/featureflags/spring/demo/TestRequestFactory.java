package co.featureflags.spring.demo;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public class TestRequestFactory {
    public static HttpEntity<String> New(String identity) {
        // add custom header to request
        HttpHeaders headers = new HttpHeaders();
        headers.add("x-request-userId", identity);
        headers.add("x-request-userName", identity);

        return new HttpEntity<>(headers);
    }
}
