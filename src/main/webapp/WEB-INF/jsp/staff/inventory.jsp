<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header">
  <h1>Inventory</h1>
  <a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/staff/dashboard">&larr; Dashboard</a>
</div>

<c:choose>
  <c:when test="${empty products}">
    <div class="empty-state">
      <div class="icon">📦</div>
      <p>No products found.</p>
    </div>
  </c:when>
  <c:otherwise>
    <div class="table-wrap">
    <table>
      <tr>
        <th>SKU</th><th>Name</th><th>Category</th><th>Stock</th><th>Reorder level</th>
        <th>Flags</th><th>Adjust stock</th>
      </tr>
      <c:forEach var="p" items="${products}">
        <tr>
          <td>${fn:escapeXml(p.sku)}</td>
          <td>${fn:escapeXml(p.name)}</td>
          <td>${fn:escapeXml(p.categoryName)}</td>
          <td>${p.stockQty} ${fn:escapeXml(p.unit)}</td>
          <td>${p.reorderLevel}</td>
          <td>
            <c:if test="${p.lowStock}"><span class="badge badge-amber">Low stock</span></c:if>
            <c:choose>
              <c:when test="${p.active}"><span class="badge badge-green">Active</span></c:when>
              <c:otherwise><span class="badge badge-gray">Inactive</span></c:otherwise>
            </c:choose>
          </td>
          <td>
            <form method="post" action="${pageContext.request.contextPath}/staff/inventory/adjust" style="display:flex; gap:6px; align-items:center; flex-wrap:wrap;">
              <input type="hidden" name="csrfToken" value="${csrfToken}">
              <input type="hidden" name="productId" value="${p.productId}">
              <input type="number" name="delta" placeholder="&plusmn;qty" required style="width:80px;">
              <input type="text" name="reason" placeholder="Reason" required style="width:130px;">
              <button type="submit" class="btn btn-primary btn-sm">Adjust</button>
            </form>
          </td>
        </tr>
      </c:forEach>
    </table>
    </div>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
