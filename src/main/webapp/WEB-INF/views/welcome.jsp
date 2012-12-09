<%@page contentType="text/html;charset=UTF-8"%>
<%@page pageEncoding="UTF-8"%>
<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<html>
<head>
	<META http-equiv="Content-Type" content="text/html;charset=UTF-8">
	<title><fmt:message key="welcome.title"/></title>
</head>
<body>
<div class="container">  
	<h1>
		<fmt:message key="welcome.title"/>
	</h1>
	<hr>	
	<ul>
		<li><a href="rss">RSS</a></li>
		<li><a href="html">HTML</a></li>
	</ul>
</div>
</body>
</html>