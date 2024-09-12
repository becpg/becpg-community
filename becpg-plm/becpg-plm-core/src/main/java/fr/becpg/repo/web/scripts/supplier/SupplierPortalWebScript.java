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
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.search.PaginatedSearchCache;
import fr.becpg.repo.supplier.SupplierPortalService;

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
	
	private static final String PROP_PJT_TPL = "projectTpl";

	private static final Log logger = LogFactory.getLog(SupplierPortalWebScript.class);

	private SupplierPortalService supplierPortalService;

	private PaginatedSearchCache paginatedSearchCache;

	/**
	 * <p>Setter for the field <code>paginatedSearchCache</code>.</p>
	 *
	 * @param paginatedSearchCache a {@link fr.becpg.repo.search.PaginatedSearchCache} object.
	 */
	public void setPaginatedSearchCache(PaginatedSearchCache paginatedSearchCache) {
		this.paginatedSearchCache = paginatedSearchCache;
	}

	/**
	 * <p>Setter for the field <code>supplierPortalService</code>.</p>
	 *
	 * @param supplierPortalService a {@link fr.becpg.repo.supplier.SupplierPortalService} object
	 */
	public void setSupplierPortalService(SupplierPortalService supplierPortalService) {
		this.supplierPortalService = supplierPortalService;
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
				if ((json != null) && json.has(PROP_PJT_TPL) && (json.getString(PROP_PJT_TPL) != null) && !json.getString(PROP_PJT_TPL).isEmpty()) {
					projectTemplateNodeRef = new NodeRef(json.getString(PROP_PJT_TPL));
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
				projectNodeRef = supplierPortalService.createSupplierProject(nodeRef, projectTemplateNodeRef,null);
			}

		} else if (entityNodeRef != null) {
			projectNodeRef = supplierPortalService.createSupplierProject(entityNodeRef, projectTemplateNodeRef,null);
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

}
