package org.example.cloudstorage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeRedirectController {
    @GetMapping("/files/**")
    public String redirectToRoot() {
        return "redirect:/";
    }
}
