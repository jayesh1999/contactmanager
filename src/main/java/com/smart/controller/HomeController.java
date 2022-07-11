package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;

import com.smart.dao.UserRepository;
//import com.smart.config.Config;
import com.smart.entity.User;
import com.smart.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@GetMapping("/")
	public String homePage(Model model) {
		model.addAttribute("title", "Home - Samer contact manager");
		return "home";
	}

	@GetMapping("about")
	public String aboutPage(Model model) {
		model.addAttribute("title", "About - Samer contact manager");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model) {

		model.addAttribute("user", new User());
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	@PostMapping("/do_register")
	public String registerUser(Model model,  @ModelAttribute("user") User user,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement,HttpSession session) {

		try  {
			if(!agreement) {
			System.out.println("You have not agreed the term and condition");

			throw new Exception("You have not agreed the term and condition");
		}
			
			
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			
			user.setPassword(passwordEncoder.encode((user.getPassword())));

			System.out.println(user);

			User result = this.userRepository.save(user);

			System.out.println(result);
			model.addAttribute("user", new User());

			session.setAttribute("message", new Message("Successfully Register", "alert-success"));
			System.out.println(session.getAttribute("message"));
			return "signup";

		 }catch (Exception e) {

			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !!" + e.getMessage(), "alert-danger"));
		}

		return "signup";
	
	}
	
	//handler for custom mapping
	
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title","Login Page");
		return "Login";
	}
	

}
