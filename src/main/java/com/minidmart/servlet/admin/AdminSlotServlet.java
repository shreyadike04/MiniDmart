package com.minidmart.servlet.admin;

import com.minidmart.dao.PickupSlotDao;
import com.minidmart.model.PickupSlot;
import com.minidmart.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;

public class AdminSlotServlet extends HttpServlet {

    private final PickupSlotDao slotDao = new PickupSlotDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("slots", slotDao.listByDateRange(LocalDate.now(), LocalDate.now().plusDays(14)));
            request.setAttribute("csrfToken", CsrfUtil.getOrCreateToken(request.getSession()));
            request.setAttribute("pageTitle", "Pickup Slots - Admin - Mini D-Mart");
            request.getRequestDispatcher("/WEB-INF/jsp/admin/slots.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!"/create".equals(request.getPathInfo())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!CsrfUtil.isValid(request)) {
            FlashUtil.error(request, "Your session expired. Please try again.");
            response.sendRedirect(request.getContextPath() + "/admin/slots");
            return;
        }
        String slotDateParam = request.getParameter("slotDate");
        String startTimeParam = request.getParameter("startTime");
        String endTimeParam = request.getParameter("endTime");
        int capacity = ValidationUtil.parsePositiveIntOrDefault(request.getParameter("capacity"), -1);

        try {
            LocalDate slotDate = LocalDate.parse(slotDateParam);
            LocalTime startTime = LocalTime.parse(startTimeParam);
            LocalTime endTime = LocalTime.parse(endTimeParam);
            if (capacity < 1 || !startTime.isBefore(endTime)) {
                FlashUtil.error(request, "Please provide a valid time range and a positive capacity.");
                response.sendRedirect(request.getContextPath() + "/admin/slots");
                return;
            }
            PickupSlot slot = new PickupSlot();
            slot.setSlotDate(slotDate);
            slot.setStartTime(startTime);
            slot.setEndTime(endTime);
            slot.setCapacity(capacity);
            try {
                int slotId = slotDao.create(slot);
                AuditLogger.log(SessionUtil.getUserId(request), "PICKUP_SLOT_CREATED", "PICKUP_SLOT", slotId,
                        slotDate + " " + startTime, request);
                FlashUtil.success(request, "Slot created.");
            } catch (SQLException e) {
                if ("23000".equals(e.getSQLState())) {
                    FlashUtil.error(request, "A slot already exists for that date and start time.");
                } else {
                    throw e;
                }
            }
            response.sendRedirect(request.getContextPath() + "/admin/slots");
        } catch (java.time.format.DateTimeParseException e) {
            FlashUtil.error(request, "Invalid date or time format.");
            response.sendRedirect(request.getContextPath() + "/admin/slots");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
