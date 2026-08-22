<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header"><h1>Users</h1></div>

<div class="table-wrap">
<table>
  <tr><th>Name</th><th>Email</th><th>Phone</th><th>Role</th><th>Status</th><th>Joined</th><th>Actions</th></tr>
  <c:forEach var="u" items="${users}">
    <tr>
      <td>${fn:escapeXml(u.fullName)}</td>
      <td>${fn:escapeXml(u.email)}</td>
      <td>${fn:escapeXml(u.phone)}</td>
      <td>
        <c:choose>
          <c:when test="${u.role == 'ADMIN'}"><span class="badge badge-green">ADMIN</span></c:when>
          <c:when test="${u.role == 'STAFF'}"><span class="badge badge-amber">STAFF</span></c:when>
          <c:otherwise><span class="badge badge-gray">CUSTOMER</span></c:otherwise>
        </c:choose>
      </td>
      <td>
        <c:choose>
          <c:when test="${u.active}"><span class="badge badge-green">Active</span></c:when>
          <c:otherwise><span class="badge badge-red">Inactive</span></c:otherwise>
        </c:choose>
      </td>
      <td>${u.createdAt}</td>
      <td>
        <form method="post" action="${pageContext.request.contextPath}/admin/users/role" style="display:inline-block; margin-right:6px;">
          <input type="hidden" name="csrfToken" value="${csrfToken}">
          <input type="hidden" name="userId" value="${u.userId}">
          <select name="role" onchange="this.form.submit()" style="width:auto; display:inline-block; padding:4px;">
            <option value="CUSTOMER" ${u.role == 'CUSTOMER' ? 'selected' : ''}>CUSTOMER</option>
            <option value="STAFF" ${u.role == 'STAFF' ? 'selected' : ''}>STAFF</option>
            <option value="ADMIN" ${u.role == 'ADMIN' ? 'selected' : ''}>ADMIN</option>
          </select>
        </form>
        <form method="post" action="${pageContext.request.contextPath}/admin/users/status" style="display:inline-block;">
          <input type="hidden" name="csrfToken" value="${csrfToken}">
          <input type="hidden" name="userId" value="${u.userId}">
          <input type="hidden" name="active" value="${u.active ? 'false' : 'true'}">
          <button type="submit" class="btn btn-sm ${u.active ? 'btn-danger' : 'btn-secondary'}">${u.active ? 'Deactivate' : 'Activate'}</button>
        </form>
      </td>
    </tr>
  </c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
