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
 * Gates the admin screens: pages this filter is mapped to require a signed-in
 * user whose Role is ADMIN. Checks the raw HttpSession attributes
 * UserSessionBean.markLoggedIn() sets, same reasoning as AuthFilter — this
 * runs before JSF would lazily create the bean.
 */
public class AdminFilter implements Filter {

    private static final String ADMIN_ROLE = "ADMIN";

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        Object uid = session != null ? session.getAttribute(UserSessionBean.SESSION_UID_ATTRIBUTE) : null;
        Object role = session != null ? session.getAttribute(UserSessionBean.SESSION_ROLE_ATTRIBUTE) : null;

        if (uid == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.xhtml");
        } else if (!ADMIN_ROLE.equalsIgnoreCase(String.valueOf(role))) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/catalog.xhtml");
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
