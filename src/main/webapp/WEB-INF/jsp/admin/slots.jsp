<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header"><h1>Pickup Slots</h1></div>

<div class="card" style="margin-bottom:18px; max-width:520px;">
  <h2>Create slot</h2>
  <form method="post" action="${pageContext.request.contextPath}/admin/slots/create">
    <input type="hidden" name="csrfToken" value="${csrfToken}">
    <div class="form-group">
      <label for="slotDate">Date</label>
      <input type="date" id="slotDate" name="slotDate" required>
    </div>
    <div class="form-group">
      <label for="startTime">Start time</label>
      <input type="time" id="startTime" name="startTime" required>
    </div>
    <div class="form-group">
      <label for="endTime">End time</label>
      <input type="time" id="endTime" name="endTime" required>
    </div>
    <div class="form-group">
      <label for="capacity">Capacity</label>
      <input type="number" id="capacity" name="capacity" value="8" min="1" required>
    </div>
    <button type="submit" class="btn btn-primary">Create slot</button>
  </form>
</div>

<div class="table-wrap">
<table>
  <tr><th>Date</th><th>Window</th><th>Capacity</th><th>Booked</th><th>Remaining</th></tr>
  <c:forEach var="s" items="${slots}">
    <tr>
      <td>${s.slotDate}</td>
      <td>${s.startTime} - ${s.endTime}</td>
      <td>${s.capacity}</td>
      <td>${s.bookedCount}</td>
      <td>
        ${s.remaining}
        <c:if test="${s.remaining <= 0}"><span class="badge badge-red">Full</span></c:if>
      </td>
    </tr>
  </c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
