package fr.becpg.repo.project.web.scripts;

import java.io.IOException;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;

import fr.becpg.model.ProjectModel;
import jakarta.servlet.http.HttpServletResponse;

/**
 * <p>TaskEditUrlWebScript class.</p>
 *
 * @author valentin
 * @version $Id: $Id
 */
public class ProjectTaskEditUrlWebScript extends AbstractWebScript {

	private NodeService nodeService;

	private SysAdminParams sysAdminParams;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		NodeRef entityNodeRef = new NodeRef(req.getParameter("nodeRef"));

		HttpServletResponse httpResponse = WebScriptServletRuntime.getHttpServletResponse(resp);

		httpResponse.sendRedirect(buildShareUrl() + "/page/task-edit?taskId=" + nodeService.getProperty(entityNodeRef, ProjectModel.PROP_TL_WORKFLOW_TASK_INSTANCE));
	}

	private String buildShareUrl() {
		StringBuilder url = new StringBuilder();
		url.append(sysAdminParams.getShareProtocol());
		url.append("://");
		url.append(sysAdminParams.getAlfrescoHost());
		if ("http".equals(sysAdminParams.getShareProtocol()) && sysAdminParams.getSharePort() == 80) {
			// Not needed
		} else if ("https".equals(sysAdminParams.getShareProtocol()) && sysAdminParams.getSharePort() == 443) {
			// Not needed
		} else {
			url.append(':');
			url.append(sysAdminParams.getSharePort());
		}
		url.append('/');
		url.append(sysAdminParams.getShareContext());

		return url.toString();
	}

}
