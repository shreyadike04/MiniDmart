<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header"><h1>Audit Log</h1></div>
<p class="muted">Most recent 200 events.</p>

<div class="table-wrap">
<table>
  <tr><th>Time</th><th>User</th><th>Action</th><th>Entity</th><th>Details</th><th>IP</th></tr>
  <c:forEach var="row" items="${rows}">
    <tr>
      <td>${row.createdAt}</td>
      <td>
        <c:choose>
          <c:when test="${not empty row.userEmail}">${fn:escapeXml(row.userName)}<br><span class="muted">${fn:escapeXml(row.userEmail)}</span></c:when>
          <c:otherwise><span class="muted">system</span></c:otherwise>
        </c:choose>
      </td>
      <td><span class="badge badge-gray">${fn:escapeXml(row.action)}</span></td>
      <td>${fn:escapeXml(row.entityType)} <c:if test="${not empty row.entityId}">#${row.entityId}</c:if></td>
      <td>${fn:escapeXml(row.details)}</td>
      <td>${fn:escapeXml(row.ipAddress)}</td>
    </tr>
  </c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
