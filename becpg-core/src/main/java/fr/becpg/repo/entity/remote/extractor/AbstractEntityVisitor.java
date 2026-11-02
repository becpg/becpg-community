/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	private static final Log logger = LogFactory.getLog(AbstractEntityVisitor.class);

	protected final NodeService mlNodeService;
	protected final NodeService nodeService;
	protected final NodeService unsecuredNodeService;
	protected final NamespaceService namespaceService;
	protected final EntityDictionaryService entityDictionaryService;
	protected final ContentService contentService;
	protected final SiteService siteService;

	protected RemoteParams params;

	protected boolean entityList = false;
	protected int extractLevel = 0;

	protected Map<NodeRef, Set<QName>> cachedAssocRef = null;

	protected final Set<NodeRef> cacheList = new HashSet<>();

	private final RemoteFieldReadAccess fieldReadAccess;

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
		this.unsecuredNodeService = remoteServiceRegisty.unsecuredNodeService();
		this.namespaceService = remoteServiceRegisty.namespaceService();
		this.entityDictionaryService = remoteServiceRegisty.entityDictionaryService();
		this.contentService = remoteServiceRegisty.contentService();
		this.siteService = remoteServiceRegisty.siteService();
		this.fieldReadAccess = new RemoteFieldReadAccess(remoteServiceRegisty.securityService(), unsecuredNodeService, namespaceService);
	}

	/**
	 * Whether a field of a node is exported, as far as the security rules are concerned.
	 * <p>
	 * The rules are applied only when the caller asked for it, see
	 * {@link fr.becpg.repo.entity.remote.RemoteParams#isEnforceFieldSecurity()}.
	 *
	 * @param nodeRef the node holding the field
	 * @param fieldName the property or association
	 * @return false when a security rule hides the field from the current user
	 */
	protected boolean canReadField(NodeRef nodeRef, QName fieldName) {
		return !isFieldSecurityEnforced() || fieldReadAccess.canRead(nodeRef, fieldName);
	}

	/**
	 * Whether a field of a type is exported under the global security rules, for a serialized node.
	 *
	 * @param nodeType the type the field belongs to
	 * @param fieldName the property or association
	 * @return false when a security rule hides the field from the current user
	 */
	protected boolean canReadField(QName nodeType, QName fieldName) {
		return !isFieldSecurityEnforced() || (nodeType == null) || fieldReadAccess.canReadOnType(nodeType, fieldName);
	}

	/**
	 * Whether the caller asked for the field level security rules to be applied.
	 *
	 * @return a boolean
	 */
	protected boolean isFieldSecurityEnforced() {
		return (params != null) && params.isEnforceFieldSecurity();
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
	
	/**
	 * <p>getPrimaryParentRef.</p>
	 *
	 * <p>Both {@code AccessDeniedException} types are caught on purpose: the {@code NodeService} AOP
	 * proxy translates the acegi one into {@link org.alfresco.repo.security.permissions.AccessDeniedException}
	 * before it leaves, and the two classes share no hierarchy. Catching only the acegi type would
	 * let an unreadable parent escape through the whole visitor, ending the JSON export on an empty
	 * body with an HTTP 200 that can no longer be turned into an error.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @throws fr.becpg.repo.entity.remote.extractor.RemoteException if any.
	 */
	protected NodeRef getPrimaryParentRef(NodeRef nodeRef) throws RemoteException {
		try {
			return Optional.ofNullable(nodeService.getPrimaryParent(nodeRef)).map(ChildAssociationRef::getParentRef)
					.orElse(null);
		} catch (final AccessDeniedException | org.alfresco.repo.security.permissions.AccessDeniedException e) {
			throw new RemoteException(String.format("Cannot read entity %s's primary parent", nodeRef.toString()), e);
		}
	}

	/**
	 * The path of the folder of a node, when the current user may read that folder.
	 * <p>
	 * A node may be readable while its folder is not, a client or a supplier shared from a
	 * restricted site for instance. Its path is then unknown to the user and is left out: failing
	 * would end a streamed export in the middle of an element, on a response already committed.
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return the path of the primary parent, empty when the node has none or when the user cannot read it
	 */
	protected Optional<Path> findReadableParentPath(NodeRef nodeRef) {
		try {
			NodeRef parentRef = getPrimaryParentRef(nodeRef);
			return parentRef != null ? Optional.of(nodeService.getPath(parentRef)) : Optional.empty();
		} catch (RemoteException | AccessDeniedException | org.alfresco.repo.security.permissions.AccessDeniedException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("Folder of " + nodeRef + " not readable by the current user, path left out of the export");
			}
			return Optional.empty();
		}
	}
}
