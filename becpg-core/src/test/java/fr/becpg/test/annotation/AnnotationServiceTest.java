package fr.becpg.test.annotation;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.repo.jscript.AnnotationScriptHelper;
import fr.becpg.test.RepoBaseTestCase;

public class AnnotationServiceTest extends RepoBaseTestCase {

	private Log logger = LogFactory.getLog(AnnotationServiceTest.class);

	@Resource
	private AnnotationScriptHelper annotationScriptHelper;

	//@Test
	public void testAnnotation() {
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				NodeRef nodeRef = nodeService
						.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ContentModel.TYPE_CONTENT)
						.getChildRef();
				
				nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, "file.pdf");

				ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
				if (writer != null) {

					ClassPathResource resource = new ClassPathResource("beCPG/file.pdf");

					writer.setMimetype("application/pdf");
					writer.putContent(resource.getInputStream());

					if (logger.isDebugEnabled()) {
						logger.debug("File successfully modified");
					}
				} else {
					logger.error("Cannot write node");
				}
								
				ScriptNode scriptNode = new ScriptNode(nodeRef,serviceRegistry);
				String documentIdentifier = annotationScriptHelper.uploadDocument(scriptNode);
				assertNotNull(documentIdentifier);
				String sessionUrl = annotationScriptHelper.createSession(scriptNode, "admin", 1);
				assertNotNull(sessionUrl);
				annotationScriptHelper.exportDocument(scriptNode);
				annotationScriptHelper.deleteDocument(scriptNode);
				return null;
			}
		}, false, true);
	}

}
