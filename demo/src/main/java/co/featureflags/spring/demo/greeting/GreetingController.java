package co.featureflags.spring.demo.greeting;

import co.featureflags.server.exterior.FFCClient;
import co.featureflags.spring.FeatureGate;
import co.featureflags.spring.RouteMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            @RouteMapping(value = "cn", path = "/hello/cn"),
            @RouteMapping(value = "default", path = "/hello/default"),
    }, fallback = "/hello/fallback")
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

    @GetMapping("/hello/default")
    public ResponseEntity<String> helloDefault() {
        return ResponseEntity.ok("default");
    }

    @GetMapping("/hello/fallback")
    public ResponseEntity<String> helloFallback() {
        return ResponseEntity.ok("fallback");
    }

    @GetMapping("v2/hello")
    public ResponseEntity<String> helloV2() {

        final String helloFeature = "hello-feature";

        final Map<String, String> helloMap = new HashMap<String, String>() {{
            put("en", "hello");
            put("fr", "bonjour");
            put("cn", "你好");
        }};

        // fallback value will be returned if any error occurs during evaluating variation
        final String fallback = "fallback-value";

        // variation will be en, fr, cn or fallback-value
        // user will be auto-captured
        String variation = client.variation(helloFeature, fallback);

        // if variation is fallback value
        if (Objects.equals(variation, fallback)) {
            log.error(String.format("some error occurred during evaluating %s's variation", helloFeature));
            return ResponseEntity.ok("fallback response value");
        }

        // get value based on variation
        String response = helloMap.get(variation);
        return ResponseEntity.ok(response);
    }

    @GetMapping("v3/hello/{name}")
    @FeatureGate(feature = "hello-feature", value = "en", others = {
            @RouteMapping(value = "cn", path = "v3/hello/{name}/cn"),
    })
    public ResponseEntity<String> helloV3(@PathVariable String name) {
        return ResponseEntity.ok(String.format("hello, %s", name));
    }

    @GetMapping("v3/hello/{name}/cn")
    public ResponseEntity<String> helloV3InChinese(@PathVariable String name) {
        return ResponseEntity.ok(String.format("你好, %s", name));
    }
}
