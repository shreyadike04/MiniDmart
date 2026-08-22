<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header">
  <h1>Returns &amp; Exchanges</h1>
</div>
<p class="muted">To request a return or exchange, open a completed order and use the "Return / Exchange" link next to the item.
  <a href="${pageContext.request.contextPath}/orders">Go to my orders &rarr;</a></p>

<c:choose>
  <c:when test="${empty returns}">
    <div class="empty-state">
      <div class="icon">↩️</div>
      <p>No return or exchange requests yet.</p>
    </div>
  </c:when>
  <c:otherwise>
    <div class="table-wrap">
    <table>
      <tr><th>Order #</th><th>Product</th><th>Type</th><th>Qty</th><th>Reason</th><th>Status</th><th>Requested</th></tr>
      <c:forEach var="r" items="${returns}">
        <tr>
          <td>${r.orderNumber}</td>
          <td>${fn:escapeXml(r.productName)}
            <c:if test="${r.type == 'EXCHANGE' && not empty r.exchangeProductName}">
              <div class="muted">for ${fn:escapeXml(r.exchangeProductName)}</div>
            </c:if>
          </td>
          <td><span class="badge badge-gray">${r.type}</span></td>
          <td>${r.quantity}</td>
          <td>${fn:escapeXml(r.reason)}</td>
          <td>
            <c:choose>
              <c:when test="${r.status == 'APPROVED' || r.status == 'COMPLETED'}"><span class="badge badge-green">${r.status}</span></c:when>
              <c:when test="${r.status == 'REJECTED'}"><span class="badge badge-red">${r.status}</span></c:when>
              <c:otherwise><span class="badge badge-amber">${r.status}</span></c:otherwise>
            </c:choose>
            <c:if test="${not empty r.staffNotes}"><div class="muted">${fn:escapeXml(r.staffNotes)}</div></c:if>
          </td>
          <td>${r.requestedAt}</td>
        </tr>
      </c:forEach>
    </table>
    </div>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
