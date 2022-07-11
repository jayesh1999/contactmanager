package com.smart.MyConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRepository;
import com.smart.entity.User;

public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		
		User  user = userRepository.getUserByUserName(email);
		
		if(user == null) {
			throw new UsernameNotFoundException("user is not found in database");
		}
		
		CustomUserDetails  customUserDetails   = new CustomUserDetails(user);
		return customUserDetails;
	}

}
