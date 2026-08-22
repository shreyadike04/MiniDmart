<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header"><h1>My Orders</h1></div>

<c:choose>
  <c:when test="${empty orders}">
    <div class="empty-state">
      <div class="icon">📦</div>
      <p>No orders yet.</p>
      <a class="btn btn-primary" href="${pageContext.request.contextPath}/products">Start shopping</a>
    </div>
  </c:when>
  <c:otherwise>
    <div class="table-wrap">
    <table>
      <tr><th>Order #</th><th>Type</th><th>Status</th><th>Total</th><th>Placed</th><th></th></tr>
      <c:forEach var="o" items="${orders}">
        <tr>
          <td><a href="${pageContext.request.contextPath}/orders/view?id=${o.orderId}">${fn:escapeXml(o.orderNumber)}</a></td>
          <td>${o.fulfillmentType}</td>
          <td>
            <c:choose>
              <c:when test="${o.status == 'PLACED' || o.status == 'CONFIRMED' || o.status == 'PREPARING'}">
                <span class="badge badge-amber">${o.status}</span>
              </c:when>
              <c:when test="${o.status == 'READY_FOR_PICKUP' || o.status == 'OUT_FOR_DELIVERY' || o.status == 'COMPLETED'}">
                <span class="badge badge-green">${o.status}</span>
              </c:when>
              <c:when test="${o.status == 'CANCELLED'}">
                <span class="badge badge-red">${o.status}</span>
              </c:when>
              <c:otherwise><span class="badge badge-gray">${o.status}</span></c:otherwise>
            </c:choose>
          </td>
          <td>₹${o.totalAmount}</td>
          <td>${o.placedAt}</td>
          <td><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/orders/view?id=${o.orderId}">View</a></td>
        </tr>
      </c:forEach>
    </table>
    </div>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
