package co.featureflags.spring;

import co.featureflags.server.FFCClientImp;
import co.featureflags.server.FFCConfig;
import co.featureflags.server.Factory;
import co.featureflags.server.InsightProcessorBuilder;
import co.featureflags.server.StreamingBuilder;
import co.featureflags.server.exterior.FFCClient;
import co.featureflags.server.exterior.HttpConfigurationBuilder;
import org.apache.commons.codec.binary.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static com.google.common.base.Preconditions.checkArgument;

@Configuration
@EnableConfigurationProperties({FFCClientConfigProperties.class})
public class FFCClientConfiguration {

    @Bean
    public FFCClient ffcClient(FFCClientConfigProperties properties) {
        checkArgument(Base64.isBase64(properties.getEnvSecret()), "envSecret is invalid");
        HttpConfigurationBuilder builder = Factory.httpConfigFactory();
        if (properties.getProxyHost() != null && properties.getProxyPort() > 0) {
            builder.httpProxy(properties.getProxyHost(), properties.getProxyPort());
            if (properties.getProxyUser() != null && properties.getProxyPassword() != null) {
                builder.passwordAuthenticator(properties.getProxyUser(), properties.getProxyPassword());
            }
        }
        StreamingBuilder streamingBuilder = Factory.streamingBuilder()
                .newStreamingURI(properties.getStreamUri());

        InsightProcessorBuilder insightProcessorBuilder = Factory.insightProcessorFactory()
                .eventUri(properties.getEventUri());
        FFCConfig config = new FFCConfig.Builder()
                .offline(properties.isOffline())
                .startWaitTime(Duration.ofSeconds(properties.getStartWait()))
                .httpConfigFactory(builder)
                .updateProcessorFactory(streamingBuilder)
                .insightProcessorFactory(insightProcessorBuilder)
                .build();
        return new FFCClientImp(properties.getEnvSecret(), config);
    }
}
