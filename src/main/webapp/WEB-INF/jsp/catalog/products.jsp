<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header">
  <div>
    <h1>Shop groceries</h1>
    <p class="muted">Fresh produce, dairy, bakery and more.</p>
  </div>
  <form method="get" action="${pageContext.request.contextPath}/products">
    <c:if test="${not empty selectedCategoryId}"><input type="hidden" name="categoryId" value="${selectedCategoryId}"></c:if>
    <input type="text" name="keyword" value="${fn:escapeXml(keyword)}" placeholder="Search products...">
    <button type="submit" class="btn btn-secondary">Search</button>
  </form>
</div>

<div class="tag-row">
  <a class="tag-link ${empty selectedCategoryId ? 'active' : ''}" href="${pageContext.request.contextPath}/products">All</a>
  <c:forEach var="cat" items="${categories}">
    <a class="tag-link ${selectedCategoryId == cat.categoryId ? 'active' : ''}"
       href="${pageContext.request.contextPath}/products?categoryId=${cat.categoryId}">${fn:escapeXml(cat.name)}</a>
  </c:forEach>
</div>

<c:choose>
  <c:when test="${empty products}">
    <div class="empty-state">
      <div class="icon">🧺</div>
      <p>No products match your search.</p>
    </div>
  </c:when>
  <c:otherwise>
    <div class="grid grid-products">
      <c:forEach var="p" items="${products}">
        <a class="card product-card" href="${pageContext.request.contextPath}/products/view?id=${p.productId}">
          <div class="thumb">
            <c:choose>
              <c:when test="${not empty p.imageUrl}"><img src="${fn:escapeXml(p.imageUrl)}" alt="${fn:escapeXml(p.name)}"></c:when>
              <c:otherwise>🛒</c:otherwise>
            </c:choose>
          </div>
          <h3>${fn:escapeXml(p.name)}</h3>
          <div class="muted">${fn:escapeXml(p.unit)}</div>
          <div class="flex-between">
            <span class="price">₹${p.price}</span>
            <c:choose>
              <c:when test="${p.stockQty <= 0}"><span class="badge badge-red">Out of stock</span></c:when>
              <c:when test="${p.lowStock}"><span class="badge badge-amber">Low stock</span></c:when>
            </c:choose>
          </div>
        </a>
      </c:forEach>
    </div>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
