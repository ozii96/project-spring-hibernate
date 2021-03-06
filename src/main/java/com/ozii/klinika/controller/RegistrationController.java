package com.ozii.klinika.controller;

import java.util.List;
import java.util.logging.Logger;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ozii.klinika.user.Customer;

@Controller
@RequestMapping("/registration")
public class RegistrationController {
	

	// Create new users
	@Autowired
	private UserDetailsManager userDetailsManager;

	// Encrypt user's password
	private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	// logger for diagnostics
	private Logger logger = Logger.getLogger(getClass().getName());

	// Pre-process every String data form. Remove leading and trailing whitespaces.
	// If only whitespaces - trim to null
	@InitBinder
	public void initBinder(WebDataBinder dataBinder) {

		StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

		dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
	}
	
	/* Return registration form
	 * 
	 * access / can register
	 * 
	 * ADMIN - PATIENT, DOCTOR, MODERATOR, ADMIN
	 * MODERATOR - PATIENT, DOCTOR
	 * DOCTOR - PATIENT
	 */
	@GetMapping("/")
	public String showRegistrationForm(Model theModel) {
		theModel.addAttribute("customer", new Customer());
		return "registration-form";
	}
	

	/* Checks registered Customer
	 * If any errors, method return registration form with suitable error message.
	 * If successful, method return registration confirmation. 
	 */
	@PostMapping("/processRegistrationForm")
	public String processRegistrationForm(@Valid @ModelAttribute("customer") Customer theCustomer,
			BindingResult theBindingResult, Model theModel) {

		String userName = theCustomer.getUserName();

		logger.info("Processing registration form for: " + userName);

		/* Form validation
		 * Return registration form if invalid username or password
		 */
		if (theBindingResult.hasErrors()) {

			theModel.addAttribute("customer", new Customer());
			theModel.addAttribute("registrationError", "Use PESEL as username / password cannot be empty.");

			logger.warning("User name/password cannot be empty.");

			return "registration-form";
		}

		/* Check the database if user already exists.
		 * Return registration form if user (username) exists.
		 */
		logger.info("Checking if user exists: " + userName);
		
		boolean exists = userDetailsManager.userExists(userName);
		
		logger.info("User: " + userName + ", exists: " + exists);
		
		if (exists) {
			theModel.addAttribute("customer", new Customer());
			theModel.addAttribute("registrationError", "User name already exists.");

			logger.warning("User name already exists.");

			return "registration-form";
		}
		
		// The validation is successful
		
		// encrypt the password
		String encodedPassword = passwordEncoder.encode(theCustomer.getPassword());

		// prepend the encoding algorithm id
		encodedPassword = "{bcrypt}" + encodedPassword;

		// give user roles
		List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(theCustomer.getAuthorities());
		
		// add default role: ROLE_PATIENT
		authorities.add(new SimpleGrantedAuthority("ROLE_PATIENT"));

		// create user object (from Spring Security framework)
		User tempUser = new User(userName, encodedPassword, authorities);

		// save user in the database
		userDetailsManager.createUser(tempUser);

		logger.info("Successfully created user: " + userName);

		return "registration-confirmation";
	}
}

