package sv.org.arrupe.pagos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import sv.org.arrupe.pagos.model.Usuario;
import sv.org.arrupe.pagos.repository.UsuarioRepository;
import sv.org.arrupe.pagos.service.UsuarioService;
import sv.org.arrupe.pagos.util.JwtUtil;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UsuarioRepository usuarioRepository) {
        return username -> usuarioRepository.findByCorreo(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler(UsuarioService usuarioService, JwtUtil jwtUtil) {
        return (request, response, authentication) -> {
            DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
            String email = oauthUser.getAttribute("email");
            String name = oauthUser.getAttribute("name");

            Usuario usuario = usuarioService.processOAuthPostLogin(email, name);
            String token = jwtUtil.generateToken(usuario);

            String targetUrl = "http://localhost:5173/oauth/callback?token=" + token;
            response.sendRedirect(targetUrl);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter, AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/api/auth/**",
                    "/api/grados/**",
                    "/oauth2/**",
                    "/login/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                .requestMatchers("/api/dashboard/**").hasRole("ADMINISTRADOR")
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2AuthenticationSuccessHandler)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}