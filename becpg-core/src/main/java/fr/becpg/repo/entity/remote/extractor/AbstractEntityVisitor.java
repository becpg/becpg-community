/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 *
 * @author matthieu
 *
 */
public abstract class AbstractEntityVisitor {

	protected final NodeService mlNodeService;
	protected final NodeService nodeService;
	protected final NamespaceService namespaceService;
	protected final DictionaryService dictionaryService;
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

	public void setDumpAll(boolean dumpAll) {
		this.dumpAll = dumpAll;
	}

	public void setLight(boolean light) {
		this.light = light;
	}

	public void setFilteredLists(List<String> filteredLists) {
		this.filteredLists = filteredLists;
	}

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

	protected boolean isValidQNameString(String qName) {
		String[] qnameArray = qName.split(":");
		if ((qName.indexOf(":") > 0) && (qnameArray.length > 1)) {
			return true;
		}
		return false;
	}

	public AbstractEntityVisitor(NodeService mlNodeService, NodeService nodeService, NamespaceService namespaceService,
			DictionaryService dictionaryService, ContentService contentService, SiteService siteService) {
		super();
		this.mlNodeService = mlNodeService;
		this.nodeService = nodeService;
		this.namespaceService = namespaceService;
		this.dictionaryService = dictionaryService;
		this.contentService = contentService;
		this.siteService = siteService;
	}

	public abstract void visit(NodeRef entityNodeRef, OutputStream result) throws Exception;

	public abstract void visit(List<NodeRef> entities, OutputStream result) throws Exception;

	public abstract void visitData(NodeRef entityNodeRef, OutputStream result) throws Exception;

	protected String writeCDATA(String attribute) {
		return attribute != null
				? attribute.replace("&", "&amp;").replace("\"", "&quot;").replace("\'", "&apos;").replace("<", "&lt;").replace(">", "&gt;")
				: "";
	}

	protected boolean shouldDumpAll(NodeRef nodeRef) {
		QName nodeType = nodeService.getType(nodeRef).getPrefixedQName(namespaceService);

		return dumpAll && !cacheList.contains(nodeRef) && !(ContentModel.TYPE_AUTHORITY.equals(nodeType) || ContentModel.TYPE_PERSON.equals(nodeType)
				|| ContentModel.TYPE_AUTHORITY_CONTAINER.equals(nodeType));
	}

}
