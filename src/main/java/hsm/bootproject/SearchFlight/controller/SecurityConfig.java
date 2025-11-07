package hsm.bootproject.SearchFlight.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            
            // ----------------------------------------------------
            // [수정] 모든 경로에 대해 접근을 허용 (시큐리티 비활성화 효과)
            // ----------------------------------------------------
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // 모든 요청을 로그인/인증 없이 허용
            )
            
            // ----------------------------------------------------
            // 폼 로그인 및 로그아웃 설정을 비활성화 (모든 접근을 허용하므로 필요 없음)
            // ----------------------------------------------------
            .formLogin(form -> form.disable()) // 폼 로그인 비활성화
            .logout(logout -> logout.disable()); // 로그아웃 비활성화

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt 사용
    }
}
