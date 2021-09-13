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
package fr.becpg.repo.web.scripts.supplier;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.jscript.SupplierPortalHelper;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.PaginatedSearchCache;

/**
 * <p>SupplierPortalWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SupplierPortalWebScript extends AbstractWebScript {

	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	private static final String PARAM_ALLPAGES = "allPages";

	private static final String PARAM_QUERY_EXECUTION_ID = "queryExecutionId";

	private static final String PARAM_NODEREFS = "nodeRefs";

	private static final String PARAM_TPL_NODEREF = "tplNodeRef";

	private static final Log logger = LogFactory.getLog(SupplierPortalWebScript.class);

	private NodeService nodeService;

	private AssociationService associationService;

	private RepoService repoService;

	private EntityVersionService entityVersionService;
	
	private SiteService siteService;

	private AlfrescoRepository<ProjectData> alfrescoRepository;

	private NamespaceService namespaceService;

	private PaginatedSearchCache paginatedSearchCache;
	
	private boolean createBranch;

	private String entityNameTpl = "{entity_cm:name} - UPDATE - {date_YYYY}";
	private String projectNameTpl = "{entity_cm:name} - {supplier_cm:name} - UPDATE - {date_YYYY}";

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>entityVersionService</code>.</p>
	 *
	 * @param entityVersionService a {@link fr.becpg.repo.entity.version.EntityVersionService} object.
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProjectData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>paginatedSearchCache</code>.</p>
	 *
	 * @param paginatedSearchCache a {@link fr.becpg.repo.search.PaginatedSearchCache} object.
	 */
	public void setPaginatedSearchCache(PaginatedSearchCache paginatedSearchCache) {
		this.paginatedSearchCache = paginatedSearchCache;
	}

	/**
	 * <p>Setter for the field <code>entityNameTpl</code>.</p>
	 *
	 * @param entityNameTpl a {@link java.lang.String} object.
	 */
	public void setEntityNameTpl(String entityNameTpl) {
		this.entityNameTpl = entityNameTpl;
	}

	/**
	 * <p>Setter for the field <code>projectNameTpl</code>.</p>
	 *
	 * @param projectNameTpl a {@link java.lang.String} object.
	 */
	public void setProjectNameTpl(String projectNameTpl) {
		this.projectNameTpl = projectNameTpl;
	}

	/**
	 * <p>Setter for the field <code>repoService</code>.</p>
	 *
	 * @param repoService a {@link fr.becpg.repo.helper.RepoService} object.
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}
	
	/**
	 * <p>Setter for the field <code>createBranch</code>.</p>
	 *
	 * @param createBranch a boolean.
	 */
	public void setCreateBranch(boolean createBranch) {
		this.createBranch = createBranch;
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException, IOException {

		logger.debug("Calling SupplierPortalWebScript");

		String entityNodeRefParam = req.getParameter(PARAM_ENTITY_NODEREF);
		String projectTemplateParam = req.getParameter(PARAM_TPL_NODEREF);

		NodeRef projectTemplateNodeRef = null;
		if (projectTemplateParam == null) {

			JSONObject json = (JSONObject) req.parseContent();

			try {
				if ((json != null) && json.has("projectTpl") && (json.getString("projectTpl") != null) && !json.getString("projectTpl").isEmpty()) {
					projectTemplateNodeRef = new NodeRef(json.getString("projectTpl"));
				}
			} catch (JSONException e) {
				throw new IllegalStateException(I18NUtil.getMessage("message.supplier.project-template"));
			}

		} else {
			projectTemplateNodeRef = new NodeRef(projectTemplateParam);
		}

		if (projectTemplateNodeRef == null) {
			throw new IllegalStateException(I18NUtil.getMessage("message.supplier.project-template"));
		}

		String allPagesParam = req.getParameter(PARAM_ALLPAGES);
		String queryExecutionId = req.getParameter(PARAM_QUERY_EXECUTION_ID);
		String nodeRefsParam = req.getParameter(PARAM_NODEREFS);

		List<NodeRef> nodeRefs = new ArrayList<>();

		if ((allPagesParam != null) && "true".equalsIgnoreCase(allPagesParam) && (queryExecutionId != null)) {
			nodeRefs = paginatedSearchCache.getSearchResults(queryExecutionId);
		} else if ((nodeRefsParam != null) && !nodeRefsParam.isEmpty()) {
			for (String nodeRefItem : nodeRefsParam.split(",")) {
				nodeRefs.add(new NodeRef(nodeRefItem));
			}
		}

		NodeRef entityNodeRef = null;
		if ((entityNodeRefParam != null) && !entityNodeRefParam.isEmpty()) {
			entityNodeRef = new NodeRef(entityNodeRefParam);
		}

		NodeRef projectNodeRef = null;

		if (!nodeRefs.isEmpty()) {

			for (NodeRef nodeRef : nodeRefs) {
				projectNodeRef = createProject(nodeRef, projectTemplateNodeRef);
			}

		} else if (entityNodeRef != null) {

			projectNodeRef = createProject(entityNodeRef, projectTemplateNodeRef);
		} else {
			throw new IllegalStateException(I18NUtil.getMessage("message.incorrect.paramater"));
		}

		try {
			JSONObject ret = new JSONObject();

			if (projectNodeRef != null) {
				ret.put("persistedObject", projectNodeRef);
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new IllegalStateException("Unable to serialize JSON", e);
		}

	}

	/**
	 * <p>createProject.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param projectTemplateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef createProject(NodeRef entityNodeRef, NodeRef projectTemplateNodeRef) {

		Date currentDate = Calendar.getInstance().getTime();

		NodeRef supplierNodeRef = checkSupplierNodeRef(entityNodeRef);

		NodeRef destNodeRef = associationService.getTargetAssoc(projectTemplateNodeRef, BeCPGModel.PROP_ENTITY_TPL_DEFAULT_DEST);
		if (destNodeRef == null) {
			throw new IllegalStateException(I18NUtil.getMessage("message.project-template.destination.missed"));
		}
		NodeRef branchNodeRef = entityNodeRef;
		if(createBranch) {
			
			String branchName = repoService.getAvailableName(destNodeRef, createName(entityNodeRef, supplierNodeRef, entityNameTpl, currentDate), false);
			
			NodeRef supplierDestFolder = getSupplierDestFolder(supplierNodeRef);
			
			if (supplierDestFolder != null && nodeService.getChildByName(supplierDestFolder, ContentModel.ASSOC_CONTAINS, branchName) != null) {
				throw new IllegalStateException(I18NUtil.getMessage("message.supplier.entity-already-exists"));
			}
			
			branchNodeRef = entityVersionService.createBranch(entityNodeRef, destNodeRef);
			associationService.update(branchNodeRef, BeCPGModel.ASSOC_AUTO_MERGE_TO, entityNodeRef);
			nodeService.setProperty(branchNodeRef, ContentModel.PROP_NAME, branchName);
		}
		

		ProjectData projectData = new ProjectData();
		projectData
				.setName(repoService.getAvailableName(destNodeRef, createName(entityNodeRef, supplierNodeRef, projectNameTpl, currentDate), false));
		projectData.setParentNodeRef(destNodeRef);
		projectData.setState(ProjectState.InProgress.toString());
		projectData.setEntities(Arrays.asList(branchNodeRef));
		projectData.setProjectTpl(projectTemplateNodeRef);

		if (logger.isDebugEnabled()) {
			logger.debug("Creating supplier portal project : " + projectData.getName());
		}

		return alfrescoRepository.save(projectData).getNodeRef();

	}

	private String createName(NodeRef entityNodeRef, NodeRef supplierNodeRef, String nameFormat, Date currentDate) {

		Matcher patternMatcher = Pattern.compile("\\{([^}]+)\\}").matcher(nameFormat);
		StringBuffer sb = new StringBuffer();
		while (patternMatcher.find()) {

			String propQname = patternMatcher.group(1);
			String replacement = "";
			if (propQname.contains("|")) {
				for (String propQnameAlt : propQname.split("\\|")) {
					replacement = extractPropText(propQnameAlt, entityNodeRef, supplierNodeRef, currentDate);
					if ((replacement != null) && !replacement.isEmpty()) {
						break;
					}
				}

			} else {
				replacement = extractPropText(propQname, entityNodeRef, supplierNodeRef, currentDate);
			}

			patternMatcher.appendReplacement(sb, replacement != null ? replacement.replace("$", "") : "");

		}
		patternMatcher.appendTail(sb);

		return sb.toString().replace("-  -", "-").replace("- -", "-").trim().replaceAll("\\-$|\\(\\)", "").trim().replaceAll("\\-$|\\(\\)", "")
				.trim();

	}

	private String extractPropText(String propQname, NodeRef entityNodeRef, NodeRef supplierNodeRef, Date currentDate) {
		if (propQname != null) {
			if ((propQname.indexOf("supplier_") == 0) && (supplierNodeRef != null) && !supplierNodeRef.equals(entityNodeRef)) {
				
				QName prop = QName.createQName(propQname.replace("supplier_", ""), namespaceService);
				
				String entityProp = (String) nodeService.getProperty(entityNodeRef, prop);
				String supplierProp = (String) nodeService.getProperty(supplierNodeRef, prop);
				
				// case of supplier name already contained in entity name
				if (entityProp != null && supplierProp != null && entityProp.toLowerCase().contains(supplierProp.toLowerCase())) {
					return null;
				}
				
				return supplierProp;
			} else if (propQname.indexOf("entity_") == 0) {
				return (String) nodeService.getProperty(entityNodeRef, QName.createQName(propQname.replace("entity_", ""), namespaceService));
			} else if ("date".equals(propQname)) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(propQname.replace("date_", ""));
				return dateFormat.format(currentDate);
			}

		}
		return "";
	}

	private NodeRef checkSupplierNodeRef(NodeRef entityNodeRef) {
		NodeRef supplierNodeRef = null;

		if (entityNodeRef != null) {

			if (PLMModel.TYPE_SUPPLIER.equals(nodeService.getType(entityNodeRef))) {
				supplierNodeRef = entityNodeRef;

			} else {
				supplierNodeRef = associationService.getTargetAssoc(entityNodeRef, PLMModel.ASSOC_SUPPLIERS);
			}

			if (supplierNodeRef != null) {

				List<NodeRef> accountNodeRefs = associationService.getTargetAssocs(supplierNodeRef, PLMModel.ASSOC_SUPPLIER_ACCOUNTS);
				if (accountNodeRefs == null || accountNodeRefs.isEmpty()) {
					throw new IllegalStateException(I18NUtil.getMessage("message.supplier-account.missed"));
				}

			} else {
				throw new IllegalStateException(I18NUtil.getMessage("message.supplier.missed"));
			}
		}

		return supplierNodeRef;
	}
	
	private NodeRef getSupplierDestFolder(NodeRef supplierNodeRef) {
		NodeRef destFolder = null;

		if (supplierNodeRef != null) {
			SiteInfo siteInfo = siteService.getSite(SupplierPortalHelper.SUPPLIER_SITE_ID);

			if (siteInfo != null) {

				Locale currentLocal = I18NUtil.getLocale();
				Locale currentContentLocal = I18NUtil.getContentLocale();
				
				try {
					I18NUtil.setLocale(Locale.getDefault());
					I18NUtil.setContentLocale(null);
					
					NodeRef documentLibraryNodeRef = siteService.getContainer(SupplierPortalHelper.SUPPLIER_SITE_ID, SiteService.DOCUMENT_LIBRARY);
					if (documentLibraryNodeRef != null) {
						
						destFolder = nodeService.getChildByName(documentLibraryNodeRef, ContentModel.ASSOC_CONTAINS, I18NUtil.getMessage("path.referencing"));
						
						if (destFolder != null) {
							
							String supplierName = (String) nodeService.getProperty(supplierNodeRef, ContentModel.PROP_NAME);
							
							destFolder = nodeService.getChildByName(destFolder, ContentModel.ASSOC_CONTAINS, supplierName);
						}
					}
				} finally {
					I18NUtil.setLocale(currentLocal);
					I18NUtil.setContentLocale(currentContentLocal);
				}

			}
		}

		return destFolder;
	}

}
