package com.minidmart.servlet.auth;

import com.minidmart.dao.UserDao;
import com.minidmart.model.User;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class LoginServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        request.setAttribute("pageTitle", "Sign In - Mini D-Mart");
        request.setAttribute("redirect", request.getParameter("redirect"));
        request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String redirect = request.getParameter("redirect");

        if (email == null || password == null) {
            reshow(request, response, "Please enter your email and password.", email, redirect);
            return;
        }
        email = email.trim();

        if (LoginAttemptTracker.isLocked(email)) {
            int secs = LoginAttemptTracker.remainingLockoutSeconds(email);
            reshow(request, response, "Too many failed attempts. Try again in " + (secs / 60 + 1) + " minute(s).",
                    email, redirect);
            return;
        }

        try {
            Optional<User> found = userDao.findByEmail(email);
            boolean ok = found.isPresent() && found.get().isActive()
                    && PasswordUtil.verify(password, found.get().getPasswordHash());

            if (!ok) {
                LoginAttemptTracker.recordFailure(email);
                AuditLogger.log(found.map(User::getUserId).orElse(null), "LOGIN_FAILURE", "USER", null,
                        "email=" + email, request);
                reshow(request, response, "Invalid email or password.", email, redirect);
                return;
            }

            LoginAttemptTracker.recordSuccess(email);
            User user = found.get();

            request.changeSessionId();
            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("role", user.getRole().name());
            session.setAttribute("fullName", user.getFullName());
            session.setAttribute("email", user.getEmail());

            AuditLogger.log(user.getUserId(), "LOGIN_SUCCESS", "USER", user.getUserId(), null, request);

            String target = resolveTarget(redirect, user.getRole().name(), request.getContextPath());
            response.sendRedirect(target);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private String resolveTarget(String redirect, String role, String contextPath) {
        if (redirect != null && redirect.startsWith("/") && !redirect.startsWith("//")) {
            boolean staffPath = redirect.startsWith("/staff/");
            boolean adminPath = redirect.startsWith("/admin/");
            if (staffPath && !("STAFF".equals(role) || "ADMIN".equals(role))) {
                // fall through to default below
            } else if (adminPath && !"ADMIN".equals(role)) {
                // fall through to default below
            } else {
                return contextPath + redirect;
            }
        }
        if ("ADMIN".equals(role)) return contextPath + "/admin/dashboard";
        if ("STAFF".equals(role)) return contextPath + "/staff/dashboard";
        return contextPath + "/home";
    }

    private void reshow(HttpServletRequest request, HttpServletResponse response, String error,
                         String email, String redirect) throws ServletException, IOException {
        request.setAttribute("pageTitle", "Sign In - Mini D-Mart");
        request.setAttribute("flashError", error);
        request.setAttribute("email", email);
        request.setAttribute("redirect", redirect);
        request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/jsp/auth/login.jsp").forward(request, response);
    }
}
