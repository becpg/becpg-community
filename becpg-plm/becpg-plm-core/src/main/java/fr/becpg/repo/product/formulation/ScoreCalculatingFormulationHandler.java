package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
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

	private static final String MESSAGE_MANDATORY_FIELD_MISSING = "message.formulate.mandatory.property";
	private static final String MESSAGE_NON_VALIDATED_STATE = "message.formulate.nonValidatedState";

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	private DictionaryService dictionaryService;

	private NamespaceService namespaceService;

	private AssociationService associationService;

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

	@Override
	public boolean process(ProductData product) {
		Double childScore = 0d;
		Double childrenSize = 0d;

		Double[] childrenScores = new Double[2];
		childrenScores[0] = childScore;
		childrenScores[1] = childrenSize;

//		if (logger.isDebugEnabled()) {
//			logger.debug("calculating score of " + product.getName() + ", state=" + product.getState());
//		}

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

//		if (logger.isDebugEnabled()) {
//			logger.debug("end of all visits, childScore=" + childrenScores[0] + ", childrenSize=" + childrenScores[1]);
//		}

		// checks if mandatory fields are present
		JSONObject mandatoryFieldsRet = calculateMandatoryFieldsScore(product.getNodeRef(), product);
		Integer specificationsScore = calculateSpecificationScore(product); 
		double componentsValidationScore = (childrenScores[1] > 0 ? childrenScores[0] / childrenScores[1] : 1d);


		JSONObject mandatoryFieldsScores;
		double mandatoryFieldsScore = 0d;
		try {
			mandatoryFieldsScores = mandatoryFieldsRet.getJSONObject("scores");
			int i=0;

			Iterator iterator = mandatoryFieldsScores.keys();
			while(iterator.hasNext()){
				String key = (String) iterator.next();
				mandatoryFieldsScore+=mandatoryFieldsScores.getDouble(key);
//				logger.debug("reading key "+key+", score="+mandatoryFieldsScores.getDouble(key));
				i++;
			}

			if(i>0){
				mandatoryFieldsScore/=(double)i;
			}
		} catch (JSONException e) {
			logger.error("unable to compute mandatory fields score from json object");
		}

		double completionPercent=0d;
		completionPercent = ((componentsValidationScore * 100) + (mandatoryFieldsScore * 100) + specificationsScore)/3d;

		// done computing scores, setting intermediate global score var to sum
		// of those

//		if (logger.isDebugEnabled()) {
//			logger.debug("Children score=" + childrenScores[0] + ", childrenSize=" + childrenScores[1] + ", completion="
//					+ (componentsValidationScore * 100) + "%");
//			logger.debug("specificationScore=" + specificationsScore + "%");
//			logger.debug("Global score=" + (completionPercent)+"%");
//		}

		JSONObject scores = new JSONObject();
		JSONObject details = new JSONObject();

		try {
			details.put("mandatoryFields", mandatoryFieldsScore);
			details.put("mandatoryFieldsDetails", mandatoryFieldsRet.getJSONObject("scores"));
			details.put("specifications", specificationsScore);
			details.put("componentsValidation", componentsValidationScore);

			scores.put("global", completionPercent);
			scores.put("details", details);
			scores.put("missingFields", mandatoryFieldsRet.get("missingFields"));

		} catch (JSONException e) {
			logger.error("error putting details in scores json");
		}

//		if(logger.isDebugEnabled()){
//			logger.debug("scores="+scores);
//		}
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
//				if (logger.isDebugEnabled()) {
//					logger.debug("component is not validated, childScore=" + childrenArray[0]);
//				}
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
	public JSONObject calculateMandatoryFieldsScore(NodeRef nodeRef, ProductData dat) {

		int mandatoryFieldsVisited = 0;
		int violatedMandatoryFields = 0;
		JSONObject ret = new JSONObject();
		JSONArray missingFieldsArray = new JSONArray();
		QName qname; 
		List<NodeRef> assoc; 
		AssociationDefinition assocDesc;
		PropertyDefinition property;
		Map<QName, Serializable> properties;
		JSONArray catalogs;

		try {
//			if(logger.isDebugEnabled()){
//				logger.debug("mandatoryFields (should be JSON) : "+mandatoryFields);
//			}
			catalogs = new JSONArray(mandatoryFields);
		} catch (JSONException e1) {
			logger.error("unable to create catalog json object from mandatory fields");
			return null;
		}

		//list of missing fields per catalog
		JSONObject catalogsMissingFields = new JSONObject();
		JSONObject catalogsScores = new JSONObject();

		for(int i=0; i< catalogs.length(); i++){

			mandatoryFieldsVisited = 0;
			violatedMandatoryFields = 0;

			JSONObject currentCatalog=null;
			String id="id";
			String label="label";
			//JSONArray locales=null;
			JSONArray fields=new JSONArray();
			missingFieldsArray = new JSONArray();

			try {
				currentCatalog = catalogs.getJSONObject(i);
				id = currentCatalog.getString("id");
				label = currentCatalog.getString("label");
				//locales = currentCatalog.getJSONArray("locales");
				fields = currentCatalog.getJSONArray("fields");
			} catch (JSONException e){
				logger.error("unable to read a property from json : "+e.getLocalizedMessage());
			}

//			if(logger.isDebugEnabled()){
//				logger.debug("read catalog, id="+id+", label="+label+", fields="+fields);
//			}

			if(currentCatalog != null){
				String field ="";
				for (int j=0; j<fields.length(); j++) {
					try {
						field = fields.getString(j);
					} catch (JSONException e) {
						logger.error("error while parsing field of index "+j);
					}

//					if (logger.isDebugEnabled()) {
//						logger.debug("Checking if node has field " + field + "...");
//					}

					qname = QName.createQName(field, namespaceService);
					assoc = associationService.getTargetAssocs(nodeRef, qname);
					assocDesc  = dictionaryService.getAssociation(qname);
					property = dictionaryService.getProperty(qname);
					properties = nodeService.getProperties(nodeRef);

					// if we don't have any association or prop with the name, raise an
					// rclDataItem
					boolean hasAssoc = false;
					boolean hasProp = false;

					if (properties.containsKey(qname)) {
						Serializable val = properties.get(qname);
						if ((val != null) && !val.equals("")) {
							hasProp = true;
						}
					}

					if ((assoc != null) && !assoc.isEmpty()) {
						hasAssoc = true;
					}

					if (!hasAssoc && !hasProp) {
//						if (logger.isDebugEnabled()) {
//							logger.debug("...no it doesn't.");
//						}

						violatedMandatoryFields++;
						String fieldMessage = "";

						if (assocDesc != null) {
							fieldMessage = (assocDesc.getTitle(dictionaryService));
						} else if (property != null) {
							fieldMessage = (property.getTitle(dictionaryService));
						}

						String message = I18NUtil.getMessage(MESSAGE_MANDATORY_FIELD_MISSING, (fieldMessage == null ? field : fieldMessage), label);

						// adds rclDataItem for this mandatory field that is absent
						ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, new ArrayList<NodeRef>(),
								RequirementDataType.Completion);
						rclDataItem.getSources().add(nodeRef);
						
						
						//TODO modify this if we're considering these properties only apply to FP/SFP
						if(!dat.getCompoListView().getCompoList().isEmpty()){
							dat.getCompoListView().getReqCtrlList().add(rclDataItem);
						}
						
						if(!dat.getProcessListView().getProcessList().isEmpty()){
							dat.getProcessListView().getReqCtrlList().add(rclDataItem);
						}
						
						if(!dat.getPackagingListView().getPackagingList().isEmpty()){
							dat.getPackagingListView().getReqCtrlList().add(rclDataItem);
						}
						missingFieldsArray.put(field);
//						if(logger.isDebugEnabled()){
//							logger.debug("added field "+field+" to array, array="+missingFieldsArray);
//						}
					}

					mandatoryFieldsVisited++;
				}

				double currentCatalogsScore = mandatoryFieldsVisited > 0
						? (mandatoryFieldsVisited - violatedMandatoryFields) / (double) mandatoryFieldsVisited : 1d;

						try {
							catalogsMissingFields.put(id, missingFieldsArray);
							catalogsScores.put(id, currentCatalogsScore);
						} catch (JSONException e) {
							logger.error("unable to add current catalog missing fields to collection : "+e.getLocalizedMessage());
						}
			}
		}

		try {
			ret.put("scores", catalogsScores);
			ret.put("missingFields", catalogsMissingFields);
		} catch (JSONException e) {
			logger.error("unable to serialize scores or missing fields array");
		}

//		if (logger.isDebugEnabled()) {
//			logger.debug("Mandatory fields visited=" + mandatoryFieldsVisited + ", violated=" + violatedMandatoryFields + ", catalogsScores="
//					+ catalogsScores + "%");
//			logger.debug("Ret="+ret.toString());
//		}
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

//		if (logger.isDebugEnabled()) {
//			logger.debug("\nChecking forbidden ctrl on " + product.getName() + "\n");
//		}

		for (ReqCtrlListDataItem ctrl : product.getCompoListView().getReqCtrlList()) {
			if ((ctrl.getName() != null) && (ctrl.getReqDataType() == RequirementDataType.Specification)
					&& (ctrl.getReqType() == RequirementType.Forbidden)) {
//				if (logger.isDebugEnabled()) {
//					logger.debug(ctrl.getName() + " is forbidden, -10%");
//				}
				specificationScore = Math.max(specificationScore - 10, 0);

			}
		}

		return specificationScore;
	}

}
