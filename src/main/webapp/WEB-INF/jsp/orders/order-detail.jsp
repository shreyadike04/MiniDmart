<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header">
  <div>
    <h1>Order ${order.orderNumber}</h1>
    <p class="muted">Placed ${order.placedAt}</p>
  </div>
  <div>
    <c:choose>
      <c:when test="${order.status == 'PLACED' || order.status == 'CONFIRMED' || order.status == 'PREPARING'}">
        <span class="badge badge-amber">${order.status}</span>
      </c:when>
      <c:when test="${order.status == 'READY_FOR_PICKUP' || order.status == 'OUT_FOR_DELIVERY' || order.status == 'COMPLETED'}">
        <span class="badge badge-green">${order.status}</span>
      </c:when>
      <c:when test="${order.status == 'CANCELLED'}">
        <span class="badge badge-red">${order.status}</span>
      </c:when>
      <c:otherwise><span class="badge badge-gray">${order.status}</span></c:otherwise>
    </c:choose>
  </div>
</div>

<div class="grid" style="grid-template-columns: 2fr 1fr; align-items:start;">
  <div class="card">
    <h2>Items</h2>
    <div class="table-wrap" style="box-shadow:none;">
    <table>
      <tr><th>Product</th><th>Unit price</th><th>Qty</th><th>Line total</th><th></th></tr>
      <c:forEach var="item" items="${order.items}">
        <tr>
          <td>${fn:escapeXml(item.productNameSnap)}</td>
          <td>₹${item.unitPriceSnap}</td>
          <td>${item.quantity}</td>
          <td>₹${item.lineTotal}</td>
          <td>
            <c:if test="${item.returnEligible}">
              <a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/returns/new?orderItemId=${item.orderItemId}">Return / Exchange</a>
            </c:if>
          </td>
        </tr>
      </c:forEach>
    </table>
    </div>

    <c:if test="${order.status.cancellable}">
      <h3 style="margin-top:22px;">Cancel this order</h3>
      <form method="post" action="${pageContext.request.contextPath}/orders/cancel">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="orderId" value="${order.orderId}">
        <div class="form-group">
          <label for="reason">Reason</label>
          <input type="text" id="reason" name="reason" placeholder="Changed my mind" required>
        </div>
        <button type="submit" class="btn btn-danger" onclick="return confirm('Cancel this order?');">Cancel order</button>
      </form>
    </c:if>

    <c:if test="${order.status == 'CANCELLED'}">
      <div class="alert alert-info" style="margin-top:18px;">
        Cancelled ${order.cancelledAt}<c:if test="${not empty order.cancelReason}"> &mdash; ${fn:escapeXml(order.cancelReason)}</c:if>
      </div>
    </c:if>
  </div>

  <div class="card">
    <h2>Summary</h2>
    <table>
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
  </div>
</div>

<p style="margin-top:18px;"><a href="${pageContext.request.contextPath}/orders">&larr; Back to my orders</a></p>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
