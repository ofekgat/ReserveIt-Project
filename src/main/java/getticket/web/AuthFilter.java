package getticket.web;

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
 * Enforces the design doc's login gate (section 8, step 2): pages other than
 * login.xhtml require a signed-in user. Checks the raw HttpSession attribute
 * UserSessionBean.login()/register() set, rather than looking up the JSF bean
 * itself, since this filter runs before JSF would lazily create it.
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
