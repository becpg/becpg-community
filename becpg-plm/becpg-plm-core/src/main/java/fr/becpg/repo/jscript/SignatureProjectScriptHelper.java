package fr.becpg.repo.jscript;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.api.BeCPGPublicApi;
import fr.becpg.repo.signature.SignatureProjectService;

/**
 * <p>SignatureProjectScriptHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@BeCPGPublicApi
public class SignatureProjectScriptHelper extends BaseScopableProcessorExtension {

	private SignatureProjectService signatureProjectService;
	
	private ServiceRegistry serviceRegistry;

	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	/**
	 * <p>Setter for the field <code>signatureProjectService</code>.</p>
	 *
	 * @param signatureProjectService a {@link fr.becpg.repo.signature.SignatureProjectService} object
	 */
	public void setSignatureProjectService(SignatureProjectService signatureProjectService) {
		this.signatureProjectService = signatureProjectService;
	}
	
	/**
	 * <p>prepareSignatureProject.</p>
	 *
	 * @param project a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param documents an array of {@link org.alfresco.repo.jscript.ScriptNode} objects
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public ScriptNode prepareSignatureProject(ScriptNode project, ScriptNode[] documents) {
		
		if (documents != null && documents.length > 0) {
			
			List<NodeRef> documentNodeRefs = new ArrayList<>();
			
			for (ScriptNode document : documents) {
				documentNodeRefs.add(document.getNodeRef());
			}
		
			return new ActivitiScriptNode(signatureProjectService.prepareSignatureProject(project.getNodeRef(), documentNodeRefs), serviceRegistry);
		}
		return null;
	}
	
	/**
	 * <p>createEntitySignatureTasks.</p>
	 *
	 * @param project a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param task a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @param projectType a {@link java.lang.String} object
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public ScriptNode createEntitySignatureTasks(ScriptNode project, ScriptNode task, String projectType) {
		return new ActivitiScriptNode(signatureProjectService.createEntitySignatureTasks(project.getNodeRef(), task.getNodeRef(), projectType), serviceRegistry);
	}
	
	/**
	 * <p>extractRecipients.</p>
	 *
	 * @param items an array of {@link org.alfresco.repo.jscript.ScriptNode} objects
	 * @return an array of {@link org.alfresco.repo.jscript.ScriptNode} objects
	 */
	public ScriptNode[] extractRecipients(ScriptNode[] items) {
		if (items != null) {
			for (ScriptNode item : items) {

				List<NodeRef> recipients = signatureProjectService.extractRecipients(item.getNodeRef());

				if (!recipients.isEmpty()) {
					return recipients.stream().map(n -> new ActivitiScriptNode(n, serviceRegistry)).toArray(ScriptNode[]::new);
				}
			}
		}

		return new ScriptNode[0];
	}
}
