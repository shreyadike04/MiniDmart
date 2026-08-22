package com.minidmart.servlet.auth;

import com.minidmart.util.AuditLogger;
import com.minidmart.util.SessionUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = SessionUtil.getUserId(request);
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (userId != null) {
                AuditLogger.log(userId, "LOGOUT", "USER", userId, null, request);
            }
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/home");
    }
}
