/*
package ru.minikhanov.cloud_storage.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.minikhanov.cloud_storage.models.security.User;

import javax.sql.DataSource;

@EnableWebSecurity(debug = true)
//@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private DataSource dataSource;







    */
/*@Bean
    public PasswordEncoder encoder() {
        return NoOpPasswordEncoder.getInstance();
        //return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        *//*
*/
/*PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;*//*
*/
/*
    }*//*


    */
/*@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        System.out.println(encoder().encode("admin"));
        System.out.println(encoder().encode("admin"));
        System.out.println(encoder().encode("admin"));

        System.out.println(encoder().encode("user"));
        auth.inMemoryAuthentication()
                //.passwordEncoder(encoder())
                //.dataSource(dataSource)
                .withUser("admin").password("admin").roles("admin")
                .and()
                .withUser("user").password("user").roles("user")
                *//*
*/
/*.usersByUsernameQuery(
                        "select u.login, u.password, enabled from storage_users u left join role_table r on r.id=u.id_role where u.login=?")
                .authoritiesByUsernameQuery(
                        "select u.login, r.role from storage_users u left join role_table r on r.id=u.id_role where login=?")*//*
*/
/*;
    }*//*


    */
/*@Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().and()
        //http.formLogin().and()
                .authorizeRequests().antMatchers(HttpMethod.POST, "/file").hasRole("admin")
                .antMatchers(HttpMethod.GET, "/file").hasRole("user")
                .and()
                .csrf().disable();
    }*//*

}
*/
