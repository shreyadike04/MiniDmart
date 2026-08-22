<%@ page contentType="text/html;charset=UTF-8" %>
<% String pageTitle = "Access Denied - Mini D-Mart"; %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>
<div class="empty-state">
  <div class="icon">🚫</div>
  <h2>Access denied</h2>
  <p class="muted">You don't have permission to view this page.</p>
  <a class="btn btn-primary" href="${pageContext.request.contextPath}/home">Go home</a>
</div>
<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
