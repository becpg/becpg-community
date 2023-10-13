/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.formulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.formulation.spel.SpelHelper.SpelShortcut;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.DynamicCharactExecOrder;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;

/**
 * Use Spring EL to parse formula and compute value
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FormulaFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(FormulaFormulationHandler.class);

	static {
		SpelHelper
				.registerShortcut(new SpelShortcut("cost\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "costList.^[cost.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("nut\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "nutList.^[nut.toString() == '$1']"));
		SpelHelper.registerShortcut(
				new SpelShortcut("allergen\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "allergenList.^[allergen.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("ing\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "ingList.^[ing.toString() == '$1']"));
		SpelHelper.registerShortcut(
				new SpelShortcut("organo\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "organoList.^[organo.toString() == '$1']"));
		SpelHelper.registerShortcut(
				new SpelShortcut("physico\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "physicoChemList.^[physicoChem.toString() == '$1']"));
		SpelHelper.registerShortcut(
				new SpelShortcut("microbio\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "microbioList.^[microbio.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("compo\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",
				"compoListView.compoList.^[product.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("process\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",
				"processListView.processList.^[resource.toString() == '$1']"));
		SpelHelper.registerShortcut(
				new SpelShortcut("resParam\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "resourceParamList.^[param.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("pack\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",
				"packagingListView.packagingList.^[product.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("packaging\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",
				"packagingListView.packagingList.^[product.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("compoVar\\['(.*?)'\\]", "compoListView.dynamicCharactList.^[title == '$1']?.value"));
		SpelHelper.registerShortcut(new SpelShortcut("packVar\\['(.*?)'\\]", "packagingListView.dynamicCharactList.^[title == '$1']?.value"));
		SpelHelper.registerShortcut(new SpelShortcut("packagingVar\\['(.*?)'\\]", "packagingListView.dynamicCharactList.^[title == '$1']?.value"));
		SpelHelper.registerShortcut(new SpelShortcut("processVar\\['(.*?)'\\]", "processListView.dynamicCharactList.^[title == '$1']?.value"));
		SpelHelper.registerShortcut(
				new SpelShortcut("labelClaim\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]", "labelClaimList.^[labelClaim.toString() == '$1']"));
		SpelHelper.registerShortcut(new SpelShortcut("labeling\\['(workspace://SpacesStore/[a-z0-9A-Z\\\\-]*)'\\]",
				"labelingListView.ingLabelingList.^[grp.toString() == '$1']"));
	}

	private NodeService nodeService;

	private SpelFormulaService formulaService;

	private DynamicCharactExecOrder execOrder = DynamicCharactExecOrder.Post;

	/**
	 * <p>Setter for the field <code>execOrder</code>.</p>
	 *
	 * @param execOrder a {@link fr.becpg.repo.product.data.productList.DynamicCharactExecOrder} object.
	 */
	public void setExecOrder(DynamicCharactExecOrder execOrder) {
		this.execOrder = execOrder;
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

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData productData) {

		if (!(productData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (productData instanceof ProductSpecificationData))) {

			if (DynamicCharactExecOrder.Pre.equals(execOrder)) {
				copyTemplateDynamicCharactLists(productData);
			}

			StandardEvaluationContext context = formulaService.createEntitySpelContext(productData);

			for (AbstractProductDataView view : productData.getViews()) {
				FormulaHelper.computeFormula(productData, context, view, execOrder);
			}

			if (DynamicCharactExecOrder.Post.equals(execOrder)) {
				computeNutrientProfile(productData, formulaService.getSpelParser(), context);
			}
		}
		return true;
	}

	private void computeNutrientProfile(ProductData productData, ExpressionParser parser, StandardEvaluationContext context) {
		if (productData.getNutrientProfile() != null && nodeService.exists(productData.getNutrientProfile())) {
			String scoreformula = (String) nodeService.getProperty(productData.getNutrientProfile(), PLMModel.PROP_NUTRIENT_PROFILE_SCORE_FORMULA);
			if ((scoreformula != null) && (scoreformula.length() > 0)) {
				try {
					productData.setNutrientScore(null);
					productData.setNutrientClass(null);
					Expression exp = parser.parseExpression(SpelHelper.formatFormula(scoreformula));
					Object ret = exp.getValue(context);
					if (ret instanceof Number) {
						productData.setNutrientScore(Double.valueOf(ret.toString()));
						String classformula = (String) nodeService.getProperty(productData.getNutrientProfile(),
								PLMModel.PROP_NUTRIENT_PROFILE_CLASS_FORMULA);
						if ((classformula != null) && (classformula.length() > 0)) {
							exp = parser.parseExpression(SpelHelper.formatFormula(classformula));
							productData.setNutrientClass((String) exp.getValue(context));
						}
					} else {
						productData.setNutrientClass(I18NUtil.getMessage("message.formulate.formula.incorrect.nutrientProfile",
								I18NUtil.getMessage("message.formulate.formula.incorrect.type.double", Locale.getDefault()), Locale.getDefault()));
					}
				} catch (Exception e) {
					MLText errorMsg = MLTextHelper.getI18NMessage("message.formulate.formula.incorrect.nutrientProfile", e.getLocalizedMessage());

					productData.setNutrientClass(MLTextHelper.getClosestValue(errorMsg, Locale.getDefault()));

					productData.getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Forbidden, errorMsg, null, new ArrayList<>(Arrays.asList(productData.getNodeRef())),
							RequirementDataType.Formulation));

					if (logger.isDebugEnabled()) {
						logger.warn("Error in nutrient score formula :" + SpelHelper.formatFormula(scoreformula));
						logger.trace(e, e);
					}
				}
			}

		} else {
			productData.setNutrientScore(null);
			productData.setNutrientClass(null);
		}

	}

	/**
	 * Copy missing item from template
	 *
	 * @param formulatedProduct
	 * @param simpleListDataList
	 */
	private void copyTemplateDynamicCharactLists(ProductData formulatedProduct) {
		if ((formulatedProduct.getEntityTpl() != null) && !formulatedProduct.getEntityTpl().equals(formulatedProduct)) {
			ProductData templateProductData = formulatedProduct.getEntityTpl();

			FormulaHelper.copyTemplateDynamicCharactList(templateProductData.getCompoListView().getDynamicCharactList(),
					formulatedProduct.getCompoListView().getDynamicCharactList());
			FormulaHelper.copyTemplateDynamicCharactList(templateProductData.getPackagingListView().getDynamicCharactList(),
					formulatedProduct.getPackagingListView().getDynamicCharactList());
			FormulaHelper.copyTemplateDynamicCharactList(templateProductData.getProcessListView().getDynamicCharactList(),
					formulatedProduct.getProcessListView().getDynamicCharactList());
		}

		if (formulatedProduct.getProductSpecifications() != null) {
			for (ProductSpecificationData specData : formulatedProduct.getProductSpecifications()) {
				FormulaHelper.copyTemplateDynamicCharactList(specData.getDynamicCharactList(), formulatedProduct.getCompoListView().getDynamicCharactList());
			}
		}

	}

}
