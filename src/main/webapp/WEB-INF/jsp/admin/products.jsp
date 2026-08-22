<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header">
  <h1>Products</h1>
  <a class="btn btn-primary" href="${pageContext.request.contextPath}/admin/products/new">Add product</a>
</div>

<div class="table-wrap">
<table>
  <tr><th>SKU</th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th><th></th></tr>
  <c:forEach var="p" items="${products}">
    <tr>
      <td>${fn:escapeXml(p.sku)}</td>
      <td>${fn:escapeXml(p.name)}</td>
      <td>${fn:escapeXml(p.categoryName)}</td>
      <td>₹${p.price}</td>
      <td>${p.stockQty} <c:if test="${p.lowStock}"><span class="badge badge-amber">Low</span></c:if></td>
      <td><span class="badge ${p.active ? 'badge-green' : 'badge-gray'}">${p.active ? 'Active' : 'Inactive'}</span></td>
      <td><a class="btn btn-secondary btn-sm" href="${pageContext.request.contextPath}/admin/products/edit?id=${p.productId}">Edit</a></td>
    </tr>
  </c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
