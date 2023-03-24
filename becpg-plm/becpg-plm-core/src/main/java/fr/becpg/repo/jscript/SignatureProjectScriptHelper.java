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

@BeCPGPublicApi
public class SignatureProjectScriptHelper extends BaseScopableProcessorExtension {

	private SignatureProjectService signatureProjectService;
	
	private ServiceRegistry serviceRegistry;

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}
	
	public void setSignatureProjectService(SignatureProjectService signatureProjectService) {
		this.signatureProjectService = signatureProjectService;
	}
	
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
	
	public ScriptNode createEntitySignatureTasks(ScriptNode project, ScriptNode task, String projectType) {
		return new ActivitiScriptNode(signatureProjectService.createEntitySignatureTasks(project.getNodeRef(), task.getNodeRef(), projectType), serviceRegistry);
	}
	
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
