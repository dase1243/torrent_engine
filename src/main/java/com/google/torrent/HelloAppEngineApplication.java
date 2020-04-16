package com.google.torrent;

import com.google.torrent.dao.PrivilegeRepository;
import com.google.torrent.dao.RoleRepository;
import com.google.torrent.dao.UserRepository;
import com.google.torrent.entity.Privilege;
import com.google.torrent.entity.Role;
import com.google.torrent.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
@EnableAspectJAutoProxy
@SpringBootApplication(scanBasePackages = "com.google", exclude = ErrorMvcAutoConfiguration.class)
public class HelloAppEngineApplication {
    public static boolean developEnvironment = false;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(HelloAppEngineApplication.class, args);
    }

    @EventListener
    public void handleContextRefreshEvent(ContextStartedEvent ctxStartEvt) {
        System.out.println("Context Start Event received.");
    }

    @PostConstruct
    public void onApplicationEvent() {

        // == create initial privileges
        final Privilege readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
        final Privilege writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");
        final Privilege passwordPrivilege = createPrivilegeIfNotFound("CHANGE_PASSWORD_PRIVILEGE");

        // == create initial roles
        final List<Privilege> adminPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, writePrivilege, passwordPrivilege));
        final List<Privilege> userPrivileges = new ArrayList<>(Arrays.asList(readPrivilege, passwordPrivilege));
        final Role adminRole = createRoleIfNotFound("ROLE_ADMIN", adminPrivileges);
        final Role userRole = createRoleIfNotFound("ROLE_USER", userPrivileges);

        // == create initial user
        createUserIfNotFound(new User().builder()
                .username("admin")
                .email("admin@admin")
                .name("name")
                .lastName("last name")
                .password(passwordEncoder.encode("admin"))
                .passwordConfirm("admin")
                .roles(new ArrayList<>(Arrays.asList(adminRole)))
                .enabled(true)
                .build()
        );
        createUserIfNotFound(new User().builder()
                .username("user")
                .email("user@admin")
                .name("user")
                .lastName("last name")
                .password(passwordEncoder.encode("password"))
                .passwordConfirm("password")
                .roles(new ArrayList<>(Arrays.asList(userRole)))
                .enabled(true)
                .build()
        );
        createUserIfNotFound(new User().builder()
                .username("dreikaa")
                .email("dreikaa@admin")
                .name("dreikaa")
                .lastName("last name")
                .password(passwordEncoder.encode("12345678"))
                .passwordConfirm("12345678")
                .roles(new ArrayList<>(Arrays.asList(userRole)))
                .enabled(true)
                .build()
        );
    }

    @Transactional
    private Privilege createPrivilegeIfNotFound(String name) {

        Privilege privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege(name);
            privilegeRepository.saveAndFlush(privilege);
        }
        return privilege;
    }

    @Transactional
    private Role createRoleIfNotFound(
            String name, Collection<Privilege> privileges) {

        Role role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role(name);
            role.setPrivileges(privileges);
            roleRepository.saveAndFlush(role);
        }
        return role;
    }

    @Transactional
    private void createUserIfNotFound(User userAdmin) {
        User user = userRepository.findByEmail(userAdmin.getEmail());
        if (user == null) {
            user = userAdmin;
        }
        userRepository.saveAndFlush(user);
    }
}