<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header"><h1>${empty product ? 'Add Product' : 'Edit Product'}</h1></div>

<div class="card" style="max-width:560px;">
  <form method="post" action="${pageContext.request.contextPath}/admin/products/save">
    <input type="hidden" name="csrfToken" value="${csrfToken}">
    <c:if test="${not empty product}"><input type="hidden" name="productId" value="${product.productId}"></c:if>

    <div class="form-group">
      <label for="name">Name</label>
      <input type="text" id="name" name="name" value="${fn:escapeXml(product.name)}" required>
    </div>
    <div class="form-group">
      <label for="sku">SKU</label>
      <input type="text" id="sku" name="sku" value="${fn:escapeXml(product.sku)}" required>
    </div>
    <div class="form-group">
      <label for="categoryId">Category</label>
      <select id="categoryId" name="categoryId" required>
        <c:forEach var="cat" items="${categories}">
          <option value="${cat.categoryId}" ${product.categoryId == cat.categoryId ? 'selected' : ''}>${fn:escapeXml(cat.name)}</option>
        </c:forEach>
      </select>
    </div>
    <div class="form-group">
      <label for="description">Description</label>
      <textarea id="description" name="description" rows="2">${fn:escapeXml(product.description)}</textarea>
    </div>
    <div class="form-group">
      <label for="unit">Unit</label>
      <input type="text" id="unit" name="unit" value="${fn:escapeXml(product.unit)}" placeholder="1 kg / 500 ml / each" required>
    </div>
    <div class="form-group">
      <label for="price">Price (₹)</label>
      <input type="text" id="price" name="price" value="${product.price}" required>
    </div>
    <c:choose>
      <c:when test="${empty product}">
        <div class="form-group">
          <label for="stockQty">Initial stock quantity</label>
          <input type="number" id="stockQty" name="stockQty" value="0" min="0" required>
        </div>
      </c:when>
      <c:otherwise>
        <div class="form-group">
          <label>Stock quantity</label>
          <p class="form-hint">${product.stockQty} ${fn:escapeXml(product.unit)} on hand &mdash;
            <a href="${pageContext.request.contextPath}/staff/inventory">adjust stock here</a> so the change is recorded in the audit trail.</p>
        </div>
      </c:otherwise>
    </c:choose>
    <div class="form-group">
      <label for="reorderLevel">Reorder level</label>
      <input type="number" id="reorderLevel" name="reorderLevel" value="${empty product ? 10 : product.reorderLevel}" min="0" required>
    </div>
    <div class="form-group">
      <label for="imageUrl">Image URL (optional)</label>
      <input type="text" id="imageUrl" name="imageUrl" value="${fn:escapeXml(product.imageUrl)}">
    </div>
    <div class="form-group">
      <label><input type="checkbox" name="active" ${empty product || product.active ? 'checked' : ''} style="width:auto;display:inline-block;"> Active (visible in store)</label>
    </div>

    <button type="submit" class="btn btn-primary">Save product</button>
  </form>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
