<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header"><h1>Your cart</h1></div>

<c:choose>
  <c:when test="${empty items}">
    <div class="empty-state">
      <div class="icon">🛒</div>
      <p>Your cart is empty.</p>
      <a class="btn btn-primary" href="${pageContext.request.contextPath}/products">Start shopping</a>
    </div>
  </c:when>
  <c:otherwise>
    <div class="table-wrap">
    <table>
      <tr><th>Product</th><th>Unit price</th><th>Quantity</th><th>Line total</th><th></th></tr>
      <c:forEach var="item" items="${items}">
        <tr>
          <td>
            ${fn:escapeXml(item.productName)} <span class="muted">(${fn:escapeXml(item.unit)})</span>
            <c:if test="${!item.productActive || item.quantity > item.stockQty}"><br><span class="badge badge-amber">Check availability before checkout</span></c:if>
          </td>
          <td>₹${item.price}</td>
          <td>
            <form method="post" action="${pageContext.request.contextPath}/cart/update" class="qty-form">
              <input type="hidden" name="csrfToken" value="${csrfToken}">
              <input type="hidden" name="productId" value="${item.productId}">
              <input type="number" name="quantity" value="${item.quantity}" min="0" max="${item.stockQty}" style="width:64px;">
              <button type="submit" class="btn btn-secondary btn-sm">Update</button>
            </form>
          </td>
          <td>₹${item.lineTotal}</td>
          <td>
            <form method="post" action="${pageContext.request.contextPath}/cart/remove">
              <input type="hidden" name="csrfToken" value="${csrfToken}">
              <input type="hidden" name="productId" value="${item.productId}">
              <button type="submit" class="btn btn-danger btn-sm">Remove</button>
            </form>
          </td>
        </tr>
      </c:forEach>
    </table>
    </div>

    <div class="card" style="margin-top:18px; max-width:360px; margin-left:auto;">
      <div class="flex-between"><strong>Subtotal</strong><span class="price">₹${subtotal}</span></div>
      <a class="btn btn-primary btn-block" style="margin-top:14px;" href="${pageContext.request.contextPath}/checkout">Proceed to checkout</a>
    </div>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
