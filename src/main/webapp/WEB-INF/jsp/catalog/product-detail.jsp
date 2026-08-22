<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="grid" style="grid-template-columns: 320px 1fr; align-items:start;">
  <div class="card">
    <div class="thumb" style="height:220px;">
      <c:choose>
        <c:when test="${not empty product.imageUrl}"><img src="${fn:escapeXml(product.imageUrl)}" alt="${fn:escapeXml(product.name)}"></c:when>
        <c:otherwise><img src="${pageContext.request.contextPath}/assets/images/categories/${product.categoryIconSlug}.svg" alt="${fn:escapeXml(product.name)}"></c:otherwise>
      </c:choose>
    </div>
  </div>
  <div class="card">
    <h1 style="margin-top:0;">${fn:escapeXml(product.name)}</h1>
    <p class="muted">${fn:escapeXml(product.categoryName)} &middot; ${fn:escapeXml(product.unit)} &middot; SKU ${fn:escapeXml(product.sku)}</p>
    <p>${fn:escapeXml(product.description)}</p>
    <div class="price" style="font-size:1.6rem;">₹${product.price}</div>

    <c:choose>
      <c:when test="${product.stockQty <= 0 || !product.active}">
        <p class="badge badge-red" style="margin-top:12px;">Out of stock</p>
      </c:when>
      <c:when test="${sessionScope.role == 'CUSTOMER'}">
        <form method="post" action="${pageContext.request.contextPath}/cart/add" style="margin-top:16px;" class="qty-form">
          <input type="hidden" name="csrfToken" value="${csrfToken}">
          <input type="hidden" name="productId" value="${product.productId}">
          <input type="hidden" name="redirect" value="${fn:escapeXml(currentPath)}">
          <label for="quantity" style="margin:0;">Qty</label>
          <input type="number" id="quantity" name="quantity" value="1" min="1" max="${product.stockQty}">
          <button type="submit" class="btn btn-primary">Add to cart</button>
        </form>
        <p class="form-hint">${product.stockQty} available</p>
      </c:when>
      <c:when test="${sessionScope.userId == null}">
        <a class="btn btn-primary" style="margin-top:16px;"
           href="${pageContext.request.contextPath}/login?redirect=${loginRedirect}">
          Sign in to add to cart
        </a>
      </c:when>
    </c:choose>
  </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
