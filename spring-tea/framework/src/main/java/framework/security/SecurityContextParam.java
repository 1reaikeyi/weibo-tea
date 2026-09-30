package framework.security;


import common.constant.JwtConstant;
import framework.bo.UserBO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security 用户信息工具类
 *
 */
public class SecurityContextParam {

    /**
     * 获取当前登录用户的ID
     *
     * @return 用户ID，如果未登录返回null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 判断是否存在认证信息，并且认证通过
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        // 获取主体对象，也就是我们之前存入的 LoginUser
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserBO userBO) {
            return userBO.getId();
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名，如果未登录返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 判断是否存在认证信息，并且认证通过
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        // 获取主体对象，也就是我们之前存入的 LoginUser
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserBO userBO) {
            return userBO.getUsername();
        }
        return null;
    }


}