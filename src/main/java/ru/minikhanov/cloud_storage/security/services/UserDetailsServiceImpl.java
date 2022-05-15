package ru.minikhanov.cloud_storage.security.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.minikhanov.cloud_storage.models.security.User;
import ru.minikhanov.cloud_storage.repository.security.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    final
    UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(login)
                .orElseThrow(()-> new UsernameNotFoundException("User not found: "+login));
        return UserDetailsImpl.build(user);
    }
}
