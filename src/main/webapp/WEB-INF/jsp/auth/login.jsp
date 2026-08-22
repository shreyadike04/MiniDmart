<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card form-card">
  <h1>Sign in</h1>
  <p class="muted">Welcome back to Mini D-Mart.</p>
  <form method="post" action="${pageContext.request.contextPath}/login">
    <input type="hidden" name="csrfToken" value="${csrfToken}">
    <c:if test="${not empty redirect}"><input type="hidden" name="redirect" value="${fn:escapeXml(redirect)}"></c:if>
    <div class="form-group">
      <label for="email">Email</label>
      <input type="email" id="email" name="email" value="${fn:escapeXml(email)}" required autofocus>
    </div>
    <div class="form-group">
      <label for="password">Password</label>
      <input type="password" id="password" name="password" required>
    </div>
    <button type="submit" class="btn btn-primary btn-block">Sign in</button>
  </form>
  <p class="muted" style="margin-top:14px;">New here? <a href="${pageContext.request.contextPath}/register">Create an account</a></p>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
