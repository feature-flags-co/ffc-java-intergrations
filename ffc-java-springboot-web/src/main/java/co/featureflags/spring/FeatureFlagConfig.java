package co.featureflags.spring;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FeatureFlagConfig implements WebMvcConfigurer {
    private final FeatureFlagHandler featureFlagHandler;

    public FeatureFlagConfig(FeatureFlagHandler featureFlagHandler) {
        this.featureFlagHandler = featureFlagHandler;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(featureFlagHandler)
                .addPathPatterns("/**")
                .order(Ordered.LOWEST_PRECEDENCE);
    }
}
