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
                        // 0. Archivos públicos y recursos estáticos
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/login", "/signup", "/activate", "/activation/**", "/css/**", "/js/**", "/error/**").permitAll()
                        .requestMatchers("/assistant/ask", "/api/test/**", "/api/assistant/**").permitAll()

                        // 1. Panel de Administración (Solo ADMIN)
                        .requestMatchers("/users/**", "/admin/**", "/dev/**", "/api/notifications/dev/**").hasRole("ADMIN")

                        // 2. Gestión de Estudiantes, Profesores y Aulas (ADMIN crea/edita, ADMIN y TEACHER leen)
                        // 2. Gestión de Estudiantes, Profesores y Aulas (ADMIN crea/edita, ADMIN y TEACHER leen)
                        .requestMatchers("/students/new", "/students/edit/**", "/students/*/report-card",
                                "/teachers/new", "/teachers/edit/**", "/teachers/**",
                                "/classrooms/new", "/classrooms/edit/**", "/classrooms/delete/**")
                        .hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/students/**", "/teachers/**", "/classrooms/**").hasRole("ADMIN") // <--- Corregido el '/' aquí
                        .requestMatchers(HttpMethod.GET, "/students/**", "/teachers/**", "/classrooms/**").hasAnyRole("ADMIN", "TEACHER")

                        // 3. Cursos y Exámenes (ADMIN y TEACHER gestionan, todos autenticados leen)
                        .requestMatchers("/courses/new", "/courses/edit/**", "/courses/*/grades/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.POST, "/courses/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.GET, "/courses/**").authenticated()

                        .requestMatchers(HttpMethod.GET, "/exams/*/paper/view", "/exams/*/paper/download").authenticated()
                        .requestMatchers("/exams/**").hasAnyRole("ADMIN", "TEACHER")

                        // 4. Quizzes (Estudiantes pueden realizarlos, Admin/Teacher gestionarlos)
                        .requestMatchers("/quizzes/*/take", "/quizzes/*/submit", "/quizzes/*/result").hasAnyRole("STUDENT", "ADMIN", "TEACHER")
                        .requestMatchers("/my-quizzes", "/quizzes/**").authenticated()

                        // 5. Inscripciones y Asistencias
                        .requestMatchers(HttpMethod.POST, "/enrollments/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/enrollments/**").authenticated()
                        .requestMatchers("/attendances/my", "/attendances/student/**").authenticated()
                        .requestMatchers("/attendances/**").hasAnyRole("ADMIN", "TEACHER")
                        .requestMatchers("/export/**").hasAnyRole("ADMIN", "TEACHER")

                        // 6. Horarios / Timetable
                        .requestMatchers("/schedules/new", "/schedules/edit/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/schedules/**").hasRole("ADMIN")
                        .requestMatchers("/schedules/**").authenticated()

                        // 7. Módulo de Padres (PARENT, ADMIN, TEACHER)
                        .requestMatchers("/parent/**").hasAnyRole("PARENT", "ADMIN", "TEACHER")

                        // 8. Pagos y Finanzas (Admin administra, Parent ve lo suyo)
                        .requestMatchers("/admin/fees/**").hasRole("ADMIN")
                        .requestMatchers("/parent/fees/**").hasAnyRole("PARENT", "ADMIN")

                        // 9. Calendario (ADMIN edita/crea, todos autenticados leen)
                        .requestMatchers(HttpMethod.POST, "/calendar/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/calendar/**").hasRole("ADMIN")
                        .requestMatchers("/calendar/**").authenticated()
                        // 10. Dashboard principal (ADMIN y TEACHER)
                        .requestMatchers("/dashboard").hasAnyRole("ADMIN", "TEACHER")

                        // Cualquier otra petición requiere autenticación
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
                            // Redirección inteligente según el rol al iniciar sesión
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