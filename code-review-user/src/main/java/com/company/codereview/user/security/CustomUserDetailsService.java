package com.company.codereview.user.security;

import com.company.codereview.user.entity.User;
import com.company.codereview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义用户详情服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
        
        if (!user.getActive()) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }
        
        log.debug("加载用户详情: {}", username);
        return new CustomUserPrincipal(user);
    }
    
    /**
     * 自定义用户主体类
     */
    public static class CustomUserPrincipal implements UserDetails {
        
        private final User user;
        
        public CustomUserPrincipal(User user) {
            this.user = user;
        }
        
        public User getUser() {
            return user;
        }
        
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().getAuthority())
            );
        }
        
        @Override
        public String getPassword() {
            return user.getPassword();
        }
        
        @Override
        public String getUsername() {
            return user.getUsername();
        }
        
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }
        
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }
        
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }
        
        @Override
        public boolean isEnabled() {
            return user.getActive();
        }
    }
}