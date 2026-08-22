<%@ page contentType="text/html;charset=UTF-8" %>
<% String pageTitle = "Not Found - Mini D-Mart"; %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>
<div class="empty-state">
  <div class="icon">🔎</div>
  <h2>Page not found</h2>
  <p class="muted">The page you're looking for doesn't exist or may have moved.</p>
  <a class="btn btn-primary" href="${pageContext.request.contextPath}/home">Go home</a>
</div>
<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
