package fr.becpg.repo.web.scripts.product;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.LCAListDataItem;
import fr.becpg.repo.product.formulation.lca.LCADatabaseService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * <p>LCADatabaseImportWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LCADatabaseImportWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(LCADatabaseImportWebScript.class);

	private static final String SUPPLIER_PARAM = "supplier";

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	private LCADatabaseService lcaDatabaseService;

	/**
	 * <p>Setter for the field <code>lcaDatabaseService</code>.</p>
	 *
	 * @param lcaDatabaseService a {@link fr.becpg.repo.product.formulation.lca.LCADatabaseService} object
	 */
	public void setLcaDatabaseService(LCADatabaseService lcaDatabaseService) {
		this.lcaDatabaseService = lcaDatabaseService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) {
		try {

			JSONObject json = (JSONObject) req.parseContent();
			String entities = "";

			if ((json != null) && json.has("entities")) {
				entities = (String) json.get("entities");
			}

			final NodeRef destNodeRef;
			String destination = req.getParameter("dest");
			if (destination != null) {
				destNodeRef = new NodeRef(destination);

				JSONArray ret = new JSONArray();

				if ((json != null) && json.has(SUPPLIER_PARAM) && (json.getString(SUPPLIER_PARAM) != null)
						&& !json.getString(SUPPLIER_PARAM).isEmpty()) {
					NodeRef databaseNodeRef = new NodeRef(json.getString(SUPPLIER_PARAM));
					
					for (final String entity : entities.split(",")) {
						logger.debug("using entity: " + entity);
						
						List<LCAListDataItem> lcaItems = lcaDatabaseService.extractLCAList(databaseNodeRef, entity);
						Double score = lcaDatabaseService.extractScore(databaseNodeRef, entity);
						String method = lcaDatabaseService.getMethod(databaseNodeRef);
						
						if (!lcaItems.isEmpty()) {
							logger.debug("Importing LCA in product");
							L2CacheSupport.doInCacheContext(() -> {
								
								ProductData productData = alfrescoRepository.findOne(destNodeRef);
								productData.getLcaList().clear();
								productData.getLcaList().addAll(lcaItems);
								
								productData.setLcaScore(score);
								productData.setLcaScoreMethod(method);
								
								alfrescoRepository.save(productData);
								
							}, false, true);
							break;
						}
					}
					res.setContentType("application/json");
					res.setContentEncoding("UTF-8");
					res.getWriter().write(ret.toString());
				}
			}

		} catch (JSONException | IOException e) {
			throw new WebScriptException(e.getMessage());
		}
	}

}
