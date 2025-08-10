package com.company.codereview.user;

import com.company.codereview.common.response.ResponseResult;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务测试启动类
 */
@SpringBootApplication
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class UserServiceTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceTestApplication.class, args);
    }
    
    @GetMapping("/test")
    public ResponseResult<String> test() {
        return ResponseResult.success("用户服务运行正常！");
    }
    
    @PostMapping("/auth/login")
    public ResponseResult<LoginTestResponse> login(@RequestBody LoginTestRequest request) {
        // 简单的测试登录逻辑
        if ("admin".equals(request.getUsername()) && "password".equals(request.getPassword())) {
            LoginTestResponse response = new LoginTestResponse();
            response.setToken("test-jwt-token-12345");
            response.setUser(new UserTestInfo("admin", "管理员", "ARCHITECT"));
            return ResponseResult.success(response);
        } else {
            return ResponseResult.error("用户名或密码错误");
        }
    }
    
    @PostMapping("/auth/logout")
    public ResponseResult<String> logout() {
        return ResponseResult.success("登出成功");
    }
    
    @GetMapping("/users/profile")
    public ResponseResult<UserTestInfo> getProfile(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token != null && token.startsWith("Bearer ")) {
            UserTestInfo user = new UserTestInfo("admin", "管理员", "ARCHITECT");
            return ResponseResult.success(user);
        } else {
            return ResponseResult.error(401, "未授权");
        }
    }
    
    // 测试用的简单数据类
    public static class LoginTestRequest {
        private String username;
        private String password;
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class LoginTestResponse {
        private String token;
        private UserTestInfo user;
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public UserTestInfo getUser() { return user; }
        public void setUser(UserTestInfo user) { this.user = user; }
    }
    
    public static class UserTestInfo {
        private String username;
        private String realName;
        private String role;
        
        public UserTestInfo() {}
        
        public UserTestInfo(String username, String realName, String role) {
            this.username = username;
            this.realName = realName;
            this.role = role;
        }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}