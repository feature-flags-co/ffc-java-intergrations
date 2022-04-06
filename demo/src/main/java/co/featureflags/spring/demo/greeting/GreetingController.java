package co.featureflags.spring.demo.greeting;

import co.featureflags.server.exterior.FFCClient;
import co.featureflags.spring.FeatureGate;
import co.featureflags.spring.RouteMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
public class GreetingController {

    public static final Logger log = LoggerFactory.getLogger(GreetingController.class);

    private final FFCClient client;

    public GreetingController(FFCClient client) {
        this.client = client;
    }

    @GetMapping("/hello")
    @FeatureGate(feature = "hello-feature", value = "en", others = {
            @RouteMapping(value = "fr", path = "/hello/fr"),
            @RouteMapping(value = "cn", path = "/hello/cn")
    })
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("hello");
    }

    @GetMapping("/hello/fr")
    public ResponseEntity<String> helloInFrench() {
        return ResponseEntity.ok("bonjour");
    }

    @GetMapping("/hello/cn")
    public ResponseEntity<String> helloInChinese() {
        return ResponseEntity.ok("你好");
    }

    @GetMapping("hi")
    public ResponseEntity<String> hi() {

        final String hiFeature = "hi-feature";

        final Map<String, String> hiMap = new HashMap<String, String>() {{
            put("en", "hi, how are you");
            put("fr", "bonjour, ça va");
            put("cn", "你好 :)");
        }};

        // fallback value will be returned if any error occurs during evaluating variation
        final String fallback = "fallback-value";

        // variation will be en, fr, cn or fallback-value
        // user will be auto-captured
        String variation = client.variation(hiFeature, fallback);

        // if variation is fallback value
        if (Objects.equals(variation, fallback)) {
            log.error(String.format("some error occurred during evaluating %s's variation", hiFeature));
            return ResponseEntity.ok("fallback response value");
        }

        // get value based on variation
        String response = hiMap.get(variation);
        return ResponseEntity.ok(response);
    }
}
