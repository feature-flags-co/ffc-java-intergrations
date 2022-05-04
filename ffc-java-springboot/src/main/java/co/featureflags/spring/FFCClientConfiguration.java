package co.featureflags.spring;

import co.featureflags.server.FFCClientImp;
import co.featureflags.server.FFCConfig;
import co.featureflags.server.Factory;
import co.featureflags.server.InsightProcessorBuilder;
import co.featureflags.server.StreamingBuilder;
import co.featureflags.server.exterior.FFCClient;
import co.featureflags.server.exterior.HttpConfigurationBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
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
        HttpConfigurationBuilder httpConfigFactory = Factory.httpConfigFactory();
        if (properties.getProxyHost() != null && properties.getProxyPort() > 0) {
            httpConfigFactory.httpProxy(properties.getProxyHost(), properties.getProxyPort());
            if (properties.getProxyUser() != null && properties.getProxyPassword() != null) {
                httpConfigFactory.passwordAuthenticator(properties.getProxyUser(), properties.getProxyPassword());
            }
        }

        String streamUri = properties.getStreamUri();
        StreamingBuilder streamingBuilder =
                StringUtils.isBlank(streamUri) ? null : Factory.streamingBuilder().newStreamingURI(streamUri);

        String eventUri = properties.getEventUri();
        InsightProcessorBuilder insightProcessorBuilder =
                StringUtils.isBlank(eventUri) ? null : Factory.insightProcessorFactory().eventUri(eventUri);

        FFCConfig.Builder builder = new FFCConfig.Builder()
                .offline(properties.isOffline())
                .startWaitTime(Duration.ofSeconds(properties.getStartWait()))
                .httpConfigFactory(httpConfigFactory);

        if (streamingBuilder != null) {
            builder.updateProcessorFactory(streamingBuilder);
        }

        if (insightProcessorBuilder != null) {
            builder.insightProcessorFactory(insightProcessorBuilder);
        }
        return new FFCClientImp(properties.getEnvSecret(), builder.build());
    }
}
