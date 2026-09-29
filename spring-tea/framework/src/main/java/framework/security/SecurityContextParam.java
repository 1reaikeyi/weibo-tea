package framework.security;


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
        return SecurityContextParam.getCurrentUserId() == null ? null : SecurityContextParam.getCurrentUserId();
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名，如果未登录返回null
     */
    public static String getCurrentUsername() {
        return SecurityContextParam.getCurrentUsername() == null ? null : SecurityContextParam.getCurrentUsername();
    }


}