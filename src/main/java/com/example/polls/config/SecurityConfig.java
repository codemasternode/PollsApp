package com.example.polls.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //Służy do sprawdzenia roli użytkowników, dostarcza dane użytkowników
    @Autowired
    private CustomUserDetailsService customUserDetailsService;


    //wyrzucanie błędów do klienta, jeśli ten próbuje dostać się do danych strzeżonych
    @Autowired
    private JWTAuthenticationEntryPoint unautorizedHandler;



    /*
        Odczytanie tokenu JWT z nagłówka żądania,
        Walidacja tokenu,
        Załadowanie danych użytkownika powiązanych z tokenem,
        Zapisanie danych użytkownika w kontekście Springa (SecurityContext),
        Spring używa tych danych do wykonania autoryzacji
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    //Ustawienie Serwisu użytkowników i dodanie encodera hasła
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
            http.
                cors()
                .and().csrf().disable().exceptionHandling()
                .authenticationEntryPoint(unautorizedHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/",
                        "/favicon.ico",
                        "/**/*.png",
                        "/**/*.gif",
                        "/**/*.svg",
                        "/**/*.jpg",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js")
                .permitAll()
                .antMatchers("/auth/**")
                    .permitAll()
                .antMatchers("/user/checkUsernameAvailability","/user/checkEmailAvailability")
                    .permitAll()
                .antMatchers(HttpMethod.GET,"/polls/**","/user/**")
                    .permitAll()
                .anyRequest()
                    .authenticated();
    }
}
