<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header">
  <h1>Orders</h1>
  <a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/staff/dashboard">&larr; Dashboard</a>
</div>

<c:choose>
  <c:when test="${not empty order}">
    <div class="page-header">
      <div>
        <h2>Order ${order.orderNumber}</h2>
        <p class="muted">${fn:escapeXml(order.customerName)} &middot; Placed ${order.placedAt}</p>
      </div>
      <div>
        <c:choose>
          <c:when test="${order.status == 'COMPLETED'}"><span class="badge badge-green">${order.status}</span></c:when>
          <c:when test="${order.status == 'CANCELLED'}"><span class="badge badge-red">${order.status}</span></c:when>
          <c:otherwise><span class="badge badge-amber">${order.status}</span></c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="card">
      <h3>Items</h3>
      <div class="table-wrap" style="box-shadow:none;">
      <table>
        <tr><th>Product</th><th>Unit price</th><th>Qty</th><th>Line total</th></tr>
        <c:forEach var="item" items="${order.items}">
          <tr>
            <td>${fn:escapeXml(item.productNameSnap)}</td>
            <td>₹${item.unitPriceSnap}</td>
            <td>${item.quantity}</td>
            <td>₹${item.lineTotal}</td>
          </tr>
        </c:forEach>
      </table>
      </div>

      <table style="margin-top:14px;">
        <tr><th>Subtotal</th><td>₹${order.subtotal}</td></tr>
        <tr><th>Delivery fee</th><td>₹${order.deliveryFee}</td></tr>
        <tr><th>Total</th><td><strong>₹${order.totalAmount}</strong></td></tr>
      </table>

      <h3 style="margin-top:18px;">Fulfillment</h3>
      <p><span class="badge badge-gray">${order.fulfillmentType}</span></p>
      <c:if test="${order.fulfillmentType == 'PICKUP' && not empty order.pickupSlot}">
        <p class="muted">${order.pickupSlot.slotDate} &middot; ${order.pickupSlot.startTime} - ${order.pickupSlot.endTime}</p>
      </c:if>
      <c:if test="${order.fulfillmentType == 'DELIVERY' && not empty order.deliveryAddress}">
        <p class="muted">${fn:escapeXml(order.deliveryAddress.formatted)}</p>
      </c:if>

      <c:if test="${not empty nextStatus}">
        <h3 style="margin-top:18px;">Advance status</h3>
        <form method="post" action="${pageContext.request.contextPath}/staff/orders">
          <input type="hidden" name="csrfToken" value="${csrfToken}">
          <input type="hidden" name="orderId" value="${order.orderId}">
          <input type="hidden" name="newStatus" value="${nextStatus}">
          <button type="submit" class="btn btn-primary">Mark as ${nextStatus}</button>
        </form>
      </c:if>
    </div>

    <p style="margin-top:18px;"><a href="${pageContext.request.contextPath}/staff/orders">&larr; Back to all orders</a></p>
  </c:when>

  <c:otherwise>
    <div class="tabs">
      <a href="${pageContext.request.contextPath}/staff/orders" class="${empty selectedStatus ? 'active' : ''}">All</a>
      <c:forEach var="s" items="${statuses}">
        <a href="${pageContext.request.contextPath}/staff/orders?status=${s}" class="${selectedStatus == s ? 'active' : ''}">${s}</a>
      </c:forEach>
    </div>

    <c:choose>
      <c:when test="${empty orders}">
        <div class="empty-state">
          <div class="icon">&#128230;</div>
          <p>No orders found.</p>
        </div>
      </c:when>
      <c:otherwise>
        <div class="table-wrap">
        <table>
          <tr><th>Order #</th><th>Customer</th><th>Type</th><th>Status</th><th>Total</th><th>Placed</th></tr>
          <c:forEach var="o" items="${orders}">
            <tr>
              <td><a href="${pageContext.request.contextPath}/staff/orders?id=${o.orderId}">${o.orderNumber}</a></td>
              <td>${fn:escapeXml(o.customerName)}</td>
              <td>${o.fulfillmentType}</td>
              <td>
                <c:choose>
                  <c:when test="${o.status == 'COMPLETED'}"><span class="badge badge-green">${o.status}</span></c:when>
                  <c:when test="${o.status == 'CANCELLED'}"><span class="badge badge-red">${o.status}</span></c:when>
                  <c:otherwise><span class="badge badge-amber">${o.status}</span></c:otherwise>
                </c:choose>
              </td>
              <td>₹${o.totalAmount}</td>
              <td>${o.placedAt}</td>
            </tr>
          </c:forEach>
        </table>
        </div>
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
