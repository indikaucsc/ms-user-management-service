package com.pharmacy.usermanagement.service;

import com.pharmacy.usermanagement.model.UserEntity;
import com.pharmacy.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SecurityUserDetails implements UserDetailsService {


    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<UserEntity> userOptional = userRepository.findByEmail(username);

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User with email '" + username + "' not found.");
        }
        UserEntity userDB = userOptional.get();
//        List<GrantedAuthority> authorities = userDB.getUserRoles().stream()
//                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getRoleName()))
//                .collect(Collectors.toList());


        UserDetails userDetails = new User(userDB.getEmail(), userDB.getPassword(), userDB.isAccountLocked(), true, true, true, List.of());

        System.out.println("   ************************     user name  " + userDB.getEmail());
        return userDetails;
    }
}
