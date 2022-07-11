package com.smart.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entity.User;
import com.smart.entity.Contact;
import java.util.*;

@RestController
public class SearchController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query, Principal principal){
		
		System.out.println(query);
		System.out.println(principal.getName());
		User user = this.userRepository.getUserByUserName(principal.getName());
		
		
		List<Contact> contacts1  = this.contactRepository.findByNameContainingAndUser(query, user);
		System.out.println(contacts1);
		return ResponseEntity.ok(contacts1);
	}

}
