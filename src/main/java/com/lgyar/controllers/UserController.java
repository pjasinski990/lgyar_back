package com.lgyar.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "user")
public class UserController {

    @RequestMapping(value = "")
    @ResponseBody
    public String index() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            System.out.println("logged user has authorities: " + auth.getAuthorities());
        }
        else {
            System.out.println("no user logged in");
        }
        return "Hello user!";
    }

    @GetMapping(value = "username")
    @ResponseBody
    public String currentUserName() {
        return "Hello from get username";
    }
}
