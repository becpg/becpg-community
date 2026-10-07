<%@page session="false" import="java.util.Arrays, java.util.List"%><%
// OAuth2 discovery of the beCPG AI module (MCP server), served from the host root.
//
// RFC 9728 requires an MCP client to look for the metadata documents at the ROOT of the
// resource host, inserting the well-known prefix before the resource path:
//
//   resource https://<host>/ai/<instance>/mcp/sse
//   -> https://<host>/.well-known/oauth-protected-resource/ai/<instance>/mcp/sse
//
// The AI module is deployed under /ai of the instance host (server.servlet.context-path),
// as a separate backend behind the ingress, and Tomcat has no context for /.well-known:
// such a request lands here and would 404, which makes the client fall back to the default
// endpoints of the host (/authorize, /token) and fail. So redirect it to the module,
// dropping the /ai segment the client copied from the resource path: MCP clients follow
// redirects, and the module resolves the instance from what remains of the path.
//
// Requests that are not an AI metadata lookup keep their regular 404.
final List<String> METADATA_TYPES = Arrays.asList(
        "oauth-protected-resource", "oauth-authorization-server", "openid-configuration");
final String AI_PATH = "ai";

String pathInfo = request.getPathInfo();
String location = null;

if (pathInfo != null) {
    // "/<type>/<ai>/<resource path>"
    String[] segments = pathInfo.split("/", 4);
    if ((segments.length == 4) && METADATA_TYPES.contains(segments[1]) && AI_PATH.equals(segments[2])
            // Only plain path characters, so the redirect cannot be steered elsewhere.
            && segments[3].matches("[A-Za-z0-9._~/-]+") && (segments[3].indexOf("..") == -1)) {
        location = "/" + AI_PATH + "/.well-known/" + segments[1] + "/" + segments[3];
    }
}

if (location != null) {
    response.sendRedirect(location);
} else {
    response.sendError(404);
}
%>
