package fi.hsl.jore4.hastus.config

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy

@Configuration
@EnableWebSecurity
class WebSecurityConfig : WebSecurityConfigurerAdapter() {

    override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .and()
            .csrf().disable()
            .cors().and()
            .authorizeRequests()
            .antMatchers(
                HttpMethod.GET,
                "/actuator/health"
            ).permitAll()
            .antMatchers(
                HttpMethod.POST,
                "/export/routes"
            )
            .permitAll()
            .anyRequest().denyAll()
    }
}
