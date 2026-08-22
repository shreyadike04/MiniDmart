<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header"><h1>Checkout</h1></div>

<div class="grid" style="grid-template-columns: 1fr 340px; align-items:start;">
  <form method="post" action="${pageContext.request.contextPath}/checkout" id="checkoutForm">
    <input type="hidden" name="csrfToken" value="${csrfToken}">

    <div class="card" style="margin-bottom:16px;">
      <h2>Fulfillment method</h2>
      <div class="form-group">
        <label><input type="radio" name="fulfillmentType" value="PICKUP" checked
                      onclick="document.getElementById('pickupBlock').style.display='block';document.getElementById('deliveryBlock').style.display='none';
                               document.getElementById('pickupFeeLine').style.display='flex';document.getElementById('deliveryFeeLine').style.display='none';"
                      style="width:auto;display:inline-block;"> Store pickup (free)</label>
        <label><input type="radio" name="fulfillmentType" value="DELIVERY" ${empty addresses ? 'disabled' : ''}
                      onclick="document.getElementById('pickupBlock').style.display='none';document.getElementById('deliveryBlock').style.display='block';
                               document.getElementById('pickupFeeLine').style.display='none';document.getElementById('deliveryFeeLine').style.display='flex';"
                      style="width:auto;display:inline-block;"> Home delivery (₹${deliveryFee})</label>
      </div>

      <div id="pickupBlock">
        <h3>Choose a pickup slot</h3>
        <c:choose>
          <c:when test="${empty slots}">
            <p class="muted">No upcoming pickup slots available. Try home delivery instead.</p>
          </c:when>
          <c:otherwise>
            <c:forEach var="slot" items="${slots}">
              <label style="display:block; margin-bottom:6px;">
                <input type="radio" name="pickupSlotId" value="${slot.slotId}" style="width:auto;display:inline-block;">
                ${slot.slotDate} &middot; ${slot.startTime} - ${slot.endTime}
                <span class="muted">(${slot.remaining} slots left)</span>
              </label>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>

      <div id="deliveryBlock" style="display:none;">
        <h3>Choose a delivery address</h3>
        <c:choose>
          <c:when test="${empty addresses}">
            <p class="muted">No saved addresses. <a href="${pageContext.request.contextPath}/profile">Add one in your profile</a> first.</p>
          </c:when>
          <c:otherwise>
            <c:forEach var="addr" items="${addresses}">
              <label style="display:block; margin-bottom:6px;">
                <input type="radio" name="addressId" value="${addr.addressId}" ${addr.defaultAddress ? 'checked' : ''} style="width:auto;display:inline-block;">
                <strong>${fn:escapeXml(addr.label)}</strong> - ${fn:escapeXml(addr.formatted)}
              </label>
            </c:forEach>
          </c:otherwise>
        </c:choose>
      </div>
    </div>

    <button type="submit" class="btn btn-primary btn-block">Place order</button>
  </form>

  <div class="card">
    <h2>Order summary</h2>
    <c:forEach var="item" items="${items}">
      <div class="flex-between" style="margin-bottom:6px;">
        <span>${fn:escapeXml(item.productName)} &times; ${item.quantity}</span>
        <span>₹${item.lineTotal}</span>
      </div>
    </c:forEach>
    <hr>
    <div class="flex-between"><span>Subtotal</span><span>₹${subtotal}</span></div>
    <div class="flex-between muted" id="pickupFeeLine"><span>Pickup fee</span><span>₹0.00</span></div>
    <div class="flex-between muted" id="deliveryFeeLine" style="display:none;"><span>Delivery fee</span><span>₹${deliveryFee}</span></div>
    <p class="form-hint">The final fee is always confirmed on the server when you place the order.</p>
  </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
