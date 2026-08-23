<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><c:out value="${pageTitle != null ? pageTitle : 'Mini D-Mart'}"/></title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
<nav class="navbar">
  <div class="container">
    <a class="brand" href="${pageContext.request.contextPath}/home">🛒 Mini D-Mart</a>
    <div class="nav-links">
      <c:choose>
        <c:when test="${sessionScope.userId == null}">
          <a href="${pageContext.request.contextPath}/products">Browse</a>
          <a href="${pageContext.request.contextPath}/login">Login</a>
          <a href="${pageContext.request.contextPath}/register">Register</a>
        </c:when>
        <c:when test="${sessionScope.role == 'ADMIN'}">
          <a href="${pageContext.request.contextPath}/admin/dashboard">Admin Dashboard</a>
          <a href="${pageContext.request.contextPath}/staff/dashboard">Staff Dashboard</a>
          <a href="${pageContext.request.contextPath}/products">Browse</a>
        </c:when>
        <c:when test="${sessionScope.role == 'STAFF'}">
          <a href="${pageContext.request.contextPath}/staff/dashboard">Staff Dashboard</a>
          <a href="${pageContext.request.contextPath}/products">Browse</a>
        </c:when>
        <c:otherwise>
          <a href="${pageContext.request.contextPath}/products">Browse</a>
          <a href="${pageContext.request.contextPath}/cart">Cart
            <c:if test="${cartCount != null && cartCount > 0}"><span class="cart-badge">${cartCount}</span></c:if>
          </a>
          <a href="${pageContext.request.contextPath}/orders">My Orders</a>
          <a href="${pageContext.request.contextPath}/returns">Returns</a>
        </c:otherwise>
      </c:choose>
      <c:if test="${sessionScope.userId != null}">
        <a href="${pageContext.request.contextPath}/profile">${sessionScope.fullName}</a>
        <span class="role-pill">${sessionScope.role}</span>
        <a href="${pageContext.request.contextPath}/logout">Logout</a>
      </c:if>
    </div>
  </div>
</nav>
<main class="main">
  <div class="container">
    <c:if test="${not empty flashError}">
      <div class="alert alert-error">${flashError}</div>
    </c:if>
    <c:if test="${not empty flashSuccess}">
      <div class="alert alert-success">${flashSuccess}</div>
    </c:if>
