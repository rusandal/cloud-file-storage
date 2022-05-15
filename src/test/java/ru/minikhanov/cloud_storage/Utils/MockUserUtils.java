package ru.minikhanov.cloud_storage.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.minikhanov.cloud_storage.models.security.ERole;
import ru.minikhanov.cloud_storage.models.security.Role;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.security.RoleRepository;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Component
public class MockUserUtils {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;
    public User addTestUser(String login, String password){
        Role role = roleRepository.save(new Role(ERole.ROLE_TEST));
        User user=User.builder().login(login).password(password).enabled(true).build();
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRole(roles);
        return userRepository.save(user);
    }
    public static User getMockUser(String username) {
        User user=User.builder().login(username).password("testPassword").enabled(false).build();
        Role role = new Role(ERole.ROLE_TEST);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRole(roles);
        return user;
    }
}