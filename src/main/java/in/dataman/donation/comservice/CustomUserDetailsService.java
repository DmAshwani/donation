package in.dataman.donation.comservice;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import in.dataman.donation.comentity.UserMast;
import in.dataman.donation.comrepository.UserMastRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMastRepository userMastRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        UserMast user = userMastRepository.findByEMailOrMobile(identifier, identifier)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email or mobile: " + identifier));
        System.out.println("User found: " + user); 
        return new org.springframework.security.core.userdetails.User(
                user.getEMail(), user.getPassWd(), new ArrayList<>());
    }
}
