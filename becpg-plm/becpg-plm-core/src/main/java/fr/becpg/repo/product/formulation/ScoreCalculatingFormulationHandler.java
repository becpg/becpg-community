package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
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

		if (logger.isDebugEnabled()) {
			logger.debug("calculating score of " + product.getName() + ", state=" + product.getState());
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

		if (logger.isDebugEnabled()) {
			logger.debug("end of all visits, childScore=" + childrenScores[0] + ", childrenSize=" + childrenScores[1]);
		}

		// checks if mandatory fields are present
		calculateMandatoryFieldsScore(product.getNodeRef(), product);
		Integer specificationsScore = calculateSpecificationScore(product);

		// done computing scores, setting intermediate global score var to sum
		// of those
		double componentsValidationScore = (childrenScores[1] > 0 ? childrenScores[0] / childrenScores[1] : 1d);
		double globalScore = (componentsValidationScore * 100) + (product.getCharacteristicsCompletion() * 100) + specificationsScore;

		if (logger.isDebugEnabled()) {
			logger.debug("Children score=" + childrenScores[0] + ", childrenSize=" + childrenScores[1] + ", completion="
					+ (componentsValidationScore * 100) + "%");
			logger.debug("specificationScore=" + specificationsScore + "%");
			logger.debug("Global score=" + (globalScore / 3d));
		}

		product.setComponentCompletion(componentsValidationScore);
		product.setCompletionPercent(globalScore / 3d);
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
		if (logger.isDebugEnabled()) {
			logger.debug("Visiting dataItem " + dataItem.getName() + ", scores are " + childrenArray[0] + " and " + childrenArray[1]);
		}

		if (dataItem.getComponent() != null) {

			if (!checkProductValidity(dataItem.getComponent())) {
				if (logger.isDebugEnabled()) {
					logger.debug("component is not validated, childScore=" + childrenArray[0]);
				}
				view.getReqCtrlList().add(createValidationRclDataItem(dataItem.getComponent()));
			} else {
				childrenArray[0] += 1;
			}

			childrenArray[1] += 1;
			if (logger.isDebugEnabled()) {
				logger.debug(
						"end of visit of product " + dataItem.getName() + ", childScore=" + childrenArray[0] + ", childrenSize=" + childrenArray[1]);
			}
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
	public boolean calculateMandatoryFieldsScore(NodeRef nodeRef, ProductData dat) {
		int mandatoryFieldsVisited = 0;
		int violatedMandatoryFields = 0;

		for (String field : mandatoryFields.split(",")) {
			if (logger.isDebugEnabled()) {
				logger.debug("Checking if node has field " + field + "...");
			}

			QName qname = QName.createQName(field, namespaceService);
			List<NodeRef> assoc = associationService.getTargetAssocs(nodeRef, qname);
			AssociationDefinition assocDesc = dictionaryService.getAssociation(qname);
			PropertyDefinition property = dictionaryService.getProperty(qname);
			Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

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
				if (logger.isDebugEnabled()) {
					logger.debug("...no it doesn't.");
				}

				violatedMandatoryFields++;
				String fieldMessage = "";

				if (assocDesc != null) {
					fieldMessage = (assocDesc.getTitle(dictionaryService));
				} else if (property != null) {
					fieldMessage = (property.getTitle(dictionaryService));
				}

				String message = I18NUtil.getMessage(MESSAGE_MANDATORY_FIELD_MISSING, (fieldMessage == null ? field : fieldMessage));

				// adds rclDataItem for this mandatory field that is absent
				ReqCtrlListDataItem rclDataItem = new ReqCtrlListDataItem(null, RequirementType.Forbidden, message, null, new ArrayList<NodeRef>(),
						RequirementDataType.Completion);
				rclDataItem.getSources().add(nodeRef);
				dat.getCompoListView().getReqCtrlList().add(rclDataItem);
				dat.getProcessListView().getReqCtrlList().add(rclDataItem);
				dat.getPackagingListView().getReqCtrlList().add(rclDataItem);
			}

			mandatoryFieldsVisited++;
		}

		double mandatoryFieldsScore = mandatoryFieldsVisited > 0
				? (mandatoryFieldsVisited - violatedMandatoryFields) / (double) mandatoryFieldsVisited : 1d;
		dat.setCharacteristicsCompletion(mandatoryFieldsScore);
		if (logger.isDebugEnabled()) {
			logger.debug("Mandatory fields visited=" + mandatoryFieldsVisited + ", violated=" + violatedMandatoryFields + ", mandatoryFieldsScore="
					+ (mandatoryFieldsScore * 100) + "%");
		}
		return true;
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

		if (logger.isDebugEnabled()) {
			logger.debug("\nChecking forbidden ctrl on " + product.getName() + "\n");
		}

		for (ReqCtrlListDataItem ctrl : product.getCompoListView().getReqCtrlList()) {
			if ((ctrl.getName() != null) && (ctrl.getReqDataType() == RequirementDataType.Specification)
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
