package com.lgyar.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MasterController {
    @RequestMapping(value = "")
    @ResponseBody
    public String index() {
        return "<h1>Hello world!</h1>";
    }

    @RequestMapping(value = "login")
    @ResponseBody
    public String login() {
        return "Hello login";
    }

    @RequestMapping(value = "register")
    @ResponseBody
    public String register() {
        return "Hello register";
    }
}
