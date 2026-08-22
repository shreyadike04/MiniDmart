<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header"><h1>Admin Dashboard</h1></div>

<div class="stat-grid">
  <div class="stat-card"><div class="num">${totalUsers}</div><div class="label">Users</div></div>
  <div class="stat-card"><div class="num">${totalProducts}</div><div class="label">Products</div></div>
  <div class="stat-card"><div class="num">${totalOrders}</div><div class="label">Orders</div></div>
  <div class="stat-card"><div class="num">₹${completedRevenue}</div><div class="label">Completed revenue</div></div>
  <div class="stat-card"><div class="num">${lowStockCount}</div><div class="label">Low stock items</div></div>
  <div class="stat-card"><div class="num">${pendingReturns}</div><div class="label">Pending returns</div></div>
</div>

<div class="tag-row">
  <a class="tag-link" href="${pageContext.request.contextPath}/admin/users">Users</a>
  <a class="tag-link" href="${pageContext.request.contextPath}/admin/products">Products</a>
  <a class="tag-link" href="${pageContext.request.contextPath}/admin/categories">Categories</a>
  <a class="tag-link" href="${pageContext.request.contextPath}/admin/slots">Pickup slots</a>
  <a class="tag-link" href="${pageContext.request.contextPath}/admin/audit">Audit log</a>
</div>

<div class="page-header"><h2>Recent orders</h2></div>
<div class="table-wrap">
<table>
  <tr><th>Order #</th><th>Customer</th><th>Type</th><th>Status</th><th>Total</th><th>Placed</th></tr>
  <c:forEach var="o" items="${recentOrders}">
    <tr>
      <td>${o.orderNumber}</td>
      <td>${fn:escapeXml(o.customerName)}</td>
      <td>${o.fulfillmentType}</td>
      <td><span class="badge badge-gray">${o.status}</span></td>
      <td>₹${o.totalAmount}</td>
      <td>${o.placedAt}</td>
    </tr>
  </c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
