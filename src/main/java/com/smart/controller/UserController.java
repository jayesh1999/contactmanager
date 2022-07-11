package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entity.Contact;
import com.smart.entity.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
@MultipartConfig
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	// this method is called for starting of the below api
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		System.out.println(principal.getName());

		User user = userRepository.getUserByUserName(principal.getName());
		model.addAttribute("user", user);

	}

	// home dashboard
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "user-dashboard");
		return "/normal/user-dashboard";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model, Principal principal) {
		model.addAttribute("title", "Add-contact");
		model.addAttribute("contact", new Contact());
		return "/normal/add-user-contact";
	}

	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			HttpSession session, Model model, Principal principal) throws IOException {

		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// processing and uploading file

			if (file.isEmpty()) {
				System.out.println("File is empty");
				// contact.setImage("profiledefault.png");
			} else {
				// upload the file to folder and update the name to contact
				System.out.println(file.getName());
				System.out.println(file.getContentType());
				System.out.println(file.getOriginalFilename());
				System.out.println(file.getSize());

				File saveFile = new ClassPathResource("static/img").getFile();
				// System.out.println(saveFile);
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				// System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				// System.out.println("file upload successfully");
			}

			contact.setImage(file.getOriginalFilename());
			contact.setUser(user);
			user.getContacts().add(contact);

			this.userRepository.save(user);
			System.out.println(contact);

			System.out.println("Added to data base");
			// message success

			session.setAttribute("message", new Message("contact added succesfully !! add more..", "success"));

		} catch (Exception e) {
			// message error
			e.printStackTrace();
			session.setAttribute("message", new Message(e.getMessage(), "danger"));
			System.out.println("Error" + e.getMessage());
		}

		return "/normal/add-user-contact";
	}

	// fetch all contacts
	// per page 5[n]
	// current page = 0 [page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "contacts");

		// forwarding list on contacts
		User user = this.userRepository.getUserByUserName(principal.getName());
		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);
		System.out.println(contacts);
		model.addAttribute("contact", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "/normal/show_contacts";
	}

	// showing particular contact details
	@GetMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") int cid, Model model, Principal principal) {
		model.addAttribute("title", "contact details");
		System.out.println("cId  " + cid);

		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact singleContact = contactOptional.get();

		User user = this.userRepository.getUserByUserName(principal.getName());

		if (user.getId() == singleContact.getUser().getId()) {
			model.addAttribute("contact", singleContact);

		}

		return "/normal/contact_details";
	}

	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") int cid, Model model, Principal principal, HttpSession session) {

		Contact singleContact = this.contactRepository.findById(cid).get();

		singleContact.setUser(null);
		this.contactRepository.delete(singleContact);

//	    User user = this.userRepository.getUserByUserName(principal.getName());
//		
//		if(user.getId() == singleContact.getUser().getId()) {
//			model.addAttribute("contact",singleContact);
//
//		}

		session.setAttribute("message", new Message("Content deleted successfully", "success"));

		return "redirect:/user/show-contacts/0";
	}

	// open update form handler
	@RequestMapping("/open-contact/{cId}")
	public String updateForm(@PathVariable("cId") int cid, Model model, Principal principal, HttpSession session) {
		model.addAttribute("title", "update contact details");

		Contact singleContact = this.contactRepository.findById(cid).get();

		User user = this.userRepository.getUserByUserName(principal.getName());

		if (user.getId() == singleContact.getUser().getId()) {
			model.addAttribute("contact", singleContact);

		} else {
			return "redirect:/user/show-contacts/0";
		}

		return "/normal/update_from";
	}

	// update contact handler
	@RequestMapping(value = "/process-update", method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, Principal principal,
			@RequestParam("profileImage") MultipartFile file, Model model, HttpSession session) {
		try {
			Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();

			if (!file.isEmpty()) {
				// file work
				// delete old photo

				File deleteFile = new ClassPathResource("static/img").getFile();
				// System.out.println(saveFile);
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();

				// update old photo

				File saveFile = new ClassPathResource("static/img").getFile();
				// System.out.println(saveFile);
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				// System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				// System.out.println("file upload successfully");

				contact.setImage(file.getOriginalFilename());
			}

			else {

			}
			System.out.println(contact.getcId());
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);

			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("your contact has been updated", "success"));

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(contact);

		return "redirect:/user/" + contact.getcId() + "/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model, Principal principal) {
		model.addAttribute("title","profile-page");
		String user = principal.getName();
		
		User dbUser =  userRepository.getUserByUserName(user);
		System.out.println(dbUser.getAbout());
		model.addAttribute("dbUser",dbUser);
		return "/normal/profile";
	}
	
	//open setting handler
	@GetMapping("/settings")
	
	public String openSetting(Model model) {
		
		model.addAttribute("title", "contact page");
		return "/normal/settings";
	}
	
	//password change handler
	@PostMapping("/change-password")
	public String changePassword(Model model, @RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal,HttpSession session) {
		
		System.out.println(oldPassword + " " + newPassword);
		
		String username = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(username);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//same password
		currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		this.userRepository.save(currentUser);
		
		session.setAttribute("message", new Message("Your Password is updated...","success"));
		}
		else {
			//error
			session.setAttribute("message", new Message("Please enter correct old password","danger"));
			return "redirect:/user/settings";
		}
		
		model.addAttribute("title", "user-dashboard");
		return "redirect:/user/index";
	}

}
