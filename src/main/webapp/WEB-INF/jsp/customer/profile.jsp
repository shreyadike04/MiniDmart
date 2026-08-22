<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header">
  <div>
    <h1>My Profile</h1>
    <p class="muted">${fn:escapeXml(user.fullName)} &middot; ${fn:escapeXml(user.email)}</p>
  </div>
</div>

<div class="grid" style="grid-template-columns: 1fr 1fr; align-items:start;">
  <div class="card">
    <h2>Account details</h2>
    <table>
      <tr><th>Full name</th><td>${fn:escapeXml(user.fullName)}</td></tr>
      <tr><th>Email</th><td>${fn:escapeXml(user.email)}</td></tr>
      <tr><th>Phone</th><td>${fn:escapeXml(user.phone)}</td></tr>
      <tr><th>Role</th><td><span class="badge badge-green">${user.role}</span></td></tr>
    </table>

    <h3 style="margin-top:22px;">Change password</h3>
    <form method="post" action="${pageContext.request.contextPath}/profile">
      <input type="hidden" name="csrfToken" value="${csrfToken}">
      <input type="hidden" name="action" value="changePassword">
      <div class="form-group">
        <label for="currentPassword">Current password</label>
        <input type="password" id="currentPassword" name="currentPassword" required>
      </div>
      <div class="form-group">
        <label for="newPassword">New password</label>
        <input type="password" id="newPassword" name="newPassword" required minlength="8">
      </div>
      <div class="form-group">
        <label for="confirmNewPassword">Confirm new password</label>
        <input type="password" id="confirmNewPassword" name="confirmNewPassword" required minlength="8">
      </div>
      <button type="submit" class="btn btn-primary">Update password</button>
    </form>
  </div>

  <div class="card">
    <h2>Delivery addresses</h2>
    <c:choose>
      <c:when test="${empty addresses}">
        <p class="muted">No saved addresses yet.</p>
      </c:when>
      <c:otherwise>
        <c:forEach var="a" items="${addresses}">
          <div class="card" style="margin-bottom:10px; box-shadow:none; border:1px solid #e5e7eb;">
            <div class="flex-between">
              <strong>${fn:escapeXml(a.label)}</strong>
              <c:if test="${a.defaultAddress}"><span class="badge badge-green">Default</span></c:if>
            </div>
            <div class="muted">${fn:escapeXml(a.formatted)}</div>
            <form method="post" action="${pageContext.request.contextPath}/profile" style="margin-top:8px;">
              <input type="hidden" name="csrfToken" value="${csrfToken}">
              <input type="hidden" name="action" value="deleteAddress">
              <input type="hidden" name="addressId" value="${a.addressId}">
              <button type="submit" class="btn btn-danger btn-sm" onclick="return confirm('Remove this address?');">Remove</button>
            </form>
          </div>
        </c:forEach>
      </c:otherwise>
    </c:choose>

    <h3 style="margin-top:18px;">Add a new address</h3>
    <form method="post" action="${pageContext.request.contextPath}/profile">
      <input type="hidden" name="csrfToken" value="${csrfToken}">
      <input type="hidden" name="action" value="addAddress">
      <div class="form-group">
        <label for="label">Label</label>
        <input type="text" id="label" name="label" placeholder="Home / Work">
      </div>
      <div class="form-group">
        <label for="line1">Address line 1</label>
        <input type="text" id="line1" name="line1" required>
      </div>
      <div class="form-group">
        <label for="line2">Address line 2 (optional)</label>
        <input type="text" id="line2" name="line2">
      </div>
      <div class="form-group">
        <label for="city">City</label>
        <input type="text" id="city" name="city" required>
      </div>
      <div class="form-group">
        <label for="state">State</label>
        <input type="text" id="state" name="state" required>
      </div>
      <div class="form-group">
        <label for="pincode">Pincode</label>
        <input type="text" id="pincode" name="pincode" required>
      </div>
      <div class="form-group">
        <label><input type="checkbox" name="isDefault" style="width:auto;display:inline-block;"> Set as default</label>
      </div>
      <button type="submit" class="btn btn-secondary">Add address</button>
    </form>
  </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
