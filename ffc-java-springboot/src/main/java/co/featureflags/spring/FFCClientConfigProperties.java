package co.featureflags.spring;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ffc.spring")
public class FFCClientConfigProperties {
    private String envSecret;

    private int startWait = 15;

    private String streamUri = "wss://api.feature-flags.co";

    private String eventUri = "https://api.feature-flags.co";

    private String proxyHost;

    private int proxyPort = -1;

    private String proxyUser;

    private String proxyPassword;

    private boolean offline = false;


}
