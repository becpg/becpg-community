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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.entity.remote.RemoteServiceRegisty;
import net.sf.acegisecurity.AccessDeniedException;

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

	protected Map<NodeRef, Set<QName>> cachedAssocRef = null;

	protected final Set<NodeRef> cacheList = new HashSet<>();

	/**
	 * <p>isLight.</p>
	 *
	 * @return a boolean
	 */
	protected boolean isLight() {
		return RemoteEntityFormat.xml_light.equals(params.getFormat());
	}

	/**
	 * <p>isAll.</p>
	 *
	 * @return a boolean
	 */
	protected boolean isAll() {
		return RemoteEntityFormat.xml_all.equals(params.getFormat()) || RemoteEntityFormat.json_all.equals(params.getFormat());
	}

	/**
	 * <p>Getter for the field <code>params</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.entity.remote.RemoteParams} object
	 */
	public RemoteParams getParams() {
		return params;
	}

	/** {@inheritDoc} */
	@Override
	public void setParams(RemoteParams params) {
		this.params = params;
	}

	/**
	 * <p>Constructor for AbstractEntityVisitor.</p>
	 *
	 * @param remoteServiceRegisty a {@link fr.becpg.repo.entity.remote.RemoteServiceRegisty} object
	 */
	protected AbstractEntityVisitor(RemoteServiceRegisty remoteServiceRegisty) {
		super();
		this.mlNodeService = remoteServiceRegisty.mlNodeService();
		this.nodeService = remoteServiceRegisty.nodeService();
		this.namespaceService = remoteServiceRegisty.namespaceService();
		this.entityDictionaryService = remoteServiceRegisty.entityDictionaryService();
		this.contentService = remoteServiceRegisty.contentService();
		this.siteService = remoteServiceRegisty.siteService();
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
	
	protected NodeRef getPrimaryParentRef(NodeRef nodeRef) {
		try {
			return Optional.ofNullable(nodeService.getPrimaryParent(nodeRef)).map(ChildAssociationRef::getParentRef)
					.orElse(null);
		} catch (final AccessDeniedException e) {
			throw new RuntimeException(String.format("Cannot read entity %s's primary parent", nodeRef.toString()), e);
		}
	}
}
