/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

/**
 * <p>Abstract AbstractEntityVisitor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractEntityVisitor {

	protected final NodeService mlNodeService;
	protected final NodeService nodeService;
	protected final NamespaceService namespaceService;
	protected final EntityDictionaryService entityDictionaryService;
	protected final ContentService contentService;
	protected final SiteService siteService;

	private boolean dumpAll = false;
	protected boolean light = false;
	protected boolean entityList = false;
	protected int extractLevel = 0;

	protected List<QName> filteredProperties = new ArrayList<>();
	protected List<String> filteredLists = new ArrayList<>();
	protected Map<QName, List<QName>> filteredAssocProperties = new HashMap<>();
	protected Map<NodeRef, List<QName>> cachedAssocRef = null;

	protected final Set<NodeRef> cacheList = new HashSet<>();

	/**
	 * <p>Setter for the field <code>dumpAll</code>.</p>
	 *
	 * @param dumpAll a boolean.
	 */
	public void setDumpAll(boolean dumpAll) {
		this.dumpAll = dumpAll;
	}

	/**
	 * <p>Setter for the field <code>light</code>.</p>
	 *
	 * @param light a boolean.
	 */
	public void setLight(boolean light) {
		this.light = light;
	}

	/**
	 * <p>Setter for the field <code>filteredLists</code>.</p>
	 *
	 * @param filteredLists a {@link java.util.List} object.
	 */
	public void setFilteredLists(List<String> filteredLists) {
		this.filteredLists = filteredLists;
	}

	/**
	 * <p>setFilteredFields.</p>
	 *
	 * @param fields a {@link java.util.List} object.
	 */
	public void setFilteredFields(List<String> fields) {

		if ((fields != null) && !fields.isEmpty()) {
			for (String el : fields) {
				String[] assoc = el.split("\\|");
				if (!isValidQNameString(assoc[0])) {
					continue;
				}
				QName propQname = QName.createQName(assoc[0], namespaceService);
				if ((assoc != null) && (assoc.length > 1)) {
					if (!isValidQNameString(assoc[1])) {
						continue;
					}
					QName assocPropQName = QName.createQName(assoc[1], namespaceService);
					if (filteredAssocProperties.containsKey(propQname)) {
						filteredAssocProperties.get(propQname).add(assocPropQName);
					} else {
						List<QName> tmp = new ArrayList<>();
						tmp.add(assocPropQName);
						filteredAssocProperties.put(propQname, tmp);
					}
				} else {
					filteredProperties.add(propQname);
				}
			}
		}

	}

	/**
	 * <p>isValidQNameString.</p>
	 *
	 * @param qName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	protected boolean isValidQNameString(String qName) {
		String[] qnameArray = qName.split(":");
		if ((qName.indexOf(":") > 0) && (qnameArray.length > 1)) {
			return true;
		}
		return false;
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
	public AbstractEntityVisitor(NodeService mlNodeService, NodeService nodeService, NamespaceService namespaceService,
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
	 * <p>visit.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @throws java.lang.Exception if any.
	 */
	public abstract void visit(NodeRef entityNodeRef, OutputStream result) throws Exception;

	/**
	 * <p>visit.</p>
	 *
	 * @param entities a {@link java.util.List} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @throws java.lang.Exception if any.
	 */
	public abstract void visit(List<NodeRef> entities, OutputStream result) throws Exception;

	/**
	 * <p>visitData.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param result a {@link java.io.OutputStream} object.
	 * @throws java.lang.Exception if any.
	 */
	public abstract void visitData(NodeRef entityNodeRef, OutputStream result) throws Exception;

	/**
	 * <p>writeCDATA.</p>
	 *
	 * @param attribute a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	protected String writeCDATA(String attribute) {
		return attribute != null
				? attribute.replace("&", "&amp;").replace("\"", "&quot;").replace("\'", "&apos;").replace("<", "&lt;").replace(">", "&gt;")
				: "";
	}
	

	/**
	 * <p>shouldDumpAll.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	protected boolean shouldDumpAll(NodeRef nodeRef) {
		QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);

		return dumpAll && !cacheList.contains(nodeRef) && !(ContentModel.TYPE_AUTHORITY.equals(nodeType) || ContentModel.TYPE_PERSON.equals(nodeType)
				|| ContentModel.TYPE_AUTHORITY_CONTAINER.equals(nodeType));
	}

}
