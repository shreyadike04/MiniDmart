<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="card form-card" style="max-width:560px;">
  <h1>Return / Exchange</h1>
  <p class="muted">Order ${fn:escapeXml(orderNumber)} &middot; ${fn:escapeXml(productName)}</p>
  <p class="form-hint">You can request a return or exchange within <%= com.minidmart.dao.ReturnDao.ELIGIBILITY_DAYS %> days of your order being completed.</p>

  <form method="post" action="${pageContext.request.contextPath}/returns/new">
    <input type="hidden" name="csrfToken" value="${csrfToken}">
    <input type="hidden" name="orderItemId" value="${orderItemId}">

    <fieldset>
      <div class="form-group" style="padding:10px;">
        <label>Request type</label>
        <label style="font-weight:normal;"><input type="radio" name="type" value="RETURN" checked onchange="toggleExchange()" style="width:auto;display:inline-block;"> Return for refund</label><br>
        <label style="font-weight:normal;"><input type="radio" name="type" value="EXCHANGE" onchange="toggleExchange()" style="width:auto;display:inline-block;"> Exchange for another product</label>
      </div>
    </fieldset>

    <div class="form-group" id="exchangeGroup" style="display:none;">
      <label for="exchangeProductId">Replacement product</label>
      <select id="exchangeProductId" name="exchangeProductId">
        <option value="">-- choose a product --</option>
        <c:forEach var="p" items="${exchangeProducts}">
          <option value="${p.productId}">${fn:escapeXml(p.name)} &mdash; ₹${p.price} (${p.stockQty} in stock)</option>
        </c:forEach>
      </select>
    </div>

    <div class="form-group">
      <label for="quantity">Quantity (max ${maxQuantity})</label>
      <input type="number" id="quantity" name="quantity" min="1" max="${maxQuantity}" value="1" required>
    </div>

    <div class="form-group">
      <label for="reason">Reason</label>
      <textarea id="reason" name="reason" rows="3" required></textarea>
    </div>

    <button type="submit" class="btn btn-primary btn-block">Submit request</button>
  </form>
</div>

<script>
function toggleExchange() {
  var isExchange = document.querySelector('input[name="type"]:checked').value === 'EXCHANGE';
  document.getElementById('exchangeGroup').style.display = isExchange ? 'block' : 'none';
  document.getElementById('exchangeProductId').required = isExchange;
}
</script>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
