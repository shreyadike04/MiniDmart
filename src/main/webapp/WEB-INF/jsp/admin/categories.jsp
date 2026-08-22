<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>

<div class="page-header"><h1>Categories</h1></div>

<div class="card" style="margin-bottom:18px; max-width:520px;">
  <h2>Add category</h2>
  <form method="post" action="${pageContext.request.contextPath}/admin/categories/save">
    <input type="hidden" name="csrfToken" value="${csrfToken}">
    <div class="form-group">
      <label for="name">Name</label>
      <input type="text" id="name" name="name" required>
    </div>
    <div class="form-group">
      <label for="description">Description</label>
      <input type="text" id="description" name="description">
    </div>
    <button type="submit" class="btn btn-primary">Add category</button>
  </form>
</div>

<div class="table-wrap">
<table>
  <tr><th>Name</th><th>Description</th><th>Actions</th></tr>
  <c:forEach var="cat" items="${categories}">
    <%-- A <form> can't legally span <td> boundaries: the HTML5 "close the cell"
         parsing rule force-closes any element opened inside a cell (including an
         unclosed form) as soon as </td> is hit, so everything after the first cell
         would silently end up outside the form. Instead, one save-form's <input>s
         are linked to it by id via the form="" attribute from the other cells,
         and delete gets its own self-contained form in the last cell. --%>
    <c:set var="editFormId" value="editCategory${cat.categoryId}"/>
    <tr>
      <td style="min-width:160px;">
        <form id="${editFormId}" method="post" action="${pageContext.request.contextPath}/admin/categories/save">
          <input type="hidden" name="csrfToken" value="${csrfToken}">
          <input type="hidden" name="categoryId" value="${cat.categoryId}">
        </form>
        <input type="text" name="name" form="${editFormId}" value="${fn:escapeXml(cat.name)}" style="width:140px;" required>
      </td>
      <td>
        <input type="text" name="description" form="${editFormId}" value="${fn:escapeXml(cat.description)}" style="width:220px;">
      </td>
      <td>
        <button type="submit" form="${editFormId}" class="btn btn-secondary btn-sm">Save</button>
        <form method="post" action="${pageContext.request.contextPath}/admin/categories/delete" style="display:inline;" onsubmit="return confirm('Delete this category?');">
          <input type="hidden" name="csrfToken" value="${csrfToken}">
          <input type="hidden" name="categoryId" value="${cat.categoryId}">
          <button type="submit" class="btn btn-danger btn-sm">Delete</button>
        </form>
      </td>
    </tr>
  </c:forEach>
</table>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jsp" %>
