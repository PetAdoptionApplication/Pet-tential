package com.codeup.pettential.controllers;

import com.codeup.pettential.models.Shelter;
import com.codeup.pettential.models.User;
import com.codeup.pettential.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {
    private Users users;
    private PasswordEncoder passwordEncoder;
    private ShelterRepository shelterDao;
    private ProgramRepository programDao;
    private AppRepository appDao;
    private UserRepository userDao;

    public UserController(Users users, PasswordEncoder passwordEncoder, ShelterRepository shelterDao, ProgramRepository programDao, AppRepository appDao, UserRepository userDao) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.shelterDao = shelterDao;
        this.programDao = programDao;
        this.appDao = appDao;
        this.userDao = userDao;
    }

    @GetMapping("/sign-up")
    public String showSignupForm(Model model){
        model.addAttribute("user", new User());
        return "sign-up";
    }

    @GetMapping("/home")
    public String success(Model model){
        model.addAttribute("shelter", shelterDao.findAll());
        model.addAttribute("program", programDao.findAll());
        model.addAttribute("app", appDao.findAll());

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();

        if (userDao.findOne(user.getId()).getIsShelter()) {
            return "shelter/home";
        } if (!userDao.findOne(user.getId()).getIsShelter()) {
            return "adopter/home";
        } else {
            return "landing";
        }
    }

    @PostMapping("/sign-up")
    public String saveUser(@ModelAttribute User user){
        String hash = passwordEncoder.encode(user.getPassword());
        user.setPassword(hash);
        users.save(user);
        String returnValue = "";
        if (user.getIsShelter()){
            returnValue = "redirect:shelter/register";
        } else {
            returnValue = "redirect:login";
        }
        return returnValue;
    }

    //For the Shelter Registration form
    @GetMapping("/shelter/register")
    public String createShelter(Model model){
        model.addAttribute("newShelter", new Shelter());
        return "shelter/register";
    }

    @PostMapping("/shelter/register")
    public String saveShelter (@ModelAttribute Shelter newShelter){
        shelterDao.save(newShelter);
        return "redirect:/shelter/home";
    }

    public static void main(String[] args) {
        System.out.println(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }
}
