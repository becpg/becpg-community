/*
 * 
 */
package fr.becpg.repo.web.scripts.search;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
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
import fr.becpg.repo.web.scripts.search.data.BlogDataExtractor;
import fr.becpg.repo.web.scripts.search.data.CalendarDataExtractor;
import fr.becpg.repo.web.scripts.search.data.ContentDataExtractor;
import fr.becpg.repo.web.scripts.search.data.DataListDataExtractor;
import fr.becpg.repo.web.scripts.search.data.ForumDataExtractor;
import fr.becpg.repo.web.scripts.search.data.LinkDataExtractor;
import fr.becpg.repo.web.scripts.search.data.NodeDataExtractor;
import fr.becpg.repo.web.scripts.search.data.WikiDataExtractor;

/**
 * Webscript that send the result of a search
 * 
 * @author matthieu
 */
public class SearchWebScript extends AbstractSearchWebScript {

	/** The logger. */
	private static Log logger = LogFactory.getLog(SearchWebScript.class);

	private ServiceRegistry serviceRegistry;

	private AttributeExtractorService attributeExtractorService;

	
	/**
	 * @param attributeExtractorService the propertyService to set
	 */

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}


	/**
	 * @param serviceRegistry
	 *            the serviceRegistry to set
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * @param req
	 *            the req
	 * @param res
	 *            the res
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
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
		String fields = req.getParameter(PARAM_FIELDS);
		List<String> metadataFields = new LinkedList<String>();

		if (fields != null && fields.length() > 0) {
			String[] splitted = fields.split(",");
			for (String field : splitted) {
				metadataFields.add(field.replace("_", ":"));
			}
		}
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
		
		for (Iterator<NodeRef> iterator = results.iterator(); iterator.hasNext();) {
			NodeRef nodeRef = (NodeRef) iterator.next();
			if(serviceRegistry.getNodeService().exists(nodeRef)){
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
			if (container.equals("blog")) {
				return new BlogDataExtractor(serviceRegistry,attributeExtractorService);
			} else if (container.equals("discussions")) {
				return new ForumDataExtractor(serviceRegistry,attributeExtractorService);
			} else if (container.equals("calendar")) {
				return new CalendarDataExtractor(serviceRegistry,attributeExtractorService);
			} else if (container.equals("wiki")) {
				return new WikiDataExtractor(serviceRegistry,attributeExtractorService);
			} else if (container.equals("links")) {
				return new LinkDataExtractor(serviceRegistry,attributeExtractorService);
			} else if (container.equals("dataLists")) {
				return new DataListDataExtractor(serviceRegistry,attributeExtractorService);
			}
		}

		return new ContentDataExtractor(metadataFields, serviceRegistry,attributeExtractorService);

	}

}
