package it.uniroma3.cyberlab.security;

import it.uniroma3.cyberlab.entity.User;
import it.uniroma3.cyberlab.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getStatus() == User.UserStatus.BANNED) {
            throw new DisabledException("User account is banned");
        }

        if (user.getStatus() == User.UserStatus.SUSPENDED) {
            throw new DisabledException("User account is suspended");
        }

        return new CustomUserPrincipal(user);
    }

    // Custom exception for disabled accounts
    public static class DisabledException extends RuntimeException {
        public DisabledException(String message) {
            super(message);
        }
    }
}