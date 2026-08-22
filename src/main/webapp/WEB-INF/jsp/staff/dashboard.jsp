<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header"><h1>Staff Dashboard</h1></div>

<div class="stat-grid">
  <div class="stat-card"><div class="num">${newOrders}</div><div class="label">New orders</div></div>
  <div class="stat-card"><div class="num">${inProgressOrders}</div><div class="label">In progress</div></div>
  <div class="stat-card"><div class="num">${readyOrders}</div><div class="label">Ready / out</div></div>
  <div class="stat-card"><div class="num">${upcomingPickups}</div><div class="label">Upcoming pickups</div></div>
  <div class="stat-card"><div class="num">${activeDeliveries}</div><div class="label">Active deliveries</div></div>
  <div class="stat-card"><div class="num">${lowStockCount}</div><div class="label">Low stock items</div></div>
  <div class="stat-card"><div class="num">${pendingReturns}</div><div class="label">Pending returns</div></div>
</div>

<div class="tag-row">
  <a class="tag-link" href="${pageContext.request.contextPath}/staff/orders">All orders</a>
  <a class="tag-link" href="${pageContext.request.contextPath}/staff/pickups">Pickups</a>
  <a class="tag-link" href="${pageContext.request.contextPath}/staff/deliveries">Deliveries</a>
  <a class="tag-link" href="${pageContext.request.contextPath}/staff/inventory">Inventory</a>
  <a class="tag-link" href="${pageContext.request.contextPath}/staff/returns">Returns &amp; exchanges</a>
</div>

<div class="page-header"><h2>Recent orders</h2></div>
<div class="table-wrap">
<table>
  <tr><th>Order #</th><th>Customer</th><th>Type</th><th>Status</th><th>Total</th><th>Placed</th></tr>
  <c:forEach var="o" items="${recentOrders}">
    <tr>
      <td><a href="${pageContext.request.contextPath}/staff/orders?id=${o.orderId}">${o.orderNumber}</a></td>
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
