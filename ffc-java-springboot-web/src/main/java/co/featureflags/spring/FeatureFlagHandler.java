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
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Map;

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
        if (user == null) {
            user = FFCUserUtils.captureUser(request, client);
            if (user == null) {
                return true;
            }
            FFCUserContextHolder.setCurrentUser(user, true);
        }
        LOG.debug("current user: {}", user);

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
                    if (enabled) {
                        LOG.info("FFC JAVA Integration: User {}, Feature Flag {}, Flag Value {}, go to {}", user.getKey(), featureFlagKey, enabled, request.getRequestURI());
                        return true;
                    } else if (StringUtils.isEmpty(fallback)) {
                        return false;
                    } else {
                        fallback = rebuildUrl(request, fallback);
                        LOG.info("FFC JAVA Integration: User {}, Feature Flag {}, Flag Value {}, redirect to {}", user.getKey(), featureFlagKey, false, fallback);
                        request.getRequestDispatcher(fallback).forward(request, response);
                        return false;
                    }
                } else {
                    String variation = client.variation(featureFlagKey, "");
                    if (StringUtils.isNotEmpty(variation) && flagValue.equals(variation)) {
                        LOG.info("FFC JAVA Integration: User {}, Feature Flag {}, Flag Value {}, go to {}", user.getKey(), featureFlagKey, variation, request.getRequestURI());
                        return true;
                    }
                    for (RouteMapping routeMapping : others) {
                        if (routeMapping.value().equals(variation)) {
                            String url = rebuildUrl(request, routeMapping.path());
                            LOG.info("FFC JAVA Integration: User {}, Feature Flag {}, Flag Value {}, redirect to {}", user.getKey(), featureFlagKey, variation, url);
                            request.getRequestDispatcher(url).forward(request, response);
                            return false;
                        }
                    }
                    if (StringUtils.isNotEmpty(fallback)) {
                        fallback = rebuildUrl(request, fallback);
                        LOG.info("FFC JAVA Integration: User {}, Feature Flag {}, Flag Value {}, redirect to {}", user.getKey(), featureFlagKey, variation, fallback);
                        request.getRequestDispatcher(fallback).forward(request, response);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private String rebuildUrl(HttpServletRequest request, String forwordUrl) {
        String url = forwordUrl;
        Map<String, String> pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null) {
            for (Map.Entry<String, String> entry : pathVariables.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                url = url.replaceAll("(\\{" + key + "})", value);
            }
        }
        return url;
    }

}
