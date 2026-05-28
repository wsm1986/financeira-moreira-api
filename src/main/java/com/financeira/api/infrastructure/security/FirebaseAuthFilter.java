package com.financeira.api.infrastructure.security;

import com.financeira.api.application.usecase.user.UpsertUserUseCase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class FirebaseAuthFilter extends OncePerRequestFilter {

    private final UpsertUserUseCase upsertUser;

    public FirebaseAuthFilter(UpsertUserUseCase upsertUser) {
        this.upsertUser = upsertUser;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        String token = header.substring(7);
        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);
            String uid = decoded.getUid();
            // Auto-upsert: garante que o usuário existe na tabela users antes de qualquer operação
            upsertUser.execute(uid,
                    decoded.getEmail() != null ? decoded.getEmail() : uid + "@firebase",
                    decoded.getName()  != null ? decoded.getName()  : "Usuário");
            var auth = new UsernamePasswordAuthenticationToken(uid, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            String msg = e.getMessage() != null
                    ? e.getMessage().replace("\"", "'")
                    : "Token inválido";
            response.getWriter().write(
                "{\"error\":\"UNAUTHORIZED\",\"message\":\"" + msg + "\"}"
            );
            return;
        }
        chain.doFilter(request, response);
    }
}
