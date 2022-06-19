package com.lgyar.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "user")
public class UserController {
    @RequestMapping(value = "")
    @ResponseBody
    public String index() {
        return "Hello user!";
    }
}
