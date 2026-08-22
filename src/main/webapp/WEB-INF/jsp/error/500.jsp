<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<% String pageTitle = "Something Went Wrong - Mini D-Mart"; %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>
<div class="empty-state">
  <div class="icon">⚠️</div>
  <h2>Something went wrong</h2>
  <p class="muted">An unexpected error occurred. Please try again, and contact support if it persists.</p>
  <a class="btn btn-primary" href="${pageContext.request.contextPath}/home">Go home</a>
</div>
<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
