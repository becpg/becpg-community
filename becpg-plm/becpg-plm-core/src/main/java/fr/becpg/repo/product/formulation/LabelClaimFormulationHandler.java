package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
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
	
	public static final String MESSAGE_UNDEFINED_CHARACT = "message.formulate.undefined.charact";

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
		
		if(productData.getLabelClaimList()!=null && !productData.getLabelClaimList().isEmpty()){
		
			if (productData.hasCompoListEl(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
	
				productData.getLabelClaimList().forEach(l -> {if(l.getIsManual() == null || !l.getIsManual())  l.setLabelClaimValue(null);});

				Set<NodeRef> visitedProducts = new HashSet<>();
	
				for (CompoListDataItem compoItem : productData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE))) {
	
					NodeRef part = compoItem.getProduct();
					if (!visitedProducts.contains(part) && (compoItem.getQtySubFormula() != null) && (compoItem.getQtySubFormula() > 0)) {
						ProductData partProduct = alfrescoRepository.findOne(part);
						if (partProduct.getLabelClaimList() != null) {
							for (LabelClaimListDataItem labelClaim : partProduct.getLabelClaimList()) {
								if(!LabelClaimListDataItem.VALUE_NA.equals(labelClaim.getLabelClaimValue())) {
									visitPart(productData, labelClaim);
								}
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

	private void visitPart(ProductData productData, LabelClaimListDataItem subLabelClaimItem) {

		
		for (LabelClaimListDataItem labelClaimItem : productData.getLabelClaimList()) {

			if (((labelClaimItem.getIsManual() == null) || !labelClaimItem.getIsManual())
					&& ((labelClaimItem.getLabelClaim() != null) && labelClaimItem.getLabelClaim().equals(subLabelClaimItem.getLabelClaim()))) {
				if (subLabelClaimItem.getLabelClaimValue() != null) {
					switch (subLabelClaimItem.getLabelClaimValue()) {
					case LabelClaimListDataItem.VALUE_TRUE:
						if (labelClaimItem.getLabelClaimValue() == null) {
							labelClaimItem.setIsClaimed(true);
						}
						break;
					case LabelClaimListDataItem.VALUE_FALSE:
						if (labelClaimItem.getIsClaimed() || (labelClaimItem.getLabelClaimValue() == null)) {
							labelClaimItem.setIsClaimed(false);
						}
						break;
					case LabelClaimListDataItem.VALUE_EMPTY:
					default:
						labelClaimItem.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);
						break;
					}
				} else {
					labelClaimItem.setLabelClaimValue(LabelClaimListDataItem.VALUE_EMPTY);
				}

			}
		}

	}
	
	private void computeClaimList(ProductData productData, ExpressionParser parser, StandardEvaluationContext context) {
		// ClaimLabel list
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

					String message = I18NUtil.getMessage("message.formulate.labelClaim.error", Locale.getDefault(),
							nodeService.getProperty(labelClaimListDataItem.getLabelClaim(), BeCPGModel.PROP_CHARACT_NAME),
							labelClaimListDataItem.getErrorLog());
					productData.getCompoListView().getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message,
							labelClaimListDataItem.getLabelClaim(), new ArrayList<NodeRef>()));
				}

			}
		}

	}

}
