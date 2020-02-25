package fr.becpg.repo.report.search.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

@Service
public class DefaultExcelReportSearchPlugin implements ExcelReportSearchPlugin {

	@Autowired
	protected NodeService nodeService;

	@Autowired
	protected PermissionService permissionService;

	@Autowired
	protected EntityListDAO entityListDAO;

	@Autowired
	protected AttributeExtractorService attributeExtractorService;

	@Autowired
	protected AssociationService associationService;

	@Autowired
	protected NamespaceService namespaceService;

	@Autowired
	protected EntityDictionaryService entityDictionaryService;

	@Override
	public void fillSheet(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum, String[] parameters,
			AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache) {
		
		for (NodeRef entityNodeRef : searchResults) {
			if (entityDictionaryService.isSubClass(nodeService.getType(entityNodeRef), mainType)) {
				if (keyColumn != null) {
					Serializable key = nodeService.getProperty(entityNodeRef, keyColumn.getFieldDef().getName());
					if (key == null) {
						key = nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CODE);
					}
					if (key == null) {
						key = nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
					}

					NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
					NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, itemType);
					if (listNodeRef != null) {
						Map<String, Object> entityItems = getEntityProperties(entityNodeRef, mainType, metadataFields, cache);
						List<NodeRef> results = entityListDAO.getListItems(listNodeRef, itemType);
						for (NodeRef itemNodeRef : results) {
							if (itemType.equals(nodeService.getType(itemNodeRef))) {
								if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
									rownum = fillRow(sheet, itemNodeRef, itemType, metadataFields, cache, rownum, key, entityItems);
								}
							}
						}
					}
				} else {
					rownum = fillRow(sheet, entityNodeRef, itemType, metadataFields, cache, rownum, null, null);
				}
			}
		}

	}

	protected Map<String, Object> getEntityProperties(NodeRef itemNodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			Map<NodeRef, Map<String, Object>> cache) {
		
		Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
		Map<String, Object> item = doExtract(itemNodeRef, itemType, metadataFields, properties, cache);
		Map<String, Object> tmp = item.entrySet().stream().filter(map -> map.getKey().contains("entity_") && (map.getValue() != null))
				.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		return tmp;
	}

	protected int fillRow(XSSFSheet sheet, NodeRef itemNodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			Map<NodeRef, Map<String, Object>> cache, int rownum, Serializable key, Map<String, Object> entityItems) {

		Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
		Map<String, Object> item = doExtract(itemNodeRef, itemType, metadataFields, properties, cache);
		if (entityItems != null) {
			item.putAll(entityItems);
		}

		Row row = sheet.createRow(rownum++);

		int cellNum = 0;
		Cell cell = row.createCell(cellNum++);
		cell.setCellValue("VALUES");

		if (key != null) {
			cell = row.createCell(cellNum++);
			cell.setCellValue(String.valueOf(key));
		}

		for (AttributeExtractorStructure metadataField : metadataFields) {
			if (metadataField.isFormulaField()) {
				if(metadataField.getFieldName().startsWith("formula") || metadataField.getFieldName().startsWith("dyn_")) {
					item.put(metadataField.getFieldName(), eval(metadataField.getFormula(), item));
				} else {
					item.put(metadataField.getFieldName(),metadataField.getFormula());
				}
			}

		}
		
		cellNum = ExcelHelper.appendExcelField(metadataFields, null, item, sheet, row, cellNum, rownum, null);

		return rownum;
	}

	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			Map<QName, Serializable> properties, final Map<NodeRef, Map<String, Object>> cache) {
		

		return attributeExtractorService.extractNodeData(nodeRef, itemType, properties, metadataFields, AttributeExtractorMode.XLSX,
				new AttributeExtractorService.DataListCallBack() {

					@Override
					public List<Map<String, Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field) {
						List<Map<String, Object>> ret = new ArrayList<>();
						if (field.isDataListItems()) {
							NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
							NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, field.getFieldQname());
							if (listNodeRef != null) {
								List<NodeRef> results = entityListDAO.getListItems(listNodeRef, field.getFieldQname());

								for (NodeRef itemNodeRef : results) {
									addExtracted(itemNodeRef, field, cache, ret);
								}
							}
						} else if (field.isEntityField()) {
							NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
							addExtracted(entityNodeRef, field, cache, ret);

						} else {

							if (field.getFieldDef() instanceof AssociationDefinition) {
								List<NodeRef> assocRefs;
								if (((AssociationDefinition) field.getFieldDef()).isChild()) {
									assocRefs = associationService.getChildAssocs(nodeRef, field.getFieldDef().getName());
								} else {
									assocRefs = associationService.getTargetAssocs(nodeRef, field.getFieldDef().getName());
								}
								for (NodeRef itemNodeRef : assocRefs) {
									addExtracted(itemNodeRef, field, cache, ret);
								}

							}
						}

						return ret;
					}

					private void addExtracted(NodeRef itemNodeRef, AttributeExtractorStructure field, Map<NodeRef, Map<String, Object>> cache,
							List<Map<String, Object>> ret) {
						if (cache.containsKey(itemNodeRef)) {
							ret.add(cache.get(itemNodeRef));
						} else {
							if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
								QName itemType = nodeService.getType(itemNodeRef);
								Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
								ret.add(doExtract(itemNodeRef, itemType, field.getChildrens(), properties, cache));
							}
						}
					}

				});
		
		
	}


	public class FormulaContext {
		private Map<String, Object> props;

		FormulaContext(Map<String, Object> props) {
			this.props = props;
		}

		public Map<String, Object> getProps() {
			return props;
		}

		public void setProps(Map<String, Object> props) {
			this.props = props;
		}
	}

	protected Object eval(String formula, Map<String, Object> values) {

		if(formula.startsWith("dyn_")){
			return values.get(formula);
		}
		
		EvaluationContext context = new StandardEvaluationContext(new FormulaContext(values));
		ExpressionParser parser = new SpelExpressionParser();
	
		Expression exp = parser.parseExpression(formula);
		Object ret = exp.getValue(context);
		
		return ret;

	}
	
	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public boolean isApplicable(QName itemType, String[] parameters) {
		return false;
	}

	
}
