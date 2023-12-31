package fr.becpg.repo.web.scripts.product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.nutrient.NutDatabaseService;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * <p>NutDatabaseCompareWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NutDatabaseCompareWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(NutDatabaseCompareWebScript.class);

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Autowired
	private NodeService nodeService;

	private NutDatabaseService nutDatabaseService;

	/**
	 * <p>Setter for the field <code>nutDatabaseService</code>.</p>
	 *
	 * @param nutDatabaseService a {@link fr.becpg.repo.product.formulation.nutrient.NutDatabaseService} object.
	 */
	public void setNutDatabaseService(NutDatabaseService nutDatabaseService) {
		this.nutDatabaseService = nutDatabaseService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		try {
			logger.debug("Compare nutrients WS");
			JSONObject ret = new JSONObject();
			JSONArray nuts = new JSONArray();

			String entities = req.getParameter("entities");

			NodeRef file = null;
			if (req.getParameter("supplier") != null) {
				file = new NodeRef(req.getParameter("supplier"));
			}

			NodeRef base = null;
			if (req.getParameter("base") != null) {
				base = new NodeRef(req.getParameter("base"));
			}

			logger.debug("Entities: " + entities + ", file: " + file + ", base: " + base);

			Map<String, JSONArray> nutValuesMap = new HashMap<>();
			List<String> productNames = new ArrayList<>();

			JSONObject currentNutsJSON = extractNutsJSON(null, null, base);
			productNames.add(currentNutsJSON.getString("name"));

			List<JSONObject> extractedNutsJSONObjects = new ArrayList<>();
			Set<String> nutNames = new HashSet<>();

			extractedNutsJSONObjects.add(currentNutsJSON);

			for (final String entity : entities.split(",")) {
				currentNutsJSON = extractNutsJSON(file, entity, null);
				extractedNutsJSONObjects.add(currentNutsJSON);
				productNames.add(currentNutsJSON.getString("name"));
			}

			// fill nut names
			nutNames.addAll(getNutsNames(extractedNutsJSONObjects));

			for (JSONObject nutsJSON : extractedNutsJSONObjects) {
				putNutJSONinMap(nutValuesMap, nutsJSON, nutNames);
			}

			logger.debug("Map: " + nutValuesMap);

			int i = 0;
			for (String entry : nutValuesMap.keySet()) {

				String parity = "odd";
				JSONObject currentObject = new JSONObject();

				if ((i & 1) == 0) {
					parity = "even";
				}

				currentObject.put("parity", parity);
				currentObject.put("name", entry);
				currentObject.put("values", nutValuesMap.get(entry));
				nuts.put(currentObject);
				++i;
			}

			ret.put("headers", new JSONArray(productNames));
			ret.put("nuts", nuts);
			logger.debug("End of WS, JSON: " + ret);
			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(ret.toString());
		} catch (JSONException e) {
			throw new WebScriptException(e.getMessage());
		} catch (IOException e) {
			throw new WebScriptException(e.getMessage());
		}
	}

	private Set<String> getNutsNames(List<JSONObject> extractedNuts) throws JSONException {
		Set<String> res = new HashSet<>();

		for (JSONObject object : extractedNuts) {
			JSONArray arr = object.getJSONArray("nuts");
			JSONObject nut = null;

			for (int i = 0; i < arr.length(); ++i) {
				nut = arr.getJSONObject(i);

				res.add(nut.getString("name"));
			}
		}
		logger.debug("Nutrient names: " + res);

		return res;
	}

	private JSONObject extractNutsJSON(NodeRef file, String entity, NodeRef existingEntity) throws JSONException {
		JSONObject currentEntity = new JSONObject();
		if (existingEntity == null) {
			// extract nuts from csv
			List<NutListDataItem> nuts = nutDatabaseService.getNuts(file, entity);
			// only send nuts in json
			logger.debug("Fetching entity from csv");

			JSONArray nutObjectsArray = getJSONNutsArray(nuts);

			currentEntity.put("nuts", nutObjectsArray);
			currentEntity.put("name", nutDatabaseService.getProductName(file, entity));

		} else {

			logger.debug("Existing product");
			ProductData product = alfrescoRepository.findOne(existingEntity);

			if (product != null) {
				List<NutListDataItem> nuts = product.getNutList();
				JSONArray nutObjectsArray = getJSONNutsArray(nuts);
				currentEntity.put("nuts", nutObjectsArray);
				currentEntity.put("name", product.getName());
			}
		}
		return currentEntity;
	}

	private JSONArray getJSONNutsArray(List<NutListDataItem> nuts) throws JSONException {
		JSONArray nutObjectsArray = new JSONArray();

		for (NutListDataItem nut : nuts) {
			JSONObject currentNutJSO = new JSONObject();
			currentNutJSO.put("nodeRef", nut.getCharactNodeRef().toString());
			currentNutJSO.put("name", getNutCharactName(nut));
			currentNutJSO.put("value", nut.getValue());
			nutObjectsArray.put(currentNutJSO);
		}

		return nutObjectsArray;
	}

	private String getNutCharactName(NutListDataItem nut) {
		return (String) nodeService.getProperty(nut.getCharactNodeRef(), BeCPGModel.PROP_CHARACT_NAME);
	}

	private void putNutJSONinMap(Map<String, JSONArray> map, JSONObject object, Set<String> nutNames) throws JSONException {
		JSONArray objectNuts = object.getJSONArray("nuts");
		String productName = object.getString("name");

		Set<String> nutNamesCopy = new HashSet<>();
		nutNamesCopy.addAll(nutNames);

		for (int i = 0; i < objectNuts.length(); ++i) {
			JSONObject currentNut = objectNuts.getJSONObject(i);

			if (!map.containsKey(currentNut.getString("name"))) {
				map.put(currentNut.getString("name"), new JSONArray());
			}

			// add product to map
			JSONArray values = map.get(currentNut.getString("name"));
			nutNamesCopy.remove(currentNut.getString("name"));
			JSONObject newNut = new JSONObject();
			newNut.put("product", productName);

			if (currentNut.has("value")) {
				newNut.put("value", currentNut.getDouble("value"));
			}

			values.put(newNut);
		}

		// put lacking nuts in values
		for (String nut : nutNamesCopy) {

			if (!map.containsKey(nut)) {
				map.put(nut, new JSONArray());
			}

			JSONArray values = map.get(nut);

			JSONObject newNut = new JSONObject();
			newNut.put("product", productName);

			values.put(newNut);
		}

	}

}
