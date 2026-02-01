package hr.abysalto.hiring.api.junior.controller;

import hr.abysalto.hiring.api.junior.components.DatabaseInitializer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    private final DatabaseInitializer databaseInitializer;

    public LoginController(final DatabaseInitializer databaseInitializer) {
        this.databaseInitializer = databaseInitializer;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

}
