package com.diving.prototype;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

public class HomeController {
    @GetMapping("/")
    public String showHomePage(Model model) {
        return "index"; // Thymeleaf 템플릿의 이름 (index.html)
    }
}
