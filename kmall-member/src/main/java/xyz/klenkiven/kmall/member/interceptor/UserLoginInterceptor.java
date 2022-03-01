package xyz.klenkiven.kmall.member.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.klenkiven.kmall.common.constant.AuthConstant;
import xyz.klenkiven.kmall.common.to.UserLoginTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Valid User Login Info
 * @author klenkiven
 */
@Component
public class UserLoginInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<UserLoginTO> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // If FEIGN call, Pass
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/member/**", requestURI);
        if (match) { return true; }

        // Do Interceptor for Login
        UserLoginTO userInfo = (UserLoginTO) request.getSession().getAttribute(AuthConstant.LOGIN_USER);
        if (userInfo != null) {
            loginUser.set(userInfo);
            return true;
        }
        response.sendRedirect("http://auth.kmall.com/login.html");
        return false;
    }
}
