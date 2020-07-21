/*
 * 
 */
package fr.becpg.repo.web.scripts.search;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.extractors.BlogDataExtractor;
import fr.becpg.repo.helper.extractors.CalendarDataExtractor;
import fr.becpg.repo.helper.extractors.ContentDataExtractor;
import fr.becpg.repo.helper.extractors.DataListDataExtractor;
import fr.becpg.repo.helper.extractors.ForumDataExtractor;
import fr.becpg.repo.helper.extractors.LinkDataExtractor;
import fr.becpg.repo.helper.extractors.NodeDataExtractor;
import fr.becpg.repo.helper.extractors.WikiDataExtractor;
import fr.becpg.repo.web.scripts.WebscriptHelper;

/**
 * Webscript that send the result of a search
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SearchWebScript extends AbstractSearchWebScript {

	
	private static final Log logger = LogFactory.getLog(SearchWebScript.class);

	private ServiceRegistry serviceRegistry;

	private AttributeExtractorService attributeExtractorService;

	
	/**
	 * <p>Setter for the field <code>attributeExtractorService</code>.</p>
	 *
	 * @param attributeExtractorService the propertyService to set
	 */
	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}


	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry
	 *            the serviceRegistry to set
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {


		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
			logger.debug("SearchWebScript executeImpl()");
		}
		
		Integer maxResults = getNumParameter(req, PARAM_MAX_RESULTS);
		Integer page = getNumParameter(req, PARAM_PAGE);
		Integer pageSize = getNumParameter(req, PARAM_PAGE_SIZE);
		List<String> metadataFields =  WebscriptHelper.extractMetadataFields(req);
		
		try {
			List<NodeRef> results = doSearch(req, maxResults);
			
			if (page == null) {
				page = 1;
			}

			if (pageSize == null) {
				pageSize = 25;
			}
			int size = results.size();

			// Pagination
			if (size > 0) {
				results = results
						.subList(Math.max((page - 1) * pageSize, 0), Math.min(page * pageSize, size));
			}

			JSONObject ret = processResults(results, metadataFields);
			ret.put("page", page);
			ret.put("pageSize", pageSize);
			ret.put("fullListSize", size);
			
			res.setContentType("application/json");
            res.setContentEncoding("UTF-8");
			res.getWriter().write(ret.toString(3));
			
			

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON");
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug(   "SearchWebScript execute in "
						+ watch.getTotalTimeSeconds() +"s");
			}
		}

	}


	private Integer getNumParameter(WebScriptRequest req, String paramName) {
		String param = req.getParameter(paramName);

		Integer ret = null;
		if (param != null) {
			try {
				ret = Integer.parseInt(param);
			} catch (NumberFormatException e) {
				logger.error("Cannot parse page argument", e);
			}
		}
		return ret;
	}

	private JSONObject processResults(List<NodeRef> results, List<String> metadataFields)
			throws InvalidNodeRefException, JSONException {

		JSONArray items = new JSONArray();

		for (NodeRef nodeRef : results) {
			if (serviceRegistry.getNodeService().exists(nodeRef) && serviceRegistry.getPermissionService().hasPermission(nodeRef, "Read") == AccessStatus.ALLOWED) {
				items.put(new JSONObject(getExtractor(nodeRef, metadataFields).extract(nodeRef)));
			}
		}

		JSONObject obj = new JSONObject();
		obj.put("items", items);
		return obj;

	}


	private NodeDataExtractor getExtractor(NodeRef nodeRef, List<String> metadataFields) {

		String path = serviceRegistry.getNodeService().getPath(nodeRef).toPrefixString(namespaceService);

		String container = SiteHelper.extractContainerId(path);
		if (container != null) {
			switch (container) {
				case "blog":
					return new BlogDataExtractor(serviceRegistry, attributeExtractorService);
				case "discussions":
					return new ForumDataExtractor(serviceRegistry, attributeExtractorService);
				case "calendar":
					return new CalendarDataExtractor(serviceRegistry, attributeExtractorService);
				case "wiki":
					return new WikiDataExtractor(serviceRegistry, attributeExtractorService);
				case "links":
					return new LinkDataExtractor(serviceRegistry, attributeExtractorService);
				case "dataLists":
					return new DataListDataExtractor(serviceRegistry, attributeExtractorService);
			}
		}

		return new ContentDataExtractor(metadataFields, serviceRegistry,attributeExtractorService);

	}

}
