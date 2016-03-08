package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.ReportModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * Computes product completion score
 * 
 * @author steven
 *
 */
public class ScoreCalculatingFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(ScoreCalculatingFormulationHandler.class);

	private static final String MESSAGE_MANDATORY_FIELD_MISSING = "message.formulate.mandatory_property";
	private static final String MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED = "message.formulate.mandatory_property_localized";
	private static final String MESSAGE_NON_VALIDATED_STATE = "message.formulate.nonValidatedState";

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	private DictionaryService dictionaryService;

	private NamespaceService namespaceService;

	private AssociationService associationService;

	private NodeService mlNodeService;

	/**
	 * Product can't be completed unless these fields are present
	 */
	private String mandatoryFields;

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setMandatoryFields(String mandatoryFields) {
		this.mandatoryFields = mandatoryFields;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setMlNodeService(NodeService mlNodeService){
		this.mlNodeService = mlNodeService;
	}

	@Override
	public boolean process(ProductData product) {
		Double childScore = 0d;
		Double childrenSize = 0d;

		Double[] childrenScores = new Double[2];
		childrenScores[0] = childScore;
		childrenScores[1] = childrenSize;

		if (logger.isDebugEnabled()) {
			logger.debug("Calculating score of " + product.getName() + ", state=" + product.getState());
		}

		// visits all refs and adds rclDataItem to them if required

		for (CompoListDataItem compo : product.getCompoList()) {
			visitProduct(compo, childrenScores, product.getCompoListView());
		}
		for (PackagingListDataItem packaging : product.getPackagingList()) {
			visitProduct(packaging, childrenScores, product.getPackagingListView());
		}

		for (ProcessListDataItem process : product.getProcessList()) {
			visitProduct(process, childrenScores, product.getProcessListView());
		}

		// checks if mandatory fields are present
		JSONArray mandatoryFieldsRet = new JSONArray();

		try {
			mandatoryFieldsRet = calculateMandatoryFieldsScore(product.getNodeRef(), product);
		} catch (JSONException e){
			logger.error("unable to compute mandatory fields score", e);
		}

		if(logger.isDebugEnabled()){
			logger.debug("ret: "+mandatoryFieldsRet);
		}
		Integer specificationsScore = calculateSpecificationScore(product); 
		double componentsValidationScore = (childrenScores[1] > 0 ? childrenScores[0] / childrenScores[1] : 1d);


		double mandatoryFieldsScore = 0d;
		int i=0;

		//compute mandatory field score (global)
		try {
			for(int j=0; j<mandatoryFieldsRet.length(); j++){
				JSONObject currentCatalogDescription = (JSONObject) mandatoryFieldsRet.get(j);
				mandatoryFieldsScore+=currentCatalogDescription.getInt("violated");
				i+=currentCatalogDescription.getInt("visited");
			}

			if(i>0){
				mandatoryFieldsScore/=(double)i;
			}

		} catch (JSONException e){
			logger.error("unable to compute mandatory fields score out of json object", e);
		}

		//there was no mandatory fields score: they are all filled
		if(i==0){
			mandatoryFieldsScore=1d;
		}

		double completionPercent=0d;
		completionPercent = ((componentsValidationScore * 100) + (mandatoryFieldsScore * 100) + specificationsScore)/3d;

		// done computing scores, setting intermediate global score var to sum
		// of those

		if (logger.isDebugEnabled()) {
			logger.debug("Children score=" + childrenScores[0] + ", childrenSize=" + childrenScores[1] + ", completion="
					+ (componentsValidationScore * 100) + "%");
			logger.debug("specificationScore=" + specificationsScore + "%");
			logger.debug("Global score=" + (completionPercent)+"%");
		}

		JSONObject scores = new JSONObject();
		JSONObject details = new JSONObject();

		try {
			details.put("mandatoryFields", mandatoryFieldsScore);
			details.put("specifications", specificationsScore);
			details.put("componentsValidation", componentsValidationScore);

			scores.put("global", completionPercent);
			scores.put("details", details);
			
			if(mandatoryFieldsRet != null){
				scores.put("catalogs",mandatoryFieldsRet);
			}				
		} catch(JSONException e){
			logger.error("unable to create scores json object properly", e);
		}

		if(logger.isDebugEnabled()){
			logger.debug("scores="+scores+"\n====================================================");
		}
		product.setProductScores(scores.toString());

		return true;
	}

	/**
	 * Checks state of product bound to node, eventually increasing childScore
	 * and adding new ReqCtrlDataItem to view if state is not valid
	 * 
	 * @param node
	 * @param childScore
	 * @param childrenSize
	 * @param view
	 */
	public void visitProduct(CompositionDataItem dataItem, Double[] childrenArray, AbstractProductDataView view) {
		if (dataItem.getComponent() != null) {
			if (!checkProductValidity(dataItem.getComponent())) {
				view.getReqCtrlList().add(createValidationRclDataItem(dataItem.getComponent()));
			} else {
				childrenArray[0] += 1;
			}
			childrenArray[1] += 1;
		}

	}

	/**
	 * Creates a new ReqCtrlListDataItem for node for validation issues
	 * 
	 * @param node
	 * @return
	 */
	public ReqCtrlListDataItem createValidationRclDataItem(NodeRef node) {
		String message = I18NUtil.getMessage(MESSAGE_NON_VALIDATED_STATE);

		ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, new ArrayList<NodeRef>(),
				RequirementDataType.Validation);

		rclDataItem.getSources().add(node);
		return rclDataItem;
	}

	/**
	 * Returns if node exists and is in valid state
	 * 
	 * @param node
	 * @return
	 */
	public boolean checkProductValidity(NodeRef node) {
		boolean res = false;
		ProductData found = alfrescoRepository.findOne(node);

		if (found != null) {
			if (SystemState.Valid.equals(found.getState())) {
				res = true;
			}
		}

		return res;
	}

	/**
	 * Checks if all mandatory fields of config are present in product data
	 * 
	 * @param nodeRef
	 * @param dat
	 * @return
	 * @throws JSONException 
	 */
	@SuppressWarnings("unchecked")
	public JSONArray calculateMandatoryFieldsScore(NodeRef nodeRef, ProductData dat) throws JSONException {
		JSONArray ret = new JSONArray();
		QName qname; 
		List<NodeRef> assoc; 
		AssociationDefinition assocDesc;
		PropertyDefinition property;
		Map<QName, Serializable> properties;
		JSONArray catalogs;

		try {
			catalogs = new JSONArray(mandatoryFields);
		} catch (Exception e1) {
			logger.error("unable to create catalog json object from mandatory fields");
			return null;
		}

		boolean isPresent=false;

		Map<QName, Serializable> props = nodeService.getProperties(nodeRef);

		ArrayList<String> reportLocales=null;

		if(props != null){
			Serializable prop = props.get(ReportModel.PROP_REPORT_LOCALES);
			if(prop != null && prop instanceof ArrayList){	
				reportLocales = (ArrayList<String>) prop;
			} 
		}

		/*
		 * For each catalog, create sub-catalogs using the intersection of
		 * locales from the catalog and the global locales set in report settings
		 * 
		 * Each localized catalog is used to check multi-language properties
		 */
		for(int i=0; i< catalogs.length(); i++){
			JSONObject currentCatalog = null;
			String label = null;
			String id = null;
			JSONArray fields = new JSONArray();
			JSONArray catalogLocales = new JSONArray();

			Object currentCatalogObject = catalogs.get(i);

			if(currentCatalogObject instanceof JSONObject){
				currentCatalog = (JSONObject) currentCatalogObject;
			} else {
				currentCatalog = new JSONObject();
			}

			if(currentCatalog.has("id") && currentCatalog.get("id") instanceof String){
				id = currentCatalog.getString("id");
			} else {
				id = "id_"+i;
			}
			
			if(currentCatalog.has("label") && currentCatalog.get("label") instanceof String){
				label = currentCatalog.getString("label");
			} else {
				label = "label";
			}

			if(currentCatalog.has("fields") && currentCatalog.get("fields") instanceof JSONArray){
				fields = currentCatalog.getJSONArray("fields");
			} else {
				fields = new JSONArray();
			}

			if(currentCatalog.has("locales") && currentCatalog.get("locales") instanceof JSONArray){
				catalogLocales = currentCatalog.getJSONArray("locales");
			} else {
				catalogLocales = new JSONArray();
			}

			if(logger.isDebugEnabled()){
				logger.debug("CatalogLocales: "+catalogLocales);
			}

			//intersection of report and catalog locales
			List<String> localesIntersection = new ArrayList<String>();

			if(catalogLocales.length() == 0 && reportLocales != null){
				localesIntersection = reportLocales;
			} else if(reportLocales == null){
				Object currentLocaleObject=null;
				for(int j=0; j<catalogLocales.length(); j++){
					currentLocaleObject = catalogLocales.get(j);

					if(currentLocaleObject != null && currentLocaleObject instanceof String){
						localesIntersection.add((String)currentLocaleObject);
					}
				}
			} else {
				//do intersection of both arrays
				for(String currentLocale : reportLocales){
					for(int currentLocaleIndex=0; currentLocaleIndex<catalogLocales.length(); currentLocaleIndex++){
						if(currentLocale.equals(catalogLocales.get(currentLocaleIndex))){
							localesIntersection.add(currentLocale);
						}
					}
				}
			}

			if(logger.isDebugEnabled()){
				logger.debug("intersectionLocales: "+localesIntersection);
			}

			if(currentCatalog != null){
				String currentLocale=null;
				List<JSONObject> localizedCatalogs = new ArrayList<JSONObject>(localesIntersection.size());

				/*
				 * create one sub-catalog per locale
				 */
				boolean localeComesFromCatalog = true;
				JSONArray currentLocales = new JSONArray();
				if(localesIntersection.size() > 0){
					//one catalog for the unlocalized version, which stores common props
					if(catalogLocales.length() > 0){
						localizedCatalogs.add(createLocalizedCatalog(new JSONArray(), label, id));
					}

					//k localized catalog for localized mlTexts
					for(int k=0; k<localesIntersection.size() && localeComesFromCatalog; k++){	
						currentLocales = new JSONArray();
						currentLocale = localesIntersection.get(k);
						

						//if locale comes from report langs, only one array with all locales (=break)
						if(catalogLocales.length() == 0){
							for(String s : localesIntersection){
								currentLocales.put(s);
							}
							localeComesFromCatalog = false;
						} else {
							currentLocales.put(currentLocale);
						}

						localizedCatalogs.add(createLocalizedCatalog(currentLocales, label, id));
						currentLocales = new JSONArray();
					}
				} else {
					//no locales at all
					localizedCatalogs.add(createLocalizedCatalog(currentLocales, label, id));
				}

				String field ="";
				for (int j=0; j<fields.length(); j++) {

					Object currentFieldObject = fields.get(j);
					if(currentFieldObject != null && currentFieldObject instanceof String){
						field = (String) currentFieldObject;
					}

					//if field is localised check for unlocalized prop
					if(field.contains("_")){
						qname = QName.createQName(field.split("_")[0], namespaceService);
					} else {
						qname = QName.createQName(field, namespaceService);
					}

					PropertyDefinition currentProperty;
					currentProperty = dictionaryService.getProperty(qname);

					List<QName> localizedProps = new ArrayList<QName>();
					boolean isMlText=false;
					if(currentProperty != null){
						isMlText = DataTypeDefinition.MLTEXT.equals(currentProperty.getDataType().getName());

						//field is mltext, unlocalized and locales of catalog are not null
						if(isMlText && localesIntersection != null && localesIntersection.size()>0 && !field.contains("_")){
							QName localizedField;
							for(int k=0; k<localesIntersection.size(); k++){
								currentLocale = localesIntersection.get(k);
								if(currentLocale != null && currentLocale != "" && currentLocale.length()==2){
									localizedField = QName.createQName(field+"_"+currentLocale, namespaceService);
									localizedProps.add(localizedField);
								}
							}
						} else if(isMlText && field.contains("_")){
							//field is already localized
							localizedProps.add(QName.createQName(field, namespaceService));
						} else {
							//unlocalised or non mlText field
							localizedProps.add(qname);
						}
					}

					String fieldMessage=null;

					//Prop is a mlText : check it using localized versions of the prop
					if(isMlText){
						MLText mlText = (MLText) mlNodeService.getProperty(nodeRef, qname);						

						for(QName qname2 : localizedProps){
							String[] splits = qname2.toString().split("_");
							String locale = "";
							localeComesFromCatalog=false;

							if(splits.length > 1){
								locale = splits[splits.length-1];
							}

							if(locale != null){
								for(String tmp :localesIntersection){
									if(locale.equals(tmp)){
										localeComesFromCatalog=true;
									}
								}
							}

							Object mlValue=null;
							if(localesIntersection.isEmpty()){
								mlValue = mlText;
							} else {
								if(mlText != null){
									mlValue = mlText.getValue(new Locale(locale));
								}
							}

							if(logger.isDebugEnabled()){
								logger.debug("mlValue: \""+mlValue+"\" for locale \""+locale+"\"");
							}

							if(mlValue != null && !mlValue.equals("")){
								//wether this field should be visited by every catalog or not
								if(!localeComesFromCatalog){
									//everybody should visit it
									for(JSONObject catalog : localizedCatalogs){
										catalog.put("visited", catalog.getInt("visited")+1);
									}
								} else {
									//only one localized catalog should visit it
									for(JSONObject catalog : localizedCatalogs){
										JSONArray currentCatalogLocales = catalog.getJSONArray("locales");

										for(int tmp=0; tmp<currentCatalogLocales.length(); tmp++){
											if(locale.equals(currentCatalogLocales.get(tmp))){
												catalog.put("visited", catalog.getInt("visited")+1);
												break;
											}
										}
									}
								}
								continue;
							}

							PropertyDefinition def;
							String title=null;
							if( (def = dictionaryService.getProperty(qname)) != null && (title = def.getTitle(dictionaryService)) != null){
								fieldMessage = title;
							}

							String message = I18NUtil.getMessage(
									MESSAGE_MANDATORY_FIELD_MISSING_LOCALIZED,(fieldMessage == null ? field : fieldMessage), label, (locale.equals("")?locale:"("+locale+")"));

							if(logger.isDebugEnabled()){
								logger.debug("ML rclDataItem message: "+message);
							}

							if(logger.isDebugEnabled()){
								logger.debug("Adding locale: "+locale+" to rclDataItem message");
							}	

							ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, new ArrayList<NodeRef>(),
									RequirementDataType.Completion);
							rclDataItem.getSources().add(nodeRef);

							//put rclDataItem in proper view
							if(!dat.getCompoListView().getCompoList().isEmpty()){
								dat.getCompoListView().getReqCtrlList().add(rclDataItem);
							} else if(!dat.getProcessListView().getProcessList().isEmpty()){
								dat.getProcessListView().getReqCtrlList().add(rclDataItem);
							} else if(!dat.getPackagingListView().getPackagingList().isEmpty()){
								dat.getPackagingListView().getReqCtrlList().add(rclDataItem);
							}

							//wether this field should be visited by every catalog or not
							if(!localeComesFromCatalog){
								//everybody should visit it
								for(JSONObject catalog : localizedCatalogs){
									JSONArray missingFields = catalog.getJSONArray("missingFields");

									//only common catalog should have this field as void
									if(catalog.has("label")){	
										String currentLabel = catalog.getString("label");
										if(localizedCatalogs.get(0).equals(catalog)){
											missingFields.put(field);
											catalog.put("visited", catalog.getInt("visited")+1);
											catalog.put("violated", catalog.getInt("violated")+1);
										}
									}

								}
							} else {
								//only one localized catalog should visit it
								JSONObject catalog;
								Iterator<JSONObject> it = localizedCatalogs.iterator();

								boolean found = false;
								while(it.hasNext() && !found){
									catalog = it.next();

									JSONArray currentLocalesArray = catalog.getJSONArray("locales");
									String currentCatalogLocale = null;

									if(currentLocalesArray.length()>0){
										currentCatalogLocale = catalog.getJSONArray("locales").getString(0);
									}

									if(locale.equals(currentCatalogLocale)){
										JSONArray missingFields = catalog.getJSONArray("missingFields");
										missingFields.put(field+"_"+locale);
										catalog.put("visited", catalog.getInt("visited")+1);
										catalog.put("violated", catalog.getInt("violated")+1);
										found=true;
									}
								}
							}
						}
					} else {
						//Not a ml text, regular prop/assoc
						assoc = associationService.getTargetAssocs(nodeRef, qname);
						assocDesc  = dictionaryService.getAssociation(qname);
						property = dictionaryService.getProperty(qname);
						properties = nodeService.getProperties(nodeRef);

						if (properties.containsKey(qname)) {
							Serializable val = properties.get(qname);
							if ((val != null) && !val.equals("")) {
								isPresent=true;
							}
						}

						if ((assoc != null) && !assoc.isEmpty()) {
							isPresent=true;
						}

						//fetch translation for prop/assoc name
						if (assocDesc != null) {
							fieldMessage = (assocDesc.getTitle(dictionaryService));
						} else if (property != null) {
							fieldMessage = (property.getTitle(dictionaryService));
						}

						//if prop is absent, raise rclDataItem
						if (!isPresent) {
							String message = I18NUtil.getMessage(
									MESSAGE_MANDATORY_FIELD_MISSING,(fieldMessage == null ? field : fieldMessage), label);
							ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, new ArrayList<NodeRef>(),
									RequirementDataType.Completion);
							rclDataItem.getSources().add(nodeRef);

							// puts rclDataItem in first non empty view (depends on type of product data : packaging kit/unit, process
							// semi finished, finished product..
							if(!dat.getCompoListView().getCompoList().isEmpty()){
								dat.getCompoListView().getReqCtrlList().add(rclDataItem);
							} else if(!dat.getProcessListView().getProcessList().isEmpty()){
								dat.getProcessListView().getReqCtrlList().add(rclDataItem);
							} else if(!dat.getPackagingListView().getPackagingList().isEmpty()){
								dat.getPackagingListView().getReqCtrlList().add(rclDataItem);
							}

							for(JSONObject catalog : localizedCatalogs){
								JSONArray missingFields = catalog.getJSONArray("missingFields");
								if(catalog.has("label")){
									String currentLabel = catalog.getString("label");
									if(!currentLabel.matches("([(][a-z]{2}[)])+")){
										missingFields.put(field);
										catalog.put("violated", catalog.getInt("violated")+1);
										catalog.put("visited", catalog.getInt("visited")+1);
									}
								}
							}
						} else {
							//prop is present
							for(JSONObject catalog : localizedCatalogs){
								catalog.put("visited", catalog.getInt("visited")+1);
							}
						}
					}
					if(logger.isDebugEnabled()){
						logger.debug("============================ End of field "+field+" ============================");
					}
					isPresent=false;
				}

				//we can put these localized catalogs inside ret and go for another catalog
				fillWithCatalogs(localizedCatalogs, ret);

				if(logger.isDebugEnabled()){
					logger.debug("\n\n============================ End of catalog "+label+" ============================\n\n");
				}
			}
		}

		if(logger.isDebugEnabled()){
			logger.debug("\n\n============================ End of catalog parsing ============================\n\n");
		}

		return ret;
	}
	
	/**
	 * Calculates localized catalogs score and put it in ret
	 * @param catalogs
	 * @param ret
	 * @throws JSONException
	 */
	public void fillWithCatalogs(List<JSONObject> catalogs, JSONArray ret) throws JSONException{
		for(JSONObject localizedCatalog : catalogs){
			
			String localizedLabel = localizedCatalog.getString("label");
			String id = localizedCatalog.getString("id");
			JSONArray localizedMissingFieldsArray = localizedCatalog.getJSONArray("missingFields");
			JSONArray localizedCatalogLocales = localizedCatalog.getJSONArray("locales");

			int violatedMandatoryFields = localizedCatalog.getInt("violated");
			int mandatoryFieldsVisited = localizedCatalog.getInt("visited");

			double currentCatalogsScore = mandatoryFieldsVisited > 0
					? (mandatoryFieldsVisited - violatedMandatoryFields) / (double) mandatoryFieldsVisited : 1d;

					if(logger.isDebugEnabled()){
						logger.debug("\nCatalog "+localizedLabel+": visited="+mandatoryFieldsVisited+", violated="+violatedMandatoryFields+" -> score="+currentCatalogsScore+"\n");
					}

					JSONObject catalogDesc = new JSONObject();
					catalogDesc.put("missingFields", localizedMissingFieldsArray);
					catalogDesc.put("locales", localizedCatalogLocales);
					catalogDesc.put("visited", mandatoryFieldsVisited);
					catalogDesc.put("violated", violatedMandatoryFields);
					catalogDesc.put("score", currentCatalogsScore);
					catalogDesc.put("label", localizedLabel);
					catalogDesc.put("id", id);
					ret.put(catalogDesc);
		}
	}

	/**
	 * Creates a sub-catalog with given parameters
	 * @param locales the locale(s) of this catalog. might be null
	 * @param label the name of this subcatalog (= original catalog + locale if just one locale)
	 * @return 
	 * @throws JSONException
	 */
	public JSONObject createLocalizedCatalog(JSONArray locales, String label, String id) throws JSONException{
		if(logger.isDebugEnabled()){
			logger.debug("Creating localized catalog, label: "+label+", id: "+id+", locales: "+locales);
		}
		JSONObject localizedCatalog = new JSONObject();
		localizedCatalog.put("visited", 0);
		localizedCatalog.put("violated", 0);
		localizedCatalog.put("missingFields", new JSONArray());
		localizedCatalog.put("label", label);
		localizedCatalog.put("locales", locales);
		localizedCatalog.put("id", id);

		return localizedCatalog;	
	}

	/**
	 * Computes score related to the specification issues of the product. Each
	 * issue found withdraws 10% to the result score
	 * 
	 * @param product
	 * @return
	 */
	public Integer calculateSpecificationScore(ProductData product) {
		int specificationScore = 100;

		List<ReqCtrlListDataItem> ctrls = new ArrayList<ReqCtrlListDataItem>();
		ctrls.addAll(product.getCompoListView().getReqCtrlList());
		ctrls.addAll(product.getPackagingListView().getReqCtrlList());
		ctrls.addAll(product.getProcessListView().getReqCtrlList());

		for (ReqCtrlListDataItem ctrl : ctrls) {		
			if ((ctrl.getReqDataType() == RequirementDataType.Specification)
					&& (ctrl.getReqType() == RequirementType.Forbidden)) {
				if (logger.isDebugEnabled()) {
					logger.debug(ctrl.getName() + " is forbidden, -10%");
				}
				specificationScore = Math.max(specificationScore - 10, 0);
			}
		}

		return specificationScore;
	}

}