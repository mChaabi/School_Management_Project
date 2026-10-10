package com.example.School.Management.System.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // 1. Public
                        .requestMatchers("/login", "/signup", "/css/**", "/js/**", "/error/**").permitAll()

                        // 2. Admin only
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        // Students / Teachers / Classrooms: ADMIN writes, TEACHER only views
                        .requestMatchers("/students/new", "/students/edit/**","/students/{id}",
                                "/teachers/new", "/teachers/edit/**","/teachers/**",
                                "/classrooms/new", "/classrooms/edit/**", "/classrooms/delete/**")

                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/students/**", "/teachers/**", "/classrooms/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/students/**", "/teachers/**", "/classrooms/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        // 3. Courses: everyone views, staff writes
                        .requestMatchers("/courses/new", "/courses/edit/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.POST, "/courses/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.GET, "/courses/**").authenticated()
                        .requestMatchers("/courses/*/grades/**").hasAnyRole("ADMIN", "TEACHER")

                        // 4. Enrollments: staff writes, everyone reads (filtered in the service)
                        .requestMatchers("/enrollments/new", "/enrollments/edit/**",
                                "/enrollments/update/**", "/enrollments/delete/**")
                        .hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.POST, "/enrollments/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.GET, "/enrollments/**").authenticated()

                        // 5. Attendance: "my" and "student/{id}" are for everyone (filtered), the rest is staff only
                        .requestMatchers("/attendances/my", "/attendances/student/**").authenticated()
                        .requestMatchers("/attendances/**").hasAnyRole("ADMIN", "TEACHER")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((req, res, auth) -> {
                            boolean staff = auth.getAuthorities().stream().anyMatch(a ->
                                    a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_TEACHER"));
                            res.sendRedirect(req.getContextPath() + (staff ? "/dashboard" : "/enrollments"));
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}