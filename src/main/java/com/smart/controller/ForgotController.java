package com.smart.controller;

import java.security.Principal;
import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entity.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {

	@Autowired
	private UserRepository userRepository;
	
	Random random =  new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	//email id from open handler
	@GetMapping("/forgot")
	public String openEmailFrom() {
		return "forgot_email_from";
	}
	
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email")String email, HttpSession session) {
		
		System.out.println("Email" + " " +  email);
		
		//generating opt of 4 digit
		
		int otp = random.nextInt(9999);
		System.out.println(otp);
		
		String subject= "otp from SCM";
		String message = "<h1>OTP is" +otp+ "</h1>";
		String to = email;
		//write code to send otp
		boolean flag = this.emailService.sendEmail(subject, message, to);
		if(flag) {
			
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "varify_otp";
			
		}else {
			session.setAttribute("message", "problem in sending an otp!!");
			return "forgot_email_from";
		}
		
	}
	
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp")int otp,HttpSession session) {
	 int sessionOtp  = (int)session.getAttribute("myotp");
	 String email = (String)session.getAttribute("email");
	 System.out.println("sessionotp"  + sessionOtp);
	 System.out.println("userotp" + otp);
	 if(sessionOtp==otp) {
		 System.out.println("otpmatch");
		 //password change form
		User user = this.userRepository.getUserByUserName(email);
		System.out.println(user);
		 if(user == null) {
			 //send error message
			 session.setAttribute("message", "User does not exists with this email");
				return "forgot_email_from";
		 }
		 else{
			//send change password 
			 return "password_change_from";
		 }
		 
	 }
	 else {
		 session.setAttribute("message", "You have entered wrong otp");
		 return "varify_otp";
	 }
	 
	}
	
	//forgot password change
	@PostMapping("/forgot-password-change")
	public String forgotPasswordChange(@RequestParam("newpassword") String newpassword, HttpSession session) {
		
		String userName  = (String)session.getAttribute("email"); 
		User user  =  this.userRepository.getUserByUserName(userName);
		
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		
		this.userRepository.save(user);
		return "redirect:/signin?change=password changed successfully....";
	}
	

}
