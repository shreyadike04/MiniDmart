<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card" style="background:linear-gradient(135deg,#2e7d32,#66bb6a);color:#fff;margin-bottom:24px;">
  <h1 style="margin-top:0;">Fresh groceries. Pick up in-store or get them delivered.</h1>
  <p style="opacity:.95;max-width:560px;">Browse categories, build your cart, and choose a scheduled store pickup slot or home delivery at checkout.</p>
  <a class="btn" style="background:#fff;color:#1b5e20;" href="${pageContext.request.contextPath}/products">Start shopping</a>
</div>

<c:if test="${not empty categories}">
  <div class="tag-row">
    <c:forEach var="cat" items="${categories}">
      <a class="tag-link" href="${pageContext.request.contextPath}/products?categoryId=${cat.categoryId}">${fn:escapeXml(cat.name)}</a>
    </c:forEach>
  </div>
</c:if>

<div class="page-header"><h2>Featured products</h2></div>
<c:choose>
  <c:when test="${empty featuredProducts}">
    <div class="empty-state">
      <div class="icon">🧺</div>
      <p>No products available right now. Check back soon.</p>
    </div>
  </c:when>
  <c:otherwise>
    <div class="grid grid-products">
      <c:forEach var="p" items="${featuredProducts}">
        <a class="card product-card" href="${pageContext.request.contextPath}/products/view?id=${p.productId}">
          <div class="thumb">
            <c:choose>
              <c:when test="${not empty p.imageUrl}"><img src="${fn:escapeXml(p.imageUrl)}" alt="${fn:escapeXml(p.name)}"></c:when>
              <c:otherwise><img src="${pageContext.request.contextPath}/assets/images/categories/${p.categoryIconSlug}.svg" alt="${fn:escapeXml(p.name)}"></c:otherwise>
            </c:choose>
          </div>
          <h3>${fn:escapeXml(p.name)}</h3>
          <div class="muted">${fn:escapeXml(p.unit)}</div>
          <div class="flex-between">
            <span class="price">₹${p.price}</span>
            <c:if test="${p.stockQty <= 0}"><span class="badge badge-red">Out of stock</span></c:if>
          </div>
        </a>
      </c:forEach>
    </div>
  </c:otherwise>
</c:choose>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
