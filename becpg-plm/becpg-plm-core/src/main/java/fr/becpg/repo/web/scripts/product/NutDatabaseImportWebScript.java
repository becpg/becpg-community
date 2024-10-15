package fr.becpg.repo.web.scripts.product;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.nutrient.NutDatabaseService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.security.SecurityService;

/**
 * <p>NutDatabaseImportWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class NutDatabaseImportWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(NutDatabaseImportWebScript.class);
	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NutDatabaseService nutDatabaseService;
	private SecurityService securityService;
	private NodeService nodeService;

	// Define static final variables for parameter names
	private static final String SUPPLIER_PARAM = "supplier";
	private static final String DEST_PARAM = "dest";
	private static final String ONLY_NUTS_PARAM = "onlyNuts";
	private static final String ENTITIES_PARAM = "entities";
	private static final String REFERENCE_LOCALES_PARAM = "referenceNutrientLocales";
	private static final String REFERENCE_IMPORT_PARAM = "addAsReferenceNutrient";

	/**
	 * <p>Setter for the field <code>nutDatabaseService</code>.</p>
	 *
	 * @param nutDatabaseService a {@link fr.becpg.repo.product.formulation.nutrient.NutDatabaseService} object.
	 */
	public void setNutDatabaseService(NutDatabaseService nutDatabaseService) {
		this.nutDatabaseService = nutDatabaseService;
	}
	
	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object.
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}
	
	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) {
		try {

			JSONObject json = (JSONObject) req.parseContent();
			String entities = "";
			String referenceLocales = "";
			String isReferenceImport = "";

			// Safely extract JSON values using constants
			if (json != null) {
				entities = json.optString(ENTITIES_PARAM, "");
				referenceLocales = json.optString(REFERENCE_LOCALES_PARAM, "");
				isReferenceImport = json.optString(REFERENCE_IMPORT_PARAM, "");
			}

			final NodeRef destNodeRef;
			String destination = req.getParameter(DEST_PARAM);
			if (destination != null) {
				destNodeRef = new NodeRef(destination);
				
				if (securityService.computeAccessMode(destNodeRef, nodeService.getType(destNodeRef), PLMModel.TYPE_NUTLIST) != SecurityService.WRITE_ACCESS) {
					throw new IllegalStateException("You do not have permission for this action.");
				}

				Boolean onlyNutsBool = Boolean.valueOf(req.getParameter(ONLY_NUTS_PARAM));

				NodeRef file = null;
				// Validate supplier existence and handle its presence
				if ((json != null) && json.has(SUPPLIER_PARAM) && !json.optString(SUPPLIER_PARAM).isEmpty()) {
					file = new NodeRef(json.getString(SUPPLIER_PARAM));

					JSONArray ret = new JSONArray();

					for (final String entity : entities.split(",")) {
						
						final String entityName = entity+" - "+nutDatabaseService.getProductName(file, entity);
						logger.debug("using entity: " + entityName);

						if (Boolean.TRUE.equals(onlyNutsBool)) {
							List<NutListDataItem> nuts = nutDatabaseService.getNuts(file, entity);

							if (!nuts.isEmpty()) {
								logger.debug("Importing nuts in product");
								
								final String finalReferenceLocales = referenceLocales.isBlank() ? MLTextHelper.localeKey(I18NUtil.getContentLocale())
										: referenceLocales;

								final String finalIsReferenceImport = isReferenceImport;

								L2CacheSupport.doInCacheContext(() -> {

									ProductData rmData = alfrescoRepository.findOne(destNodeRef);

									// Handle isReferenceImport condition
									if (Boolean.TRUE.toString().equalsIgnoreCase(finalIsReferenceImport)) {

										MLText referenceProducts = rmData.getReferenceProducts();
										if (referenceProducts == null) {
											referenceProducts = new MLText();
										}
										for (String key : finalReferenceLocales.split(",")) {
											referenceProducts.put(MLTextHelper.parseLocale(key), entityName);
										}

										rmData.setReferenceProducts(referenceProducts);
										for (NutListDataItem rmNutLitItem : rmData.getNutList()) {
											for (NutListDataItem importedNutListItem : nuts) {
												if (rmNutLitItem.getNut().equals(importedNutListItem.getNut())
														&& (importedNutListItem.getValue() != null)) {

													MLText referenceValue = rmNutLitItem.getReferenceValue();
													if (referenceValue == null) {
														referenceValue = new MLText();
													}

													for (String key : finalReferenceLocales.split(",")) {
														referenceValue.put(MLTextHelper.parseLocale(key), importedNutListItem.getValue().toString());
													}
													
													rmNutLitItem.setReferenceValue(referenceValue);
												}
											}
										}
									} else {
										// Clear and replace existing nut list
										rmData.getNutList().clear();
										rmData.getNutList().addAll(nuts);
									}

									// Save the updated ProductData
									alfrescoRepository.save(rmData);

								}, false, true);
							}
						} else {
							// Create new raw material
							logger.debug("importing new RM");
							NodeRef entityNodeRef = nutDatabaseService.createProduct(file, entity, destNodeRef);

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
			}

		} catch (JSONException | IOException e) {
			throw new WebScriptException(e.getMessage());
		}
	}
}
