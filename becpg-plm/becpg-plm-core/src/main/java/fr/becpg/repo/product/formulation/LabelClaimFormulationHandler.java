package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

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
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * <p>LabelClaimFormulationHandler class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LabelClaimFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(LabelClaimFormulationHandler.class);

	/** Constant <code>MESSAGE_MISSING_CLAIM="message.formulate.labelClaim.missing"</code> */
	public static final String MESSAGE_MISSING_CLAIM = "message.formulate.labelClaim.missing";

	/** Constant <code>MESSAGE_LABELCLAIM_ERROR="message.formulate.labelClaim.error"</code> */
	public static final String MESSAGE_LABELCLAIM_ERROR = "message.formulate.labelClaim.error";

	private NodeService nodeService;

	private NodeService mlNodeService;

	private SpelFormulaService formulaService;

	private AlfrescoRepository<ProductData> alfrescoRepository;

	/**
	 * <p>Setter for the field <code>mlNodeService</code>.</p>
	 *
	 * @param mlNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setMlNodeService(NodeService mlNodeService) {
		this.mlNodeService = mlNodeService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>formulaService</code>.</p>
	 *
	 * @param formulaService a {@link fr.becpg.repo.formulation.spel.SpelFormulaService} object.
	 */
	public void setFormulaService(SpelFormulaService formulaService) {
		this.formulaService = formulaService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData productData) throws FormulateException {

		if (productData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) 
				|| productData instanceof ProductSpecificationData) {
			return true;
		}
		
		if(productData instanceof ProductSpecificationData) {
			return true;
		}

		int sort = 2;
		if (((productData.getEntityTpl() != null) && !productData.getEntityTpl().equals(productData))) {
			synchronizeTemplate(productData.getEntityTpl().getLabelClaimList(), productData.getLabelClaimList(), sort);
		}
		

		List<ProductSpecificationData> specDatas = new LinkedList<>(extractSpecifications(productData.getProductSpecifications()));
		if ((specDatas != null) && !specDatas.isEmpty()) {
			specDatas.sort((o1, o2) -> {
				return o1.getName().compareTo(o2.getName());
			});

			for (ProductSpecificationData specData : specDatas) {
				synchronizeTemplate(specData.getLabelClaimList(), productData.getLabelClaimList(), ++sort);
			}
		}

		
		
		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext context = formulaService.createEntitySpelContext(productData);

		if ((productData.getLabelClaimList() != null) && !productData.getLabelClaimList().isEmpty()) {
			if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

		
		
				Set<NodeRef> resetedClaim = new HashSet<>();

				productData.getLabelClaimList().forEach(l -> {

					l.getMissingLabelClaims().clear();

					l.setType((String) nodeService.getProperty(l.getLabelClaim(), PLMModel.PROP_LABEL_CLAIM_TYPE));

					Boolean isManual = (Boolean) nodeService.getProperty(l.getLabelClaim(), BeCPGModel.PROP_IS_MANUAL_LISTITEM);

					if ((isManual != null) && isManual) {
						l.setIsManual(true);
					}

				});

				Set<NodeRef> visitedProducts = new HashSet<>();

				for (CompoListDataItem compoItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {

					NodeRef part = compoItem.getProduct();
					if (!visitedProducts.contains(part) && (compoItem.getQtySubFormula() != null) && (compoItem.getQtySubFormula() > 0)) {
						ProductData partProduct = alfrescoRepository.findOne(part);
						if (partProduct.getLabelClaimList() != null) {
							for (LabelClaimListDataItem labelClaim : partProduct.getLabelClaimList()) {

								visitPart(productData, partProduct, labelClaim, resetedClaim);

							}
						}
						visitedProducts.add(part);
					}
				}
			}

			computeClaimList(productData, parser, context);

		}

		return true;
	}

	private Set<ProductSpecificationData> extractSpecifications(List<ProductSpecificationData> specifications) {

		Set<ProductSpecificationData> ret = new HashSet<>();
		if (specifications != null) {
			for (ProductSpecificationData specification : specifications) {
				ret.add(specification);
				ret.addAll(extractSpecifications(specification.getProductSpecifications()));
			}
		}
		return ret;
	}

	private void synchronizeTemplate(List<LabelClaimListDataItem> templateSimpleListDataList, List<LabelClaimListDataItem> simpleListDataList,
			int sort) {

		if ((templateSimpleListDataList != null) && (simpleListDataList != null)) {

			for (LabelClaimListDataItem tsl : templateSimpleListDataList) {
				if (tsl.getLabelClaim() != null) {
					boolean isFound = false;
					for (LabelClaimListDataItem sl : simpleListDataList) {
						if (tsl.getLabelClaim().equals(sl.getLabelClaim())) {
							isFound = true;
							break;
						}
					}
					if (!isFound) {
						LabelClaimListDataItem toAdd = tsl.clone();
						toAdd.setName(null);
						toAdd.setNodeRef(null);
						toAdd.setParentNodeRef(null);
						toAdd.setLabelClaimValue(null);
						simpleListDataList.add(toAdd);
					}

				}
			}

			// check sorting
			int lastSort = 0;
			for (LabelClaimListDataItem sl : simpleListDataList) {
				if (sl.getLabelClaim() != null) {
					boolean isFound = false;

					for (LabelClaimListDataItem tsl : templateSimpleListDataList) {
						if (sl.getLabelClaim().equals(tsl.getLabelClaim())) {
							isFound = true;
							Integer tplSort = tsl.getSort();
							
							lastSort = (tplSort!=null ?tplSort : 0 ) * (10 ^ sort);
							sl.setSort(lastSort);
						}
					}

					if (!isFound) {
						sl.setSort(++lastSort);
					}
				}
			}
		}
	}

	private void visitPart(ProductData productData, ProductData partProduct, LabelClaimListDataItem subLabelClaimItem, Set<NodeRef> resetedClaim) {
		for (LabelClaimListDataItem labelClaimItem : productData.getLabelClaimList()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Visiting labelClaim " + extractName(labelClaimItem.getLabelClaim()) + " isManual: " + labelClaimItem.getIsManual()
						+ " equals to subLabelClaimItem: " + subLabelClaimItem.equals(labelClaimItem));
			}
			if (((labelClaimItem.getIsManual() == null) || !labelClaimItem.getIsManual())
					&& ((labelClaimItem.getLabelClaim() != null) && labelClaimItem.getLabelClaim().equals(subLabelClaimItem.getLabelClaim()))) {

				if (!resetedClaim.contains(labelClaimItem.getLabelClaim())) {

					if ((labelClaimItem.getIsManual() == null) || !labelClaimItem.getIsManual()) {
						labelClaimItem.setLabelClaimValue(null);
					}

					resetedClaim.add(labelClaimItem.getLabelClaim());
				}

				if (subLabelClaimItem.getLabelClaimValue() != null) {
					switch (subLabelClaimItem.getLabelClaimValue()) {
					case LabelClaimListDataItem.VALUE_TRUE:
						if ((labelClaimItem.getLabelClaimValue() == null)
								|| LabelClaimListDataItem.VALUE_NA.toString().equals(labelClaimItem.getLabelClaimValue())) {
							labelClaimItem.setIsClaimed(true);
						}
						break;
					case LabelClaimListDataItem.VALUE_NA:
						if (labelClaimItem.getLabelClaimValue() == null) {
							labelClaimItem.setLabelClaimValue(LabelClaimListDataItem.VALUE_NA);
						}
						break;
					case LabelClaimListDataItem.VALUE_SUITABLE:
						if (labelClaimItem.getIsClaimed() || (labelClaimItem.getLabelClaimValue() == null)
								|| LabelClaimListDataItem.VALUE_NA.toString().equals(labelClaimItem.getLabelClaimValue())) {
							labelClaimItem.setLabelClaimValue(LabelClaimListDataItem.VALUE_SUITABLE);
						}
						break;
					case LabelClaimListDataItem.VALUE_FALSE:
						if ((labelClaimItem.getLabelClaimValue() == null) || !labelClaimItem.getLabelClaimValue().isEmpty()) {
							labelClaimItem.setIsClaimed(false);
						}
						if ((labelClaimItem.getIsFormulated() == null) || !labelClaimItem.getIsFormulated()) {
							addMissingLabelClaims(partProduct, labelClaimItem);
						}

						break;
					case LabelClaimListDataItem.VALUE_EMPTY:
					default:
						if (logger.isDebugEnabled()) {
							logger.debug("case empty/default for " + extractName(subLabelClaimItem.getLabelClaim()) + " (value is: \""
									+ subLabelClaimItem.getLabelClaimValue() + "\")");
						}
						if (!labelClaimItem.getIsFormulated()) {
							addMissingLabelClaimReq(productData, partProduct, labelClaimItem);
							addMissingLabelClaims(partProduct, labelClaimItem);
						}

						labelClaimItem.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);
						break;
					}
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug(extractName(subLabelClaimItem.getLabelClaim()) + " has null label claim value");
					}

					if ((labelClaimItem.getIsFormulated() == null) || !labelClaimItem.getIsFormulated()) {
						addMissingLabelClaimReq(productData, partProduct, labelClaimItem);
						addMissingLabelClaims(partProduct, labelClaimItem);

					}

					labelClaimItem.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);
				}
			}
		}
	}

	private void addMissingLabelClaims(ProductData partProduct, LabelClaimListDataItem labelClaimItem) {
		logger.debug("Adding part " + partProduct.getName() + " (" + partProduct.getNodeRef() + ") to missing list");
		labelClaimItem.getMissingLabelClaims().add(partProduct.getNodeRef());
		List<LabelClaimListDataItem> partMatchingLclItems = getLclItemFromProduct(partProduct, labelClaimItem.getLabelClaim());
		for (LabelClaimListDataItem matchingLclItem : partMatchingLclItems) {
			labelClaimItem.getMissingLabelClaims().addAll(matchingLclItem.getMissingLabelClaims());
		}
	}

	private void addMissingLabelClaimReq(ProductData productData, ProductData partProduct, LabelClaimListDataItem labelClaimItem) {

		productData.getReqCtrlList()
				.add(new ReqCtrlListDataItem(null, RequirementType.Info,
						MLTextHelper.getI18NMessage(MESSAGE_MISSING_CLAIM,
								mlNodeService.getProperty(labelClaimItem.getLabelClaim(), BeCPGModel.PROP_CHARACT_NAME)),
						labelClaimItem.getLabelClaim(), new ArrayList<>(Arrays.asList(partProduct.getNodeRef())), RequirementDataType.Labelclaim));

	}

	private List<LabelClaimListDataItem> getLclItemFromProduct(ProductData product, NodeRef lclCharact) {
		return product.getLabelClaimList().stream().filter(lcl -> lclCharact.equals(lcl.getLabelClaim())).collect(Collectors.toList());
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

					String formulaText = (String) nodeService.getProperty(labelClaimListDataItem.getLabelClaim(), PLMModel.PROP_LABEL_CLAIM_FORMULA);
					if ((formulaText != null) && (formulaText.length() > 0)) {
						try {
							labelClaimListDataItem.setIsFormulated(true);

							String[] formulas = SpelHelper.formatMTFormulas(formulaText);
							for (String formula : formulas) {

								Matcher varFormulaMatcher = SpelHelper.formulaVarPattern.matcher(formula);
								if (varFormulaMatcher.matches()) {
									Expression exp = parser.parseExpression(varFormulaMatcher.group(2));
									context.setVariable(varFormulaMatcher.group(1), exp.getValue(context));
								} else {
									Expression exp = parser.parseExpression(formula);
									Object ret = exp.getValue(context);
									if (ret instanceof Boolean) {
										labelClaimListDataItem.setIsClaimed((Boolean) ret);
									} else {
										labelClaimListDataItem.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);
										labelClaimListDataItem.setErrorLog(
												I18NUtil.getMessage("message.formulate.formula.incorrect.type.boolean", Locale.getDefault()));
									}
								}
							}

						} catch (Exception e) {
							labelClaimListDataItem.setErrorLog(e.getLocalizedMessage());
							if (logger.isDebugEnabled()) {
								logger.debug("Error in formula :" + SpelHelper.formatFormula(formulaText), e);
							}
						}
					}
				}

				if (labelClaimListDataItem.getErrorLog() != null) {

					productData.getReqCtrlList()
							.add(new ReqCtrlListDataItem(null, RequirementType.Tolerated,
									MLTextHelper.getI18NMessage(MESSAGE_LABELCLAIM_ERROR,
											mlNodeService.getProperty(labelClaimListDataItem.getLabelClaim(), BeCPGModel.PROP_CHARACT_NAME),
											labelClaimListDataItem.getErrorLog()),
									labelClaimListDataItem.getLabelClaim(), new ArrayList<NodeRef>(), RequirementDataType.Labelclaim));
				}

			}
		}

	}

}
