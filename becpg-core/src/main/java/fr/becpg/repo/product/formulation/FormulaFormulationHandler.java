package fr.becpg.repo.product.formulation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.springframework.stereotype.Service;

import fr.becpg.repo.security.BeCPGAccessDeniedException;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.AbstractProductDataView;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.CompositionDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.repository.AlfrescoRepository;
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
		EvaluationContext context = new StandardEvaluationContext(createSecurityProxy(productData));

		for (AbstractProductDataView view : productData.getViews()) {
			computeFormula(productData, parser, context, view);
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
			for(int i=1 ; i<=DYN_COLUMN_SIZE ; i++){
				nullDynColumnNames.add(QName.createQName(DYN_COLUMN_NAME + i, namespaceService));
			}

			for (DynamicCharactListItem dynamicCharactListItem : view.getDynamicCharactList()) {
				try {
					logger.debug("Parse formula : " + dynamicCharactListItem.getFormula() + " (" + dynamicCharactListItem.getName() + ")");
					Expression exp = parser.parseExpression(dynamicCharactListItem.getFormula());

					if (dynamicCharactListItem.getColumnName() != null && !dynamicCharactListItem.getColumnName().isEmpty()) {
						QName columnName = QName.createQName(dynamicCharactListItem.getColumnName().replaceFirst("_", ":"), namespaceService);
						if(nullDynColumnNames.contains(columnName)){
							nullDynColumnNames.remove(columnName);
						}						
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
					dynamicCharactListItem.setErrorLog(null);
				} catch (Exception e) {
					if (e.getCause() != null && e.getCause().getCause() instanceof BeCPGAccessDeniedException) {
						dynamicCharactListItem.setValue("#AccessDenied");
					} else {
						dynamicCharactListItem.setValue("#Error");
					}
					dynamicCharactListItem.setErrorLog(e.getLocalizedMessage());
					
					logger.warn("Error in formula :" + dynamicCharactListItem.getFormula() + " (" + dynamicCharactListItem.getName() + ")", e);
				}
			}
			
			// remove null columns
			for(QName nullDynColumnName : nullDynColumnNames){				
				for (CompositionDataItem dataListItem : view.getMainDataList()) {
					dataListItem.getExtraProperties().put(nullDynColumnName, null);
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
					// charact renamed					
					if (sourceItem.getName().equals(targetItem.getName()) && !sourceItem.getTitle().equals(targetItem.getTitle())) {
						targetItem.setTitle(sourceItem.getTitle());
					}
					// update formula
					if (sourceItem.getTitle().equals(targetItem.getTitle())) {
						if(targetItem.getIsManual() == null || targetItem.getIsManual() == false){
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
