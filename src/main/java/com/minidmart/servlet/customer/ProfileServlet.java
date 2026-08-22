package com.minidmart.servlet.customer;

import com.minidmart.dao.AddressDao;
import com.minidmart.dao.UserDao;
import com.minidmart.model.Address;
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

public class ProfileServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();
    private final AddressDao addressDao = new AddressDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        show(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfUtil.isValid(request)) {
            FlashUtil.error(request, "Your session expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        int userId = SessionUtil.getUserId(request);
        String action = request.getParameter("action");
        try {
            switch (action == null ? "" : action) {
                case "changePassword":
                    changePassword(request, response, userId);
                    return;
                case "addAddress":
                    addAddress(request, userId);
                    break;
                case "deleteAddress":
                    deleteAddress(request, userId);
                    break;
                default:
                    FlashUtil.error(request, "Unknown action.");
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void changePassword(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException, SQLException {
        String current = request.getParameter("currentPassword");
        String next = request.getParameter("newPassword");
        String confirm = request.getParameter("confirmNewPassword");

        Optional<User> userOpt = userDao.findById(userId);
        if (userOpt.isEmpty() || !PasswordUtil.verify(current, userOpt.get().getPasswordHash())) {
            FlashUtil.error(request, "Current password is incorrect.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        if (!ValidationUtil.isStrongPassword(next) || !next.equals(confirm)) {
            FlashUtil.error(request, "New password must be at least 8 characters with a letter and a number, and match confirmation.");
            response.sendRedirect(request.getContextPath() + "/profile");
            return;
        }
        try (java.sql.Connection conn = DBUtil.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement("UPDATE users SET password_hash = ? WHERE user_id = ?")) {
            ps.setString(1, PasswordUtil.hash(next));
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
        AuditLogger.log(userId, "PASSWORD_CHANGED", "USER", userId, null, request);
        FlashUtil.success(request, "Password updated.");
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void addAddress(HttpServletRequest request, int userId) throws SQLException {
        String line1 = request.getParameter("line1");
        String city = request.getParameter("city");
        String state = request.getParameter("state");
        String pincode = request.getParameter("pincode");
        if (ValidationUtil.isBlank(line1) || ValidationUtil.isBlank(city)
                || ValidationUtil.isBlank(state) || !ValidationUtil.isValidPincode(pincode)) {
            FlashUtil.error(request, "Please fill in a valid address.");
            return;
        }
        Address a = new Address();
        a.setUserId(userId);
        a.setLabel(blankToDefault(request.getParameter("label"), "Home"));
        a.setLine1(line1.trim());
        a.setLine2(request.getParameter("line2"));
        a.setCity(city.trim());
        a.setState(state.trim());
        a.setPincode(pincode.trim());
        a.setDefaultAddress("on".equals(request.getParameter("isDefault")));
        addressDao.create(a);
        FlashUtil.success(request, "Address added.");
    }

    private void deleteAddress(HttpServletRequest request, int userId) throws SQLException {
        int addressId = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("addressId"), -1);
        if (addressId > 0 && addressDao.delete(addressId, userId)) {
            FlashUtil.success(request, "Address removed.");
        } else {
            FlashUtil.error(request, "Address not found.");
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int userId = SessionUtil.getUserId(request);
        try {
            Optional<User> user = userDao.findById(userId);
            if (user.isEmpty()) {
                HttpSession session = request.getSession(false);
                if (session != null) session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            request.setAttribute("user", user.get());
            request.setAttribute("addresses", addressDao.listByUser(userId));
            request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
            request.setAttribute("pageTitle", "My Profile - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/customer/profile.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private String blankToDefault(String s, String def) {
        return ValidationUtil.isBlank(s) ? def : s.trim();
    }
}
