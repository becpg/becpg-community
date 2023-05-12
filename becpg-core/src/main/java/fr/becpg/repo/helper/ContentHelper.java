/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import fr.becpg.repo.RepoConsts;

/**
 * <p>ContentHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ContentHelper {

	private static final Log logger = LogFactory.getLog(ContentHelper.class);

	private ContentService contentService;

	private MimetypeService mimetypeService;

	private NodeService nodeService;

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>mimetypeService</code>.</p>
	 *
	 * @param mimetypeService a {@link org.alfresco.service.cmr.repository.MimetypeService} object.
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>addFilesResources.</p>
	 *
	 * @param folderNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param pattern a {@link java.lang.String} object.
	 * @return a {@link java.util.List} object.
	 */
	public List<NodeRef> addFilesResources(NodeRef folderNodeRef, String pattern) {
		return addFilesResources(folderNodeRef, pattern, false);
	}

	/**
	 * <p>addFilesResources.</p>
	 *
	 * @param folderNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param pattern a {@link java.lang.String} object.
	 * @param forceUpdate a boolean.
	 * @return a {@link java.util.List} object.
	 */
	public List<NodeRef> addFilesResources(NodeRef folderNodeRef, String pattern, boolean forceUpdate) {
		List<NodeRef> ret = new ArrayList<>();

		try {

			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

			Set<String> resourcesUpdatedFromFile = new HashSet<>();
			
			boolean doUpdate = forceUpdate;
			for (Resource res : resolver.getResources(pattern)) {

				String fileName = res.getFilename();
				if (fileName != null) {
					Map<QName, Serializable> properties = new HashMap<>();
					properties.put(ContentModel.PROP_NAME, fileName);

					NodeRef nodeRef = nodeService.getChildByName(folderNodeRef, ContentModel.ASSOC_CONTAINS,
							(String) properties.get(ContentModel.PROP_NAME));
					if (nodeRef == null) {
						nodeRef = nodeService.createNode(folderNodeRef, ContentModel.ASSOC_CONTAINS,
								QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
										QName.createValidLocalName((String) properties.get(ContentModel.PROP_NAME))),
								ContentModel.TYPE_CONTENT, properties).getChildRef();
						doUpdate = true;
						logger.debug("Creating file " + fileName + " " + nodeRef);
					} else if(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE) || 
							RepoConsts.INITIAL_VERSION.equals(nodeService.getProperty(nodeRef,ContentModel.PROP_VERSION_LABEL))) {
						doUpdate = true;
						logger.debug("Updating file " + fileName + " " + nodeRef);
					}
					
					ret.add(nodeRef);

					if (doUpdate) {
						
						if (!res.isFile() && resourcesUpdatedFromFile.contains(res.getFilename())) {
							logger.debug("Do not update file " + fileName + " as it was already updated from file resource");
						} else {
							ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
							
							try (InputStream in = res.getInputStream()) {
								writer.setMimetype(mimetypeService.guessMimetype(fileName));
								if (fileName.endsWith(".csv")) {
									writer.setEncoding(RepoConsts.ISO_CHARSET);
								}
								writer.putContent(in);
							}
							if (res.isFile()) {
								resourcesUpdatedFromFile.add(res.getFilename());
							}
						}
						
					}
					doUpdate = forceUpdate;
				}
			}
		} catch ( InvalidTypeException | InvalidQNameException | ContentIOException | InvalidNodeRefException | IOException e) {
			logger.error(e, e);
		}
		return ret;

	}

}
