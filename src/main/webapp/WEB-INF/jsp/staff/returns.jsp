<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header">
  <h1>Returns &amp; Exchanges</h1>
  <a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/staff/dashboard">&larr; Dashboard</a>
</div>

<div class="tag-row">
  <a class="tag-link ${filter == 'pending' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/returns?filter=pending">Pending</a>
  <a class="tag-link ${filter == 'all' ? 'active' : ''}" href="${pageContext.request.contextPath}/staff/returns?filter=all">All</a>
</div>

<c:choose>
  <c:when test="${empty returns}">
    <div class="empty-state">
      <div class="icon">&#8617;</div>
      <p>No return or exchange requests.</p>
    </div>
  </c:when>
  <c:otherwise>
    <div class="table-wrap">
    <table>
      <tr>
        <th>Order #</th><th>Customer</th><th>Product</th><th>Type</th><th>Qty</th>
        <th>Reason</th><th>Status</th><th>Requested</th><th>Decision</th>
      </tr>
      <c:forEach var="r" items="${returns}">
        <tr>
          <td><a href="${pageContext.request.contextPath}/staff/orders?id=${r.orderId}">${r.orderNumber}</a></td>
          <td>${fn:escapeXml(r.customerName)}</td>
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
          <td>
            <c:if test="${r.status == 'REQUESTED'}">
              <form method="post" action="${pageContext.request.contextPath}/staff/returns">
                <input type="hidden" name="csrfToken" value="${csrfToken}">
                <input type="hidden" name="returnId" value="${r.returnId}">
                <div class="form-group" style="margin-bottom:8px;">
                  <textarea name="notes" placeholder="Staff notes (optional)" rows="2" style="width:100%;"></textarea>
                </div>
                <button type="submit" name="action" value="approve" class="btn btn-primary btn-sm">Approve</button>
                <button type="submit" name="action" value="reject" class="btn btn-danger btn-sm">Reject</button>
              </form>
            </c:if>
          </td>
        </tr>
      </c:forEach>
    </table>
    </div>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
