package co.featureflags.spring;

import co.featureflags.server.exterior.FFCClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@EnableConfigurationProperties
@ConditionalOnBean(FFCClient.class)
public class FeatureFlagWebConfiguration {

    @Bean
    public FilterRegistrationBean<FFCUserFilter> ffcUserFilter(FFCClient client) {
        FilterRegistrationBean<FFCUserFilter> filter = new FilterRegistrationBean<>();
        filter.setFilter(new FFCUserFilter(client));
        filter.addUrlPatterns("/*");
        filter.setOrder(Ordered.LOWEST_PRECEDENCE);
        return filter;
    }

    @Bean
    public FeatureFlagHandler featureFlagHandler(FFCClient client) {
        return new FeatureFlagHandler(client);
    }

    @Bean
    public FeatureFlagConfig featureFlagConfig(FeatureFlagHandler handler) {
        return new FeatureFlagConfig(handler);
    }

}
