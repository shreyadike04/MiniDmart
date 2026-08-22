<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header">
  <h1>Deliveries</h1>
  <a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/staff/dashboard">&larr; Dashboard</a>
</div>

<c:choose>
  <c:when test="${empty orders}">
    <div class="empty-state">
      <div class="icon">🚚</div>
      <p>No active deliveries.</p>
    </div>
  </c:when>
  <c:otherwise>
    <div class="table-wrap">
    <table>
      <tr><th>Order #</th><th>Customer</th><th>City / area</th><th>Status</th></tr>
      <c:forEach var="o" items="${orders}">
        <tr>
          <td><a href="${pageContext.request.contextPath}/staff/orders?id=${o.orderId}">${o.orderNumber}</a></td>
          <td>${fn:escapeXml(o.customerName)}</td>
          <td>
            <c:choose>
              <c:when test="${not empty o.deliveryAddress}">${fn:escapeXml(o.deliveryAddress.city)}</c:when>
              <c:otherwise><span class="muted">&mdash;</span></c:otherwise>
            </c:choose>
          </td>
          <td>
            <c:choose>
              <c:when test="${o.status == 'OUT_FOR_DELIVERY'}"><span class="badge badge-green">${o.status}</span></c:when>
              <c:otherwise><span class="badge badge-amber">${o.status}</span></c:otherwise>
            </c:choose>
          </td>
        </tr>
      </c:forEach>
    </table>
    </div>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
