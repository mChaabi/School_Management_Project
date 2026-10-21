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
                        // 0. Allow public access to REST APIs
                        .requestMatchers("/api/**").permitAll()

                        // 1. Public web pages
                        .requestMatchers("/login", "/signup", "/activate", "/activation/**", "/css/**", "/js/**", "/error/**").permitAll()

                        .requestMatchers("/assistant/ask", "/api/test/**").permitAll()
                        .requestMatchers("/api/assistant/**").permitAll()
                        // 2. Admin only
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        .requestMatchers("/students/new", "/students/edit/**","/students/{id}", "/students/*/report-card",
                                "/teachers/new", "/teachers/edit/**","/teachers/**",
                                "/classrooms/new", "/classrooms/edit/**", "/classrooms/delete/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/students/**", "/teachers/**", "/classrooms/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/students/**", "/teachers/**", "/classrooms/**")
                        .hasAnyRole("ADMIN", "TEACHER")

                        // 3. Courses
                        .requestMatchers("/courses/new", "/courses/edit/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.POST, "/courses/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.GET, "/courses/**").authenticated()
                        .requestMatchers("/courses/*/grades/**").hasAnyRole("ADMIN", "TEACHER")

                        // 4. Enrollments
                        .requestMatchers("/enrollments/new", "/enrollments/edit/**",
                                "/enrollments/update/**", "/enrollments/delete/**")
                        .hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.POST, "/enrollments/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.GET, "/enrollments/**").authenticated()

                        // Timetable
                        .requestMatchers("/schedules/new", "/schedules/edit/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/schedules/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/schedules/**").authenticated()

                        .requestMatchers("/dev/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/notifications/dev/**").hasRole("ADMIN")


                        .requestMatchers("/parent/**").hasRole("PARENT")
                        .requestMatchers("/dashboard").hasAnyRole("ADMIN", "TEACHER")

                        .requestMatchers(HttpMethod.GET, "/exams/*/paper/view", "/exams/*/paper/download").authenticated()
                        .requestMatchers("/quizzes/*/take", "/quizzes/*/submit", "/quizzes/*/result").hasAnyRole("STUDENT", "ADMIN", "TEACHER")
                        .requestMatchers("/my-quizzes", "/quizzes/**").authenticated()

                        .requestMatchers("/parent/**").hasAnyRole("PARENT", "ADMIN", "TEACHER")

                        .requestMatchers(HttpMethod.POST, "/calendar/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/calendar/**").hasRole("ADMIN")
                        .requestMatchers("/calendar/**").authenticated()

                        // 5. Attendance
                        .requestMatchers("/attendances/my", "/attendances/student/**").authenticated()
                        .requestMatchers("/attendances/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/export/**").hasAnyRole("ADMIN", "TEACHER")
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler((req, res, auth) -> {
                            var roles = auth.getAuthorities().stream().map(a -> a.getAuthority()).toList();
                            boolean staff  = roles.contains("ROLE_ADMIN") || roles.contains("ROLE_TEACHER");
                            boolean parent = roles.contains("ROLE_PARENT");
                            String target = staff ? "/dashboard" : parent ? "/parent" : "/enrollments";
                            res.sendRedirect(req.getContextPath() + target);
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