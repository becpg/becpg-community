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

import fr.becpg.repo.product.NutDatabaseService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

public class NutDatabaseImportWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(NutDatabaseImportWebScript.class);
	
	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;
	
	private NutDatabaseService nutDatabaseService;

	public void setNutDatabaseService(NutDatabaseService nutDatabaseService) {
		this.nutDatabaseService = nutDatabaseService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) {
		try {

			JSONObject json = (JSONObject) req.parseContent();
			String entities = "";
			
			if ((json != null) && json.has("entities")) {
				entities = (String) json.get("entities");
			}
			
			NodeRef destNodeRef = null;
			String destination = req.getParameter("dest");
			if (destination != null) {
				destNodeRef = new NodeRef(destination);
			}
			
			Boolean onlyNutsBool = Boolean.valueOf(req.getParameter("onlyNuts"));
			
			NodeRef file = null;
			if ((json != null) && json.has("supplier") && json.getString("supplier") != null && !json.getString("supplier").isEmpty()) {
				file = new NodeRef(json.getString("supplier"));

				JSONArray ret = new JSONArray();
				
					for (final String entity : entities.split(",")) {
						logger.debug("using entity: "+entity);
						
						if(Boolean.TRUE.equals(onlyNutsBool)){
							List<NutListDataItem> nuts = nutDatabaseService.getNuts(file, entity);
							
							if(!nuts.isEmpty()){
								logger.debug("Importing nuts in product");
								ProductData rmData = alfrescoRepository.findOne(destNodeRef);
								rmData.getNutList().clear();
								rmData.getNutList().addAll(nuts);
								alfrescoRepository.save(rmData);
								break;
							}
							
						} else {
							//create new raw material
							logger.debug("importing new RM");
							NodeRef entityNodeRef;
							entityNodeRef = nutDatabaseService.createProduct(file, entity, destNodeRef);
							if (entityNodeRef == null) {
								logger.debug("createProduct returned null");
							}
			
							ret.put(entityNodeRef);
						}
					}
				
	
				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				res.getWriter().write(ret.toString());
			
			}
			
		} catch (JSONException e) {
			throw new WebScriptException(e.getMessage());
		} catch (IOException e) {
			throw new WebScriptException(e.getMessage());
		}
	}

}
