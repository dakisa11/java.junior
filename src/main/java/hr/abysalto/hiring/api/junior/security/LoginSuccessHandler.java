package hr.abysalto.hiring.api.junior.security;

import hr.abysalto.hiring.api.junior.components.DatabaseInitializer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final DatabaseInitializer databaseInitializer;

    public LoginSuccessHandler(final DatabaseInitializer databaseInitializer) {
        this.databaseInitializer = databaseInitializer;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    )throws IOException {

        databaseInitializer.initialize();

        response.sendRedirect("/buyer/");
    }
}
