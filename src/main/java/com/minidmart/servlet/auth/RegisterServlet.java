package com.minidmart.servlet.auth;

import com.minidmart.dao.UserDao;
import com.minidmart.model.Role;
import com.minidmart.model.User;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

public class RegisterServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        request.setAttribute("pageTitle", "Create Account - Mini D-Mart");
        request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullName = trim(request.getParameter("fullName"));
        String email = trim(request.getParameter("email"));
        String phone = trim(request.getParameter("phone"));
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        String error = validate(fullName, email, phone, password, confirmPassword);
        if (error != null) {
            reshow(request, response, error, fullName, email, phone);
            return;
        }

        try {
            if (userDao.findByEmail(email).isPresent()) {
                reshow(request, response, "An account with that email already exists.", fullName, email, phone);
                return;
            }
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhone(phone);
            user.setPasswordHash(PasswordUtil.hash(password));
            user.setRole(Role.CUSTOMER);
            int userId = userDao.create(user);
            AuditLogger.log(userId, "REGISTER", "USER", userId, "New customer account", request);
            FlashUtil.success(request, "Account created. Please sign in.");
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private String validate(String fullName, String email, String phone, String password, String confirmPassword) {
        if (ValidationUtil.isBlank(fullName) || fullName.length() < 2) {
            return "Please enter your full name.";
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return "Please enter a valid email address.";
        }
        if (!ValidationUtil.isBlank(phone) && !ValidationUtil.isValidPhone(phone)) {
            return "Please enter a valid phone number.";
        }
        if (!ValidationUtil.isStrongPassword(password)) {
            return "Password must be at least 8 characters and include a letter and a number.";
        }
        if (password == null || !password.equals(confirmPassword)) {
            return "Passwords do not match.";
        }
        return null;
    }

    private void reshow(HttpServletRequest request, HttpServletResponse response, String error,
                         String fullName, String email, String phone)
            throws ServletException, IOException {
        request.setAttribute("pageTitle", "Create Account - Mini D-Mart");
        request.setAttribute("flashError", error);
        request.setAttribute("fullName", fullName);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
        request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/jsp/auth/register.jsp").forward(request, response);
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }
}
