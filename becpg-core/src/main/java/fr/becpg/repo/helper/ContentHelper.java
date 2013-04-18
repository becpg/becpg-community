package fr.becpg.repo.helper;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import fr.becpg.repo.RepoConsts;

public class ContentHelper {

	private static Log logger = LogFactory.getLog(ContentHelper.class);

	private ContentService contentService;

	private MimetypeService mimetypeService;

	private NodeService nodeService;

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void addFilesResources(NodeRef folderNodeRef, String pattern) {
		addFilesResources(folderNodeRef, pattern, false);
	}

	public void addFilesResources(NodeRef folderNodeRef, String pattern, boolean forceUpdate) {
		try {

			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

			for (Resource res : resolver.getResources(pattern)) {

				String fileName = res.getFilename();
				logger.debug("add file " + fileName);

				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(ContentModel.PROP_NAME, fileName);

				NodeRef nodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS, (String) properties.get(ContentModel.PROP_NAME));
				if (nodeRef == null) {
					nodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName((String) properties.get(ContentModel.PROP_NAME))),
							ContentModel.TYPE_CONTENT, properties).getChildRef();
					forceUpdate = true;

				}

				if (forceUpdate) {
					ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);

					try (InputStream in = res.getInputStream()) {
						writer.setMimetype(mimetypeService.guessMimetype(fileName));
						if (fileName.endsWith(".csv")) {
							writer.setEncoding(RepoConsts.ISO_CHARSET);
						}
						writer.putContent(in);
					}

				}
			}
		} catch (Exception e) {
			logger.error(e, e);
		}

	}

}
