package co.featureflags.spring;

import co.featureflags.commons.model.FFCUser;
import co.featureflags.server.exterior.FFCClient;
import co.featureflags.server.integrations.FFCUserContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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
            user = FFCUserUtils.captureUser(httpServletRequest, client);
            if (user != null) {
                LOG.debug("capture {}", user);
                FFCUserContextHolder.setCurrentUser(user, true);
            }
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            user = FFCUserContextHolder.getCurrentUser();
            LOG.debug("remove {}", user);
            FFCUserContextHolder.remove();
        }
    }
}
