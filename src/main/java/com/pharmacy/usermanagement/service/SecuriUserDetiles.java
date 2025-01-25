package com.pharmacy.usermanagement.service;

import com.pharmacy.usermanagement.model.UserEntity;
import com.pharmacy.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SecuriUserDetiles implements UserDetailsService {


    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<UserEntity> user = userRepository.findByEmail(username);

        if(user.isEmpty()){
            // throw

            // global hadle
        }


        UserEntity userDB = user.get();
        UserDetails userDetails = new User(userDB.getEmail(), userDB.getPassword(), userDB.isAccountLocked(),true,true,true,List.of());

        System.out.println("   ************************     user name  " + userDB.getEmail() );
        return userDetails;
    }
}
