package co.featureflags.spring.demo.traffic;

import co.featureflags.server.exterior.FFCClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SplittingController {

    private final FFCClient client;

    public SplittingController(FFCClient client) {
        this.client = client;
    }

    @GetMapping("random-split")
    public ResponseEntity<String> randomSplit() {

        final Map<String, String> resultMap = new HashMap<String, String>() {{
            put("v1", "v1-result");
            put("v2", "v2-result");
            put("fallback", "fallback-result");
        }};

        String variation = client.variation("random-split-feature", "fallback");
        return ResponseEntity.ok(resultMap.get(variation));
    }
}
