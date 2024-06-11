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
package fr.becpg.repo.entity.remote.extractor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteParams;

/**
 * <p>Abstract AbstractEntityVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractEntityVisitor implements RemoteEntityVisitor {

	protected final NodeService mlNodeService;
	protected final NodeService nodeService;
	protected final NamespaceService namespaceService;
	protected final EntityDictionaryService entityDictionaryService;
	protected final ContentService contentService;
	protected final SiteService siteService;

	protected RemoteParams params;

	protected boolean entityList = false;
	protected int extractLevel = 0;

	protected Map<NodeRef, List<QName>> cachedAssocRef = null;

	protected final Set<NodeRef> cacheList = new HashSet<>();

	protected boolean isLight() {
		return RemoteEntityFormat.xml_light.equals(params.getFormat());
	}

	protected boolean isAll() {
		return RemoteEntityFormat.xml_all.equals(params.getFormat()) || RemoteEntityFormat.json_all.equals(params.getFormat());
	}

	public RemoteParams getParams() {
		return params;
	}

	@Override
	public void setParams(RemoteParams params) {
		this.params = params;
	}

	/**
	 * <p>Constructor for AbstractEntityVisitor.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 * @param siteService a {@link org.alfresco.service.cmr.site.SiteService} object.
	 */
	protected AbstractEntityVisitor(NodeService mlNodeService, NodeService nodeService, NamespaceService namespaceService,
			EntityDictionaryService entityDictionaryService, ContentService contentService, SiteService siteService) {
		super();
		this.mlNodeService = mlNodeService;
		this.nodeService = nodeService;
		this.namespaceService = namespaceService;
		this.entityDictionaryService = entityDictionaryService;
		this.contentService = contentService;
		this.siteService = siteService;
	}

	/**
	 * <p>shouldDumpAll.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	protected boolean shouldDumpAll(NodeRef nodeRef) {
		QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);

		return isAll() && !cacheList.contains(nodeRef) && !(ContentModel.TYPE_AUTHORITY.equals(nodeType) || ContentModel.TYPE_PERSON.equals(nodeType)
				|| ContentModel.TYPE_AUTHORITY_CONTAINER.equals(nodeType));
	}

}
