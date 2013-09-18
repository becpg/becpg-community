package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.data.productList.RequirementType;
import fr.becpg.repo.product.data.spel.FormulaFormulationContext;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.security.BeCPGAccessDeniedException;
import fr.becpg.repo.security.aop.SecurityMethodBeforeAdvice;

/**
 * Use Spring EL to parse formula and compute value
 * 
 * @author matthieu
 * 
 */
@Service
public class FormulaFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static Log logger = LogFactory.getLog(FormulaFormulationHandler.class);

	public static final int DYN_COLUMN_SIZE = 5;
	public static final String DYN_COLUMN_NAME = "bcpg:dynamicCharactColumn";

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NodeService nodeService;

	private NamespaceService namespaceService;

	private SecurityMethodBeforeAdvice securityMethodBeforeAdvice;

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setSecurityMethodBeforeAdvice(SecurityMethodBeforeAdvice securityMethodBeforeAdvice) {
		this.securityMethodBeforeAdvice = securityMethodBeforeAdvice;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	
	@Override
	public boolean process(ProductData productData) throws FormulateException {

		copyTemplateDynamicCharactLists(productData);

		ExpressionParser parser = new SpelExpressionParser();
		StandardEvaluationContext context = new StandardEvaluationContext(createSecurityProxy(productData));

		for (AbstractProductDataView view : productData.getViews()) {
			computeFormula(productData, parser, context, view);
		}

		// ClaimLabel list

		if (productData.getLabelClaimList() != null) {
			for (LabelClaimListDataItem labelClaimListDataItem : productData.getLabelClaimList()) {
				labelClaimListDataItem.setIsFormulated(false);
				labelClaimListDataItem.setErrorLog(null);
				if ((labelClaimListDataItem.getIsManual() == null || !labelClaimListDataItem.getIsManual()) && labelClaimListDataItem.getLabelClaim() != null) {

					String formula = (String) nodeService.getProperty(labelClaimListDataItem.getLabelClaim(), BeCPGModel.PROP_LABEL_CLAIM_FORMULA);
					if (formula != null && formula.length() > 0) {
						try {
							labelClaimListDataItem.setIsFormulated(true);
							Expression exp = parser.parseExpression(formatFormula(formula));
							Object ret = exp.getValue(context);
							if (ret instanceof Boolean) {
								labelClaimListDataItem.setIsClaimed((Boolean) ret);
							} else {
								labelClaimListDataItem.setErrorLog(I18NUtil.getMessage("message.formulate.formula.incorrect.type.boolean", Locale.getDefault()));
							}

						} catch (Exception e) {
							labelClaimListDataItem.setErrorLog(e.getLocalizedMessage());
							if (logger.isDebugEnabled()) {
								logger.info("Error in formula :" + formatFormula(formula), e);
							}
						}
					}
				}

				if (labelClaimListDataItem.getErrorLog() != null) {

					String message = I18NUtil.getMessage("message.formulate.labelCLaim.error", Locale.getDefault(),
							nodeService.getProperty(labelClaimListDataItem.getLabelClaim(), ContentModel.PROP_NAME), labelClaimListDataItem.getErrorLog());
					productData.getCompoListView().getReqCtrlList().add(new ReqCtrlListDataItem(null, RequirementType.Tolerated, message, new ArrayList<NodeRef>()));
				}

			}
		}

		return true;
	}

	private Object createSecurityProxy(ProductData productData) {
		ProxyFactory factory = new ProxyFactory();
		factory.setTarget(productData);
		factory.addAdvice(securityMethodBeforeAdvice);
		return (ProductData) factory.getProxy();

	}

	private void computeFormula(ProductData productData, ExpressionParser parser, EvaluationContext context, AbstractProductDataView view) {

		if (view.getDynamicCharactList() != null) {

			Set<QName> nullDynColumnNames = new HashSet<QName>(DYN_COLUMN_SIZE);
			for (int i = 1; i <= DYN_COLUMN_SIZE; i++) {
				nullDynColumnNames.add(QName.createQName(DYN_COLUMN_NAME + i, namespaceService));
			}

			for (DynamicCharactListItem dynamicCharactListItem : view.getDynamicCharactList()) {
				try {
					String formula = formatFormula(dynamicCharactListItem.getFormula());
					logger.debug("Parse formula : " + formula + " (" + dynamicCharactListItem.getName() + ")");
					Expression exp = parser.parseExpression(formula);

					if (dynamicCharactListItem.getColumnName() != null && !dynamicCharactListItem.getColumnName().isEmpty()) {
						QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), namespaceService);
						if (nullDynColumnNames.contains(columnName)) {
							nullDynColumnNames.remove(columnName);
						}
						for (CompositionDataItem dataListItem : view.getMainDataList()) {
							// check node is not in memory (water when yield)
							if (dataListItem.getNodeRef() != null) {
								EvaluationContext dataContext = new StandardEvaluationContext(new FormulaFormulationContext(alfrescoRepository,productData, dataListItem));
								Object value = exp.getValue(dataContext);
								dataListItem.getExtraProperties().put(columnName, (Serializable) value);
								logger.debug("Value :" + value);
							}
						}
						dynamicCharactListItem.setValue(null);
					} else {
						dynamicCharactListItem.setValue(exp.getValue(context));
						logger.debug("Value :" + dynamicCharactListItem.getValue());
					}
					dynamicCharactListItem.setErrorLog(null);
				} catch (Exception e) {
					if (e.getCause() != null && e.getCause().getCause() instanceof BeCPGAccessDeniedException) {
						dynamicCharactListItem.setValue("#AccessDenied");
					} else {
						dynamicCharactListItem.setValue("#Error");
					}
					dynamicCharactListItem.setErrorLog(e.getLocalizedMessage());

					if (logger.isDebugEnabled()) {
						logger.debug("Error in formula :" + dynamicCharactListItem.getFormula() + " (" + dynamicCharactListItem.getName() + ")", e);
					}
				}
			}

			// remove null columns
			for (QName nullDynColumnName : nullDynColumnNames) {
				for (CompositionDataItem dataListItem : view.getMainDataList()) {
					dataListItem.getExtraProperties().put(nullDynColumnName, null);
				}
			}
		}

	}

	private String formatFormula(String formula) {
		return formula.replace("&lt;", "<").replace("&gt;", ">");
	}

	// Formule.Si("Mon tableau de compoList", "Ma formule de calcul",
	// "MonOperateur") par ex:
	//
	// Je veux la somme des poids net (g) des enfants de "Légumes" qui est
	// "2631,58" (cf. copie d'écran)
	//

	/**
	 * Copy missing item from template
	 * 
	 * @param formulatedProduct
	 * @param simpleListDataList
	 */
	private void copyTemplateDynamicCharactLists(ProductData formulatedProduct) {
		if (formulatedProduct.getEntityTplRef() != null) {
			ProductData templateProductData = alfrescoRepository.findOne(formulatedProduct.getEntityTplRef());

			copyTemplateDynamicCharactList(templateProductData.getCompoListView().getDynamicCharactList(), formulatedProduct.getCompoListView().getDynamicCharactList());
			copyTemplateDynamicCharactList(templateProductData.getPackagingListView().getDynamicCharactList(), formulatedProduct.getPackagingListView().getDynamicCharactList());
			copyTemplateDynamicCharactList(templateProductData.getProcessListView().getDynamicCharactList(), formulatedProduct.getProcessListView().getDynamicCharactList());
		}
	}

	protected void copyTemplateDynamicCharactList(List<DynamicCharactListItem> sourceList, List<DynamicCharactListItem> targetList) {

		for (DynamicCharactListItem sourceItem : sourceList) {
			if (sourceItem.getTitle() != null) {
				boolean isFound = false;
				for (DynamicCharactListItem targetItem : targetList) {
					// charact renamed
					if (sourceItem.getName().equals(targetItem.getName()) && !sourceItem.getTitle().equals(targetItem.getTitle())) {
						targetItem.setTitle(sourceItem.getTitle());
					}
					// update formula
					if (sourceItem.getTitle().equals(targetItem.getTitle())) {
						if (targetItem.getIsManual() == null || targetItem.getIsManual() == false) {
							targetItem.setFormula(sourceItem.getFormula());
							targetItem.setColumnName(sourceItem.getColumnName());
							targetItem.setGroupColor(sourceItem.getGroupColor());
							targetItem.setIsManual(sourceItem.getIsManual());
						}
						isFound = true;
						break;
					}
				}
				if (!isFound) {
					sourceItem.setNodeRef(null);
					sourceItem.setParentNodeRef(null);
					targetList.add(sourceItem);
				}
			}

		}
	}
}
