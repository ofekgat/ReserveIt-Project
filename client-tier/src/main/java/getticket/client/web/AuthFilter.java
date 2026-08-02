package getticket.client.web;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Same login gate as the server tier's AuthFilter: pages other than
 * login.xhtml require a signed-in user. Checks the raw HttpSession
 * attribute UserSessionBean.login()/register() set, since this filter
 * runs before JSF would lazily create the bean.
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        boolean loggedIn = session != null && session.getAttribute(UserSessionBean.SESSION_UID_ATTRIBUTE) != null;

        if (loggedIn) {
            chain.doFilter(request, response);
        } else {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.xhtml");
        }
    }

    @Override
    public void destroy() {
    }
}
