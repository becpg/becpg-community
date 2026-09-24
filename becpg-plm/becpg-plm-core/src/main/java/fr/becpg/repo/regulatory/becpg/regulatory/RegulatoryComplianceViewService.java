package fr.becpg.repo.regulatory.becpg.regulatory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;


import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

/**
 * Serves the embedded compliance view: computes the compliance of a product with
 * becpg-regulatory on demand, whatever the regulatory mode of the product, and never
 * writes anything in the repository.
 *
 * @author matthieu
 */
@Service
public class RegulatoryComplianceViewService {

	private static final Log logger = LogFactory.getLog(RegulatoryComplianceViewService.class);

	/** How long reading the recipe took on this instance, shown by the view next to the regulatory steps. */
	static final String KEY_RECIPE_FETCH_MS = "recipeFetchMs";

	private final BecpgRegulatoryClient regulatoryClient;

	/**
	 * @param regulatoryClient the becpg-regulatory client
	 */
	public RegulatoryComplianceViewService(BecpgRegulatoryClient regulatoryClient) {
		this.regulatoryClient = regulatoryClient;
	}

	/**
	 * @return the URL of the web application serving the embedded view, empty when the view
	 *         is disabled on this instance
	 */
	public Optional<String> uiUrl() {
		return regulatoryClient.uiUrl();
	}

	/**
	 * Computes the compliance view of a product, with the rights of the current user.
	 *
	 * @param productNodeRef the product
	 * @param refresh true to bypass the result cache of the regulatory service
	 * @return the compliance view, as JSON
	 * @throws JSONException if the product cannot be serialized
	 */
	public String fetchView(NodeRef productNodeRef, boolean refresh) throws JSONException {
		if (logger.isDebugEnabled()) {
			logger.debug("Computing regulatory compliance view of " + productNodeRef + (refresh ? " (refresh)" : ""));
		}
		long start = System.nanoTime();
		JSONObject recipe = regulatoryClient.fetchRecipe(productNodeRef);
		long recipeFetchMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
		JSONObject view = new JSONObject(regulatoryClient.checkView(recipe, refresh));
		view.put(KEY_RECIPE_FETCH_MS, recipeFetchMs);
		return view.toString();
	}
}
