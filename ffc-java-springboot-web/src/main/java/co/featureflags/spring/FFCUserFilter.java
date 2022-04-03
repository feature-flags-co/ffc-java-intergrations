package co.featureflags.spring;

import co.featureflags.commons.model.FFCUser;
import co.featureflags.commons.model.UserTag;
import co.featureflags.server.exterior.FFCClient;
import co.featureflags.server.integrations.FFCUserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FFCUserFilter implements Filter {

    private final static Logger LOG = LoggerFactory.getLogger(FFCUserFilter.class);

    private final FFCClient client;

    public FFCUserFilter(FFCClient client) {
        this.client = client;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        FFCUser user;
        try {
            user = captureUser(httpServletRequest);
            FFCUserContextHolder.setCurrentUser(user, true);
            LOG.debug("capture {}", user);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            user = FFCUserContextHolder.getCurrentUser();
            LOG.debug("remove {}", user);
            FFCUserContextHolder.remove();
        }
    }

    private FFCUser captureUser(HttpServletRequest request) {
        Map<UserTag, String> tags = new HashMap<>();
        for (UserTag tag : client.getAllUserTags()) {
            String value = getTagValue(request, tag);
            if (value != null) {
                tags.put(tag, value);
            }
        }
        return FFCUser.of(tags);
    }

    private String getTagValue(HttpServletRequest request, UserTag tag) {
        switch (tag.getSource()) {
            case UserTag.HEADER:
                return request.getHeader(tag.getRequestProperty());
            case UserTag.COOKIE:
                Cookie[] cookies = request.getCookies();
                cookies = cookies == null ? new Cookie[]{} : cookies;
                Cookie res = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(tag.getRequestProperty())).findFirst().orElse(null);
                return res != null ? res.getValue() : null;
            case UserTag.QUERY_STRING:
                return request.getParameter(tag.getRequestProperty());
            default:
                return null;
        }
    }
}
