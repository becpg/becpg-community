<%@page session="true" import="jakarta.servlet.ServletContext, jakarta.servlet.RequestDispatcher"%>
<%
boolean alfrescoInstalled = false;
ServletContext alfrescoContext = application.getContext("/alfresco");

if ((alfrescoContext != null) && !alfrescoContext.equals(getServletConfig().getServletContext())) {
    alfrescoInstalled = true;
}

if (request.getMethod().equalsIgnoreCase("PROPFIND") || request.getMethod().equalsIgnoreCase("OPTIONS")) {
    if (alfrescoInstalled) {
        RequestDispatcher rd = alfrescoContext.getRequestDispatcher("/AosResponder_ServerRoot");
        if (rd != null) {
            rd.forward(request, response);
            return;
        }
    }
} else {
    response.sendRedirect("/share/");
}
%>
