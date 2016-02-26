package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
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

import fr.becpg.model.BeCPGModel;
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
		JSONObject mandatoryFieldsRet = calculateMandatoryFieldsScore(product.getNodeRef(), product);
		if(logger.isDebugEnabled()){
			logger.debug("ret: "+mandatoryFieldsRet);
		}
		Integer specificationsScore = calculateSpecificationScore(product); 
		double componentsValidationScore = (childrenScores[1] > 0 ? childrenScores[0] / childrenScores[1] : 1d);


		JSONObject mandatoryFieldsScores;
		double mandatoryFieldsScore = 0d;
		int i=0;
		try {
			mandatoryFieldsScores = mandatoryFieldsRet.getJSONObject("scores");


			Iterator iterator = mandatoryFieldsScores.keys();
			while(iterator.hasNext()){
				String key = (String) iterator.next();
				mandatoryFieldsScore+=mandatoryFieldsScores.getDouble(key);
				i++;
			}

			if(i>0){
				mandatoryFieldsScore/=(double)i;
			}
		} catch (Exception e) {
			logger.error("unable to compute mandatory fields score from json object");
			mandatoryFieldsScore=0d;
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
			if(mandatoryFieldsRet != null){
				details.put("mandatoryFieldsDetails", mandatoryFieldsRet.getJSONObject("scores"));
			}
			details.put("specifications", specificationsScore);
			details.put("componentsValidation", componentsValidationScore);

			scores.put("global", completionPercent);
			scores.put("details", details);
			if(mandatoryFieldsRet != null){
				scores.put("missingFields", mandatoryFieldsRet.get("missingFields"));
			}
		} catch (JSONException e) {
			logger.error("error putting details in scores json");
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
	 */
	@SuppressWarnings("unchecked")
	public JSONObject calculateMandatoryFieldsScore(NodeRef nodeRef, ProductData dat) {
		JSONObject ret = new JSONObject();
		JSONArray missingFieldsArray = new JSONArray();
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

		//list of missing fields per catalog
		JSONObject catalogsMissingFields = new JSONObject();
		JSONObject catalogsScores = new JSONObject();
		boolean isPresent=false;

		Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
		ArrayList<String> reportLocales=null;

		if(props != null){
			Serializable prop = props.get(ReportModel.PROP_REPORT_LOCALES);
			if(prop != null){
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
			JSONObject currentCatalog=null;
			String id="id";
			String label="label";
			JSONArray fields=new JSONArray();
			missingFieldsArray = new JSONArray();
			JSONArray catalogLocales = new JSONArray();

			try {
				currentCatalog = catalogs.getJSONObject(i);
				id = currentCatalog.getString("id");
				label = currentCatalog.getString("label");
				fields = currentCatalog.getJSONArray("fields");

			} catch (JSONException e){
				logger.error("unable to read a property from json : "+e.getLocalizedMessage());
			}

			try {
				catalogLocales = currentCatalog.getJSONArray("locales");

				if(logger.isDebugEnabled()){
					logger.debug("CatalogLocales: "+catalogLocales);
				}
			} catch(JSONException e){
				logger.debug("Catalog "+label+" has no locales set");
				catalogLocales = new JSONArray();
			}

			//intersection of report and catalog locales
			List<String> localesIntersection=null;

			if(catalogLocales.length() == 0){
				if(reportLocales != null){
					localesIntersection = reportLocales;
				} else {
					localesIntersection = new ArrayList<String>();
				}

			} else if(reportLocales == null){
				localesIntersection = new ArrayList<String>();

				for(int j=0; j<catalogLocales.length(); j++){
					try {
						localesIntersection.add(catalogLocales.getString(j));
					} catch (JSONException e) {
						continue;
					}
				}
			} else {
				//do intersection of both arrays
				localesIntersection = new ArrayList<String>();
				for(String currentLocale : reportLocales){

					for(int cat=0; cat<catalogLocales.length(); cat++){
						try {
							if(catalogLocales.get(cat).equals(currentLocale)){
								localesIntersection.add(currentLocale);
							}
						} catch(Exception e){
							continue;
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
				if(localesIntersection.size() > 0){
					for(int k=0; k<localesIntersection.size(); k++){

						currentLocale = localesIntersection.get(k);

						//if this locale comes from catalog, create one localized version per locale
						if(currentLocale != null && catalogLocales.length() > 0 ){
							//						for(int cat = 0; cat<catalogLocales.length(); cat++){

							JSONObject currentLocalizedCatalog = new JSONObject();
							JSONArray currentLocalizedCatalogMissingFields = new JSONArray();
							JSONArray currentLocales = new JSONArray();
							for(String s : localesIntersection){
								currentLocales.put(s);
							}
							String localizedLabel = label+" ("+currentLocale+")";

							try {
								currentLocalizedCatalog.put("missingFields", currentLocalizedCatalogMissingFields);
								currentLocalizedCatalog.put("visited", 0);
								currentLocalizedCatalog.put("violated", 0);
								currentLocalizedCatalog.put("locales", currentLocales);
								currentLocalizedCatalog.put("label", localizedLabel);
								localizedCatalogs.add(currentLocalizedCatalog);
							} catch (Exception e){
								logger.error("unable to init localized catalog");
							}							
							//						}
						} else {
							//this locale comes from report langs, put them all in sub catalog

							JSONObject currentLocalizedCatalog = new JSONObject();
							JSONArray currentLocalizedCatalogMissingFields = new JSONArray();
							JSONArray currentLocales = new JSONArray();
							for(String s : localesIntersection){
								currentLocales.put(s);
							}

							try {
								currentLocalizedCatalog.put("missingFields", currentLocalizedCatalogMissingFields);
								currentLocalizedCatalog.put("visited", 0);
								currentLocalizedCatalog.put("violated", 0);
								currentLocalizedCatalog.put("locales", currentLocales);
								currentLocalizedCatalog.put("label", label);
								localizedCatalogs.add(currentLocalizedCatalog);
								break;
							} catch (Exception e){
								logger.error("unable to init localized catalog");
							}	
						}
					}
				} else {
					//no locales at all
					JSONObject currentLocalizedCatalog = new JSONObject();
					JSONArray currentLocalizedCatalogMissingFields = new JSONArray();
					JSONArray currentLocales = new JSONArray();

					try {
						currentLocalizedCatalog.put("missingFields", currentLocalizedCatalogMissingFields);
						currentLocalizedCatalog.put("visited", 0);
						currentLocalizedCatalog.put("violated", 0);
						currentLocalizedCatalog.put("locales", currentLocales);
						currentLocalizedCatalog.put("label", label);
						if(logger.isDebugEnabled()){
							logger.debug("currentCatalog: "+currentLocalizedCatalog+"\n=============");
						}
						localizedCatalogs.add(currentLocalizedCatalog);
						break;
					} catch (Exception e){
						logger.error("unable to init localized catalog");
					}	
				}

				String field ="";
				for (int j=0; j<fields.length(); j++) {

					try {
						field = fields.getString(j);
					} catch (Exception e) {
						logger.error("error while parsing field of index "+j);
						continue;
					}

					try {
						//if field is localised check for unlocalized prop
						if(field.contains("_")){
							qname = QName.createQName(field.split("_")[0], namespaceService);
						} else {
							qname = QName.createQName(field, namespaceService);
						}
					} catch(Exception e){
						logger.error("unable to create qname out if field: "+field+", continuing..");
						continue;
					}

					PropertyDefinition currentProperty;
					currentProperty = dictionaryService.getProperty(qname);

					List<QName> localizedProps = new ArrayList<QName>();
					boolean isMlText=false;
					if(currentProperty != null){
						isMlText = currentProperty.getDataType().getName().equals(DataTypeDefinition.MLTEXT);

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
							boolean localeComesFromCatalog=false;

							if(splits.length > 1){
								locale = splits[splits.length-1];
							}

							if(locale != null){
								for(String tmp :localesIntersection){
									if(tmp.equals(locale)){
										localeComesFromCatalog=true;
									}
								}
							}

							Object mlValue=null;

							if(localesIntersection.isEmpty()){
								mlValue = mlText;
							} else {
								if(mlText != null){
									mlValue = mlText.getValue(Locale.forLanguageTag(locale));
								}
							}

							if(logger.isDebugEnabled()){
								logger.debug("mlValue: \""+mlValue+"\" for locale \""+locale+"\"");
							}

							if(mlValue != null && !mlValue.equals("")){
								//wether this field should be visited by every catalog or not
								if(!localeComesFromCatalog){
									//everybody should visit it
									try {
										for(JSONObject catalog : localizedCatalogs){
											catalog.put("visited", catalog.getInt("visited")+1);
										}
									} catch (JSONException e) {
										logger.error("unable to increment visited field of json object for common present field");
									}
								} else {
									//only one localized catalog should visit it
									try {
										for(JSONObject catalog : localizedCatalogs){
											JSONArray currentCatalogLocales = catalog.getJSONArray("locales");

											for(int tmp=0; tmp<currentCatalogLocales.length(); tmp++){

												if(((String)currentCatalogLocales.get(tmp)).equals(locale)){
													catalog.put("visited", catalog.getInt("visited")+1);
													break;
												}
											}
										}
									} catch (Exception e) {
										logger.error("unable to increment visited field of json object for specific present field");
									} 
								}
								continue;
							}

							try {
								fieldMessage = dictionaryService.getProperty(qname).getTitle(dictionaryService);
							} catch(Exception e){
								logger.error("unable to find title of property using the dictionary); qname="+qname);
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

							if(!dat.getCompoListView().getCompoList().isEmpty()){
								dat.getCompoListView().getReqCtrlList().add(rclDataItem);
							} else if(!dat.getProcessListView().getProcessList().isEmpty()){
								dat.getProcessListView().getReqCtrlList().add(rclDataItem);
							} else if(!dat.getPackagingListView().getPackagingList().isEmpty()){
								dat.getPackagingListView().getReqCtrlList().add(rclDataItem);
							}

							try {
								//wether this field should be visited by every catalog or not
								logger.debug("Current localized field: "+field);
								if(!localeComesFromCatalog){
									//everybody should visit it
									for(JSONObject catalog : localizedCatalogs){
										JSONArray missingFields = catalog.getJSONArray("missingFields");
										missingFields.put(field);
										catalog.put("visited", catalog.getInt("visited")+1);
										catalog.put("violated", catalog.getInt("violated")+1);

									}
								} else {
									//only one localized catalog should visit it
									for(JSONObject catalog : localizedCatalogs){
										String currentCatalogLocale = catalog.getJSONArray("locales").getString(0);
										if(currentCatalogLocale.equals(locale)){
											JSONArray missingFields = catalog.getJSONArray("missingFields");
											missingFields.put(field+"_"+locale);
											catalog.put("visited", catalog.getInt("visited")+1);
											catalog.put("violated", catalog.getInt("violated")+1);
											break;
										}
									}
								}
							} catch(JSONException e){ 
								logger.error("unable to put missing field "+field+" in missing fields array or incrementing the score");
								e.printStackTrace();
							}
						}
					} else {
						//Not a ml text, regular prop/assoc
						logger.debug("Regular prop/assoc");

						assoc = associationService.getTargetAssocs(nodeRef, qname);
						assocDesc  = dictionaryService.getAssociation(qname);

						property = dictionaryService.getProperty(qname);
						properties = nodeService.getProperties(nodeRef);

						if (properties.containsKey(qname)) {
							Serializable val = properties.get(qname);
							if ((val != null) && !val.equals("")) {
								isPresent=true;
								logger.debug("it's a prop");
							}
						}

						if ((assoc != null) && !assoc.isEmpty()) {
							isPresent=true;
							logger.debug("it's an assoc");
						}

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

							try {
								for(JSONObject catalog : localizedCatalogs){
									JSONArray missingFields = catalog.getJSONArray("missingFields");
									missingFields.put(field);
									catalog.put("violated", catalog.getInt("violated")+1);
									catalog.put("visited", catalog.getInt("visited")+1);
								}
							} catch(JSONException e) { 
								logger.error("unable to put missing field "+field+" in missing fields array or incrementing the score");
							}
							//prop is present
						} else {
							try {
								for(JSONObject catalog : localizedCatalogs){
									catalog.put("visited", catalog.getInt("visited")+1);
								}
							} catch(JSONException e) { 
								logger.error("unable to put missing field "+field+" in missing fields array or incrementing the score");
							}
						}
					}
					if(logger.isDebugEnabled()){
						logger.debug("============================ End of field "+field+" ============================");
					}
					isPresent=false;
				}

				try {
					for(JSONObject localizedCatalog : localizedCatalogs){
						String localizedLabel = localizedCatalog.getString("label");
						JSONArray localizedMissingFieldsArray = localizedCatalog.getJSONArray("missingFields");

						int violatedMandatoryFields = localizedCatalog.getInt("violated");
						int mandatoryFieldsVisited = localizedCatalog.getInt("visited");


						double currentCatalogsScore = mandatoryFieldsVisited > 0
								? (mandatoryFieldsVisited - violatedMandatoryFields) / (double) mandatoryFieldsVisited : 1d;

								if(logger.isDebugEnabled()){
									logger.debug("\nCatalog "+localizedLabel+": visited="+mandatoryFieldsVisited+", violated="+violatedMandatoryFields+" -> score="+currentCatalogsScore+"\n");
								}

								catalogsMissingFields.put(localizedLabel, localizedMissingFieldsArray);
								catalogsScores.put(localizedLabel, currentCatalogsScore);
					}


				} catch (JSONException e) {
					logger.error("unable to add current catalog missing fields to collection : "+e.getLocalizedMessage());
				}


				if(logger.isDebugEnabled()){
					logger.debug("\n\n============================ End of catalog "+label+" ============================\n\n");
				}

			}
		}

		try {
			if(logger.isDebugEnabled()){
				logger.debug("\n\n============================ End of catalog parsing ============================\n\n");
			}

			ret.put("scores", catalogsScores);
			ret.put("missingFields", catalogsMissingFields);
		} catch (JSONException e) {
			logger.error("unable to serialize scores or missing fields array");
		}
		return ret;
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
