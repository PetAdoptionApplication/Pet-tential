package com.codeup.pettential.controllers;

import com.codeup.pettential.models.App;
import com.codeup.pettential.models.Shelter;
import com.codeup.pettential.models.User;
import com.codeup.pettential.repositories.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class UserController {
    private Users users;
    private PasswordEncoder passwordEncoder;
    private ShelterRepository shelterDao;
    private ProgramRepository programDao;
    private AppRepository appDao;
    private UserRepository userDao;
    private PreferencesRepository preferenceDao;

    public UserController(Users users, PasswordEncoder passwordEncoder, ShelterRepository shelterDao, ProgramRepository programDao, AppRepository appDao, UserRepository userDao, PreferencesRepository preferenceDao) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.shelterDao = shelterDao;
        this.programDao = programDao;
        this.appDao = appDao;
        this.userDao = userDao;
        this.preferenceDao = preferenceDao;
    }

    @GetMapping("/")
    public String homePage() {
        return "system/landing";
    }

    @GetMapping("/sign-up")
    public String showSignupForm(Model model){
        model.addAttribute("user", new User());
        return "system/sign-up";
    }

    @GetMapping("/home")
    public String success(Model model){
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() == "anonymousUser"){
            return "redirect:/login";
        }
        model.addAttribute("programs", programDao.findAll());
        model.addAttribute("app", appDao.findAll());

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user.getIsShelter()) {
            Shelter thisShelter = shelterDao.findByUser(user);
            List<App> appForThisShelter = appDao.findAllByShelter(thisShelter);
            model.addAttribute("apps", appForThisShelter);
            return "views/shelter_home";
        }else {
            return "views/adopter_home";
        }
    }

    @PostMapping("/sign-up")
    public String saveUser(@ModelAttribute User user, Model model){
        if (user.getUsername().equals("") || user.getPassword().equals("") || user.getEmail().equals("") ||
                user.getAddress().equals("") || user.getNumber().equals("")){
            model.addAttribute("error", "All Fields Must be filled in");
            return "system/sign-up";
        }
        if (user.getPassword().length() < 8){
            model.addAttribute("error", "Password Must Be 8 characters in length");
            return "system/sign-up";
        }
        String hash = passwordEncoder.encode(user.getPassword());
        user.setPassword(hash);
        users.save(user);
        users.save(user);
        String returnValue = "";
        if (user.getIsShelter()){
            returnValue = "redirect:shelter/register/" + user.getId();
        } else {
        }
        return returnValue;
    }

    //For the Shelter Registration form
    @GetMapping("/shelter/register/{id}")
    public String createShelter(Model model, @PathVariable long id){
        User user = userDao.findOne(id);
        model.addAttribute("user", user);
        model.addAttribute("newShelter", new Shelter());
        return "system/register";
    }

    @PostMapping("/shelter/register/{id}")
    public String saveShelter (@ModelAttribute Shelter newShelter, @PathVariable long id){
        User user = userDao.findOne(id);
        newShelter.setUser(user);
        shelterDao.save(newShelter);
        return "redirect:/login";
    }
}
