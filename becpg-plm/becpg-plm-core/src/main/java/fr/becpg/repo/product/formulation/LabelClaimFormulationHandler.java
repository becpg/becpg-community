package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.spel.SpelHelper;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 *
 * @author matthieu
 *
 */
public class LabelClaimFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(LabelClaimFormulationHandler.class);

	public static final String MESSAGE_NOT_CLAIM = "message.formulate.labelClaim.notClaimed";

	public static final String MESSAGE_MISSING_CLAIM = "message.formulate.labelClaim.missing";

	public static final String MESSAGE_LABELCLAIM_ERROR = "message.formulate.labelClaim.error";

	private NodeService nodeService;

	private FormulaService formulaService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setFormulaService(FormulaService formulaService) {
		this.formulaService = formulaService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	@Override
	public boolean process(ProductData productData) throws FormulateException {

		if (productData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {
			return true;
		}

		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext context = formulaService.createEvaluationContext(productData);

		if ((productData.getLabelClaimList() != null) && !productData.getLabelClaimList().isEmpty()) {
			if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

				productData.getLabelClaimList().forEach(l -> {
					
					l.setType((String) nodeService.getProperty(l.getLabelClaim(), PLMModel.PROP_LABEL_CLAIM_TYPE));
					
					if ((l.getIsManual() == null) || !l.getIsManual()) {
						l.setLabelClaimValue(null);
					}
				});

				Set<NodeRef> visitedProducts = new HashSet<>();

				for (CompoListDataItem compoItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

					NodeRef part = compoItem.getProduct();
					if (!visitedProducts.contains(part) && (compoItem.getQtySubFormula() != null) && (compoItem.getQtySubFormula() > 0)) {
						ProductData partProduct = alfrescoRepository.findOne(part);
						if (partProduct.getLabelClaimList() != null) {
							for (LabelClaimListDataItem labelClaim : partProduct.getLabelClaimList()) {
								visitPart(productData, partProduct, labelClaim);
								
							}
						}
						visitedProducts.add(part);
					}
				}
			}

			computeClaimList(productData, parser, context);

		}

		// check even if product has no labelclaim, which can be forbidden by
		// specifications
		checkRequirementsOfFormulatedProduct(productData);

		return true;
	}

	private void visitPart(ProductData productData, ProductData partProduct, LabelClaimListDataItem subLabelClaimItem) {
		for (LabelClaimListDataItem labelClaimItem : productData.getLabelClaimList()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Visiting labelClaim " + extractName(labelClaimItem.getLabelClaim()) + " isManual: " + labelClaimItem.getIsManual()
						+ " equals to subLabelClaimItem: " + subLabelClaimItem.equals(labelClaimItem));
			}
			if (((labelClaimItem.getIsManual() == null) || !labelClaimItem.getIsManual())
					&& ((labelClaimItem.getLabelClaim() != null) && labelClaimItem.getLabelClaim().equals(subLabelClaimItem.getLabelClaim()))) {

				if (subLabelClaimItem.getLabelClaimValue() != null) {
					switch (subLabelClaimItem.getLabelClaimValue()) {
					case LabelClaimListDataItem.VALUE_TRUE:
						if (labelClaimItem.getLabelClaimValue() == null) {
							labelClaimItem.setIsClaimed(true);
						}
						break;
					case LabelClaimListDataItem.VALUE_NA:
						if (labelClaimItem.getLabelClaimValue() == null) {
							labelClaimItem.setLabelClaimValue(LabelClaimListDataItem.VALUE_NA);
						}
						break;
					case LabelClaimListDataItem.VALUE_FALSE:
						if (labelClaimItem.getIsClaimed() || (labelClaimItem.getLabelClaimValue() == null)) {
							labelClaimItem.setIsClaimed(false);
						}
						break;
					case LabelClaimListDataItem.VALUE_EMPTY:
					default:
						if (logger.isDebugEnabled()) {
							logger.debug("case empty/default for " + extractName(subLabelClaimItem.getLabelClaim()) + " (value is: \""
									+ subLabelClaimItem.getLabelClaimValue() + "\")");
						}
						addMissingLabelClaimReq(productData, partProduct, labelClaimItem);
						labelClaimItem.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);
						break;
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug(extractName(subLabelClaimItem.getLabelClaim()) + " has null label claim value");
					}
					addMissingLabelClaimReq(productData, partProduct, labelClaimItem);
					labelClaimItem.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);
				}
			}
		}
	}

	private void addMissingLabelClaimReq(ProductData productData, ProductData partProduct, LabelClaimListDataItem labelClaimItem) {
		String message = I18NUtil.getMessage(MESSAGE_MISSING_CLAIM, extractName(labelClaimItem.getLabelClaim()));
		productData.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Info, message,
				labelClaimItem.getLabelClaim(), new ArrayList<NodeRef>(Arrays.asList(partProduct.getNodeRef())), RequirementDataType.Labelclaim));

	}

	private String extractName(NodeRef labelClaim) {
		return (String) nodeService.getProperty(labelClaim, BeCPGModel.PROP_CHARACT_NAME);
	}

	private void computeClaimList(ProductData productData, ExpressionParser parser, StandardEvaluationContext context) {
		if (productData.getLabelClaimList() != null) {
			for (LabelClaimListDataItem labelClaimListDataItem : productData.getLabelClaimList()) {
				labelClaimListDataItem.setIsFormulated(false);
				labelClaimListDataItem.setErrorLog(null);
				if (((labelClaimListDataItem.getIsManual() == null) || !labelClaimListDataItem.getIsManual())
						&& (labelClaimListDataItem.getLabelClaim() != null)) {

					String formula = (String) nodeService.getProperty(labelClaimListDataItem.getLabelClaim(), PLMModel.PROP_LABEL_CLAIM_FORMULA);
					if ((formula != null) && (formula.length() > 0)) {
						try {
							labelClaimListDataItem.setIsFormulated(true);
							Expression exp = parser.parseExpression(SpelHelper.formatFormula(formula));
							Object ret = exp.getValue(context);
							if (ret instanceof Boolean) {
								labelClaimListDataItem.setIsClaimed((Boolean) ret);
							} else {
								labelClaimListDataItem.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);
								labelClaimListDataItem
										.setErrorLog(I18NUtil.getMessage("message.formulate.formula.incorrect.type.boolean", Locale.getDefault()));
							}

						} catch (Exception e) {
							labelClaimListDataItem.setErrorLog(e.getLocalizedMessage());
							if (logger.isDebugEnabled()) {
								logger.debug("Error in formula :" + SpelHelper.formatFormula(formula), e);
							}
						}
					}
				}

				if (labelClaimListDataItem.getErrorLog() != null) {

					String message = I18NUtil.getMessage(MESSAGE_LABELCLAIM_ERROR, Locale.getDefault(),
							nodeService.getProperty(labelClaimListDataItem.getLabelClaim(), BeCPGModel.PROP_CHARACT_NAME),
							labelClaimListDataItem.getErrorLog());
					productData.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message,
							labelClaimListDataItem.getLabelClaim(), new ArrayList<NodeRef>(), RequirementDataType.Labelclaim));
				}

			}
		}

	}

	private void checkRequirementsOfFormulatedProduct(ProductData formulatedProduct) {
		if (getDataListVisited(formulatedProduct) != null) {
			Map<LabelClaimListDataItem, Boolean> specLabelClaimsVisitedMap = new HashMap<>();
			extractRequirements(formulatedProduct).forEach(extracedSpecDataItem -> {
				specLabelClaimsVisitedMap.put(extracedSpecDataItem, false);
			});

			specLabelClaimsVisitedMap.keySet().forEach(specDataItem -> {
				getDataListVisited(formulatedProduct).forEach(listDataItem -> {
					if (listDataItem.getLabelClaim().equals(specDataItem.getLabelClaim())) {
						if (logger.isDebugEnabled()) {
							logger.debug(extractName(specDataItem.getLabelClaim()) + " has been visited");
						}
						specLabelClaimsVisitedMap.put(specDataItem, true);
						if (Boolean.TRUE.equals(specDataItem.getIsClaimed() && !Boolean.TRUE.equals(listDataItem.getIsClaimed()))) {
							addSpecificationUnclaimedLabelClaim(formulatedProduct, listDataItem);
						}
					}
				});
			});

			// check that all the labelClaim in specs have been visited in
			// product
			specLabelClaimsVisitedMap.keySet().forEach(specDataItem -> {
				if (Boolean.FALSE.equals(specLabelClaimsVisitedMap.get(specDataItem))) {
					if (logger.isDebugEnabled()) {
						logger.debug(extractName(specDataItem.getLabelClaim()) + " was not found, raising rclDataItem for spec");
					}
					addSpecificationUnclaimedLabelClaim(formulatedProduct, specDataItem);
				}
			});
		}
	}

	private void addSpecificationUnclaimedLabelClaim(ProductData formulatedProduct, LabelClaimListDataItem labelClaim) {
		String message = I18NUtil.getMessage(MESSAGE_NOT_CLAIM, extractName(labelClaim.getLabelClaim()));
		formulatedProduct.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, message,
				labelClaim.getLabelClaim(), new ArrayList<NodeRef>(), RequirementDataType.Specification));
	}

	private List<LabelClaimListDataItem> extractRequirements(ProductData formulatedProduct) {
		List<LabelClaimListDataItem> ret = new ArrayList<>();
		if (formulatedProduct.getProductSpecifications() != null) {
			for (ProductSpecificationData specification : formulatedProduct.getProductSpecifications()) {
				mergeRequirements(ret, extractRequirements(specification));
				if (getDataListVisited(specification) != null) {
					mergeRequirements(ret, getDataListVisited(specification));
				}
			}
		}
		return ret;
	}

	private void mergeRequirements(List<LabelClaimListDataItem> ret, List<LabelClaimListDataItem> toAdd) {
		toAdd.forEach(item -> {
			if (item.getLabelClaim() != null) {
				boolean isFound = false;
				for (LabelClaimListDataItem sl : ret) {
					if (item.getLabelClaim().equals(sl.getLabelClaim())) {
						isFound = true;
						if (Boolean.FALSE.equals(sl.getIsClaimed()) && Boolean.TRUE.equals(item.getIsClaimed())) {
							sl.setIsClaimed(true);
						}
						break;
					}
				}
				if (!isFound) {
					ret.add(item);
				}
			}
		});
	}

	private List<LabelClaimListDataItem> getDataListVisited(ProductData partProduct) {
		return partProduct.getLabelClaimList();
	}

}
