package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.List;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * Use Spring EL to parse formula and compute value
 * 
 * @author matthieu
 * 
 */
@Service
public class FormulaFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static Log logger = LogFactory.getLog(FormulaFormulationHandler.class);

	private AlfrescoRepository<ProductData> alfrescoRepository;

	private NamespaceService namespaceService;

	public void setAlfrescoRepository(AlfrescoRepository<ProductData> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public class FormulaFormulationContext {
		ProductData entity;
		CompositionDataItem dataListItem;

		public FormulaFormulationContext(ProductData entity, CompositionDataItem dataListItem) {
			super();
			this.entity = entity;
			this.dataListItem = dataListItem;
		}

		public ProductData getEntity() {
			return entity;
		}

		public CompositionDataItem getDataListItem() {
			return dataListItem;
		}

		public ProductData getDataListItemEntity() {
			return dataListItem.getProduct() != null ? alfrescoRepository.findOne(dataListItem.getProduct()) : null;
		}

	}

	@Override
	public boolean process(ProductData productData) throws FormulateException {

		copyTemplateDynamicCharactLists(productData);

		ExpressionParser parser = new SpelExpressionParser();
		EvaluationContext context = new StandardEvaluationContext(productData);

		for (AbstractProductDataView view : productData.getViews()) {
			computeFormula(productData, parser, context, view);
		}

		return true;
	}

	private void computeFormula(ProductData productData, ExpressionParser parser, EvaluationContext context, AbstractProductDataView view) {

		if (view.getDynamicCharactList() != null) {

			for (DynamicCharactListItem dynamicCharactListItem : view.getDynamicCharactList()) {
				try {
					logger.debug("Parse formula : " + dynamicCharactListItem.getFormula() + " (" + dynamicCharactListItem.getName() + ")");
					Expression exp = parser.parseExpression(dynamicCharactListItem.getFormula());

					if (dynamicCharactListItem.getColumnName() != null && !dynamicCharactListItem.getColumnName().isEmpty()) {
						QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), namespaceService);
						for (CompositionDataItem dataListItem : view.getMainDataList()) {
							EvaluationContext dataContext = new StandardEvaluationContext(new FormulaFormulationContext(productData, dataListItem));
							Object value = exp.getValue(dataContext);
							dataListItem.getExtraProperties().put(columnName, (Serializable) value);
							logger.debug("Value :" + value);
						}
						dynamicCharactListItem.setValue(null);
					} else {

						dynamicCharactListItem.setValue(exp.getValue(context));
						logger.debug("Value :" + dynamicCharactListItem.getValue());
					}
				} catch (Exception e) {
					dynamicCharactListItem.setValue("#Error");
					logger.warn("Error in formula :" + dynamicCharactListItem.getFormula() + " (" + dynamicCharactListItem.getName() + ")", e);
				}
			}

		}

	}

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
					if (sourceItem.getTitle().equals(targetItem.getTitle())) {
						targetItem.setFormula(sourceItem.getFormula());
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
