package ru.minikhanov.cloud_storage.Utils;

import ru.minikhanov.cloud_storage.models.security.ERole;
import ru.minikhanov.cloud_storage.models.security.Role;
import ru.minikhanov.cloud_storage.models.security.User;

import java.util.HashSet;
import java.util.Set;

public class MockUserUtils {
    public static User getMockUser(String username) {
        User user=User.builder().login(username).password("testPassword").enabled(false).id(99999999L).build();
        Role role = new Role();
        role = new Role(ERole.ROLE_USER);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRole(roles);
        return user;
    }
}
