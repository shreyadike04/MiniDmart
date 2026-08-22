<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card form-card">
  <h1>Create your account</h1>
  <p class="muted">Join Mini D-Mart to shop groceries, schedule pickup or delivery.</p>
  <form method="post" action="${pageContext.request.contextPath}/register">
    <input type="hidden" name="csrfToken" value="${csrfToken}">
    <div class="form-group">
      <label for="fullName">Full name</label>
      <input type="text" id="fullName" name="fullName" value="${fn:escapeXml(fullName)}" required autofocus>
    </div>
    <div class="form-group">
      <label for="email">Email</label>
      <input type="email" id="email" name="email" value="${fn:escapeXml(email)}" required>
    </div>
    <div class="form-group">
      <label for="phone">Phone (optional)</label>
      <input type="tel" id="phone" name="phone" value="${fn:escapeXml(phone)}">
    </div>
    <div class="form-group">
      <label for="password">Password</label>
      <input type="password" id="password" name="password" required minlength="8">
      <div class="form-hint">At least 8 characters, with a letter and a number.</div>
    </div>
    <div class="form-group">
      <label for="confirmPassword">Confirm password</label>
      <input type="password" id="confirmPassword" name="confirmPassword" required minlength="8">
    </div>
    <button type="submit" class="btn btn-primary btn-block">Create account</button>
  </form>
  <p class="muted" style="margin-top:14px;">Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in</a></p>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
