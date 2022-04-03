package co.featureflags.spring;

import co.featureflags.commons.model.FFCUser;
import co.featureflags.server.exterior.FFCClient;
import co.featureflags.server.integrations.FFCUserContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class FeatureFlagHandler implements HandlerInterceptor {

    private final FFCClient client;

    private final static Logger LOG = LoggerFactory.getLogger(FeatureFlagHandler.class);

    public FeatureFlagHandler(FFCClient client) {
        this.client = client;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        FFCUser user = FFCUserContextHolder.getCurrentUser();
        LOG.debug("current user: {}", user);
        if (user == null) {
            return true;
        }
        Method method = null;
        if (handler instanceof HandlerMethod) {
            method = ((HandlerMethod) handler).getMethod();
        }
        if (method != null) {
            FeatureGate gate = method.getAnnotation(FeatureGate.class);
            if (gate != null) {
                String featureFlagKey = gate.feature();
                String flagValue = gate.value();
                String fallback = gate.fallback();
                RouteMapping[] others = gate.others();
                if (StringUtils.isEmpty(flagValue)) {
                    boolean enabled = client.isEnabled(featureFlagKey);
                    if (enabled || StringUtils.isEmpty(fallback)) {
                        return enabled;
                    } else {
                        request.getRequestDispatcher(fallback).forward(request, response);
                        return false;
                    }
                } else {
                    String variation = client.variation(featureFlagKey, null);
                    if (flagValue.equals(variation)) {
                        return true;
                    }
                    for (RouteMapping routeMapping : others) {
                        if (routeMapping.value().equals(variation)) {
                            request.getRequestDispatcher(routeMapping.path()).forward(request, response);
                            return false;
                        }
                    }
                    if (StringUtils.isNotEmpty(fallback)) {
                        request.getRequestDispatcher(fallback).forward(request, response);
                    }
                    return false;
                }
            }
        }
        return true;
    }
}
