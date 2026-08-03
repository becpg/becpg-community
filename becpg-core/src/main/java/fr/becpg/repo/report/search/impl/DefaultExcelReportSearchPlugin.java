package fr.becpg.repo.report.search.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.stereotype.Service;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.formulation.spel.DataListItemSpelContext;
import fr.becpg.repo.formulation.spel.SpelFormulaService;
import fr.becpg.repo.formulation.spel.SpelHelper;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>DefaultExcelReportSearchPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class DefaultExcelReportSearchPlugin implements ExcelReportSearchPlugin {

	protected static final Log logger = LogFactory.getLog(DefaultExcelReportSearchPlugin.class);

	/** Constant <code>HEADER_VALUES="VALUES"</code> */
	protected static final String HEADER_VALUES = "VALUES";

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

	@Autowired
	protected SpelFormulaService spelFormulaService;

	/** {@inheritDoc} */
	@Override
	public int fillSheet(Sheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum, String[] parameters,
			AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache) {

		ExcelCellStyles excelCellStyles = new ExcelCellStyles(sheet.getWorkbook());

		boolean includeEmpty = ExcelReportSearchPlugin.isIncludeEmpty(parameters);

		for (NodeRef entityNodeRef : searchResults) {
			if (nodeService.exists(entityNodeRef) && entityDictionaryService.isSubClass(nodeService.getType(entityNodeRef), mainType)) {
				if (keyColumn != null) {
					rownum = fillEntityListRows(sheet, entityNodeRef, mainType, itemType, rownum, keyColumn, metadataFields, cache, excelCellStyles,
							includeEmpty);
				} else {
					rownum = fillRow(sheet, entityNodeRef, entityNodeRef, itemType, metadataFields, cache, rownum, null, null, excelCellStyles);
				}
			}
		}

		return rownum;

	}

	/**
	 * <p>Appends the rows of a datalist for a single entity.</p>
	 *
	 * <p>When <code>includeEmpty</code> is set and the list holds no exportable item, a single row carrying only the
	 * entity columns is created, so that entities without the list still show up in the report.</p>
	 *
	 * @param sheet a {@link org.apache.poi.ss.usermodel.Sheet} object.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param mainType a {@link org.alfresco.service.namespace.QName} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param rownum a int.
	 * @param keyColumn a {@link fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param cache a {@link java.util.Map} object.
	 * @param excelCellStyles a {@link fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles} object.
	 * @param includeEmpty a boolean.
	 * @return a int.
	 */
	protected int fillEntityListRows(Sheet sheet, NodeRef entityNodeRef, QName mainType, QName itemType, int rownum,
			AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache,
			ExcelCellStyles excelCellStyles, boolean includeEmpty) {

		Serializable key = extractKey(entityNodeRef, keyColumn);
		Map<String, Object> entityItems = getEntityProperties(entityNodeRef, mainType, metadataFields, cache);

		int firstRownum = rownum;

		for (NodeRef itemNodeRef : getListItems(entityNodeRef, itemType)) {
			rownum = fillRow(sheet, entityNodeRef, itemNodeRef, itemType, metadataFields, cache, rownum, key, entityItems, excelCellStyles);
		}

		if (includeEmpty && (rownum == firstRownum)) {
			rownum = createEmptyEntityRow(sheet, rownum, key, metadataFields, entityItems, excelCellStyles);
		}

		return rownum;
	}

	/**
	 * <p>Returns the readable items of the given list for an entity, or an empty list when the entity has no such list.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.util.List} object.
	 */
	protected List<NodeRef> getListItems(NodeRef entityNodeRef, QName itemType) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, itemType);
		if (listNodeRef == null) {
			return Collections.emptyList();
		}

		// case of multiple lists of same type (ex: bcpg:surveyList@1)
		QName actualType = QName.createQName(itemType.toString().split("@")[0]);

		List<NodeRef> items = new ArrayList<>();
		for (NodeRef itemNodeRef : entityListDAO.getListItems(listNodeRef, actualType)) {
			if (nodeService.exists(itemNodeRef) && actualType.equals(nodeService.getType(itemNodeRef))
					&& (permissionService.hasPermission(itemNodeRef, PermissionService.READ) == AccessStatus.ALLOWED)) {
				items.add(itemNodeRef);
			}
		}
		return items;
	}

	/**
	 * <p>Extracts the key identifying an entity in the report, falling back on its code then its name.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param keyColumn a {@link fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure} object.
	 * @return a {@link java.io.Serializable} object.
	 */
	protected Serializable extractKey(NodeRef entityNodeRef, AttributeExtractorStructure keyColumn) {

		Serializable key = keyColumn != null ? nodeService.getProperty(entityNodeRef, keyColumn.getFieldDef().getName()) : null;
		if (key == null) {
			key = nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CODE);
		}
		if (key == null) {
			key = nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
		}
		return key;
	}

	/**
	 * <p>Creates a row holding only the entity columns, for an entity whose list is empty or missing.</p>
	 *
	 * @param sheet a {@link org.apache.poi.ss.usermodel.Sheet} object.
	 * @param rownum a int.
	 * @param key a {@link java.io.Serializable} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param entityItems a {@link java.util.Map} object.
	 * @param excelCellStyles a {@link fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles} object.
	 * @return a int.
	 */
	protected int createEmptyEntityRow(Sheet sheet, int rownum, Serializable key, List<AttributeExtractorStructure> metadataFields,
			Map<String, Object> entityItems, ExcelCellStyles excelCellStyles) {

		Row row = sheet.createRow(rownum++);

		int cellNum = 0;
		Cell cell = row.createCell(cellNum++);
		cell.setCellValue(HEADER_VALUES);

		if (key != null) {
			cell = row.createCell(cellNum++);
			cell.setCellValue(String.valueOf(key));
		}

		Map<String, Object> item = entityItems != null ? new HashMap<>(entityItems) : new HashMap<>();

		ExcelHelper.appendExcelField(metadataFields, null, item, sheet, row, cellNum, rownum, null, excelCellStyles);

		return rownum;
	}

	/**
	 * <p>getEntityProperties.</p>
	 *
	 * @param itemNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param cache a {@link java.util.Map} object.
	 * @return a {@link java.util.Map} object.
	 */
	protected Map<String, Object> getEntityProperties(NodeRef itemNodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			Map<NodeRef, Map<String, Object>> cache) {

		Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
		Map<String, Object> item = doExtract(itemNodeRef, itemType, metadataFields, properties, cache);
		return item.entrySet().stream().filter(map -> map.getKey().startsWith("entity_") && (map.getValue() != null))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

	/**
	 * <p>fillRow.</p>
	 *
	 * @param sheet a {@link org.apache.poi.ss.usermodel.Sheet} object.
	 * @param itemNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param cache a {@link java.util.Map} object.
	 * @param rownum a int.
	 * @param key a {@link java.io.Serializable} object.
	 * @param entityItems a {@link java.util.Map} object.
	 * @return a int.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param excelCellStyles a {@link fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles} object
	 */
	protected int fillRow(Sheet sheet, NodeRef entityNodeRef, NodeRef itemNodeRef, QName itemType,
			List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache, int rownum, Serializable key,
			Map<String, Object> entityItems, ExcelCellStyles excelCellStyles) {

		Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
		Map<String, Object> item = doExtract(itemNodeRef, itemType, metadataFields, properties, cache);
		for (Entry<String, Object> itemEntry : item.entrySet()) {
			String itemKey = itemEntry.getKey();
			Object itemValue = itemEntry.getValue();
			if (itemKey.startsWith("prop_bcpg_dynamicCharactColumn") && JsonFormulaHelper.isJsonString(itemValue)) {
				Object value = JsonFormulaHelper.cleanCompareJSON((String) itemValue);
				item.put(itemKey, value);
			}
		}
		if (entityItems != null) {
			item.putAll(entityItems);
		}

		Row row = sheet.createRow(rownum++);

		int cellNum = 0;
		Cell cell = row.createCell(cellNum++);
		cell.setCellValue(HEADER_VALUES);

		if (key != null) {
			cell = row.createCell(cellNum++);
			cell.setCellValue(String.valueOf(key));
		}

		for (AttributeExtractorStructure metadataField : metadataFields) {
			if (metadataField.isFormulaField()) {
				if (metadataField.getFieldName().startsWith("formula") || metadataField.getFieldName().startsWith("dyn_") || metadataField.getFieldName().startsWith("image_")) {
					item.put(metadataField.getFieldName(), eval(entityNodeRef, itemNodeRef, metadataField.getFormula(), item));	
				} else {
					item.put(metadataField.getFieldName(), metadataField.getFormula());
				}
			}

		}

		ExcelHelper.appendExcelField(metadataFields, null, item, sheet, row, cellNum, rownum, null, excelCellStyles);

		return rownum;
	}

	/**
	 * <p>doExtract.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object.
	 * @param metadataFields a {@link java.util.List} object.
	 * @param properties a {@link java.util.Map} object.
	 * @param cache a {@link java.util.Map} object.
	 * @return a {@link java.util.Map} object.
	 */
	protected Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			Map<QName, Serializable> properties, final Map<NodeRef, Map<String, Object>> cache) {

		if (cache != null && cache.containsKey(nodeRef)) {
			return new HashMap<>(cache.get(nodeRef));
		}

		Map<String, Object> result = attributeExtractorService.extractNodeData(nodeRef, itemType, properties, metadataFields, FormatMode.XLSX,
				new AttributeExtractorService.DataListCallBack() {

					@Override
					public List<Map<String, Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field, FormatMode mode) {
						List<Map<String, Object>> ret = new ArrayList<>();

						if (field.isDataListItems()) {
							NodeRef listContainerNodeRef = entityListDAO.getListContainer(nodeRef);
							NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, field.getFieldQname());
							if (listNodeRef != null) {
								List<NodeRef> results = entityListDAO.getListItems(listNodeRef, field.getFieldQname());

								for (NodeRef itemNodeRef : results) {
									if (field.getFilter() == null
											|| attributeExtractorService.matchCriteria(itemNodeRef, field.getFilter().getCriteriaMap())) {
										addExtracted(itemNodeRef, field, ret);
									}
								}
							}
						} else if (field.isEntityField()) {
							NodeRef entityNodeRef = entityListDAO.getEntity(nodeRef);
							addExtracted(entityNodeRef, field, ret);

						} else {

							if (field.getFieldDef() instanceof AssociationDefinition) {
								List<NodeRef> assocRefs;
								if (((AssociationDefinition) field.getFieldDef()).isChild()) {
									assocRefs = associationService.getChildAssocs(nodeRef, field.getFieldDef().getName());
								} else {
									assocRefs = associationService.getTargetAssocs(nodeRef, field.getFieldDef().getName());
								}
								for (NodeRef itemNodeRef : assocRefs) {
									addExtracted(itemNodeRef, field, ret);
								}

							} else if (field.getFieldDef() instanceof PropertyDefinition
									&& DataTypeDefinition.NODE_REF.equals(((PropertyDefinition) field.getFieldDef()).getDataType().getName())) {

								Object value = properties.get(field.getFieldDef().getName());
								if (value != null) {
									if (!((PropertyDefinition) field.getFieldDef()).isMultiValued()) {

										addExtracted((NodeRef) value, field, ret);
									} else {
										@SuppressWarnings("unchecked")
										List<NodeRef> values = (List<NodeRef>) value;
										for (NodeRef tempValue : values) {
											addExtracted(tempValue, field, ret);
										}

									}
								}

							}
						}

						return ret;
					}

					private void addExtracted(NodeRef itemNodeRef, AttributeExtractorStructure field, List<Map<String, Object>> ret) {
						if (cache != null && cache.containsKey(itemNodeRef)) {
							ret.add(cache.get(itemNodeRef));
						} else {
							if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
								QName itemType = nodeService.getType(itemNodeRef);
								Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
								Map<String, Object> extracted = doExtract(itemNodeRef, itemType, field.getChildrens(), properties, cache);
								if (cache != null) {
									cache.put(itemNodeRef, extracted);
								}
								ret.add(extracted);
							}
						}
					}

				});
		if (cache != null) {
			cache.put(nodeRef, result);
		}
		return result;
	}

	public class SimpleRepositoryEntity extends BeCPGDataObject {

		private static final long serialVersionUID = -8814912431022547846L;

		public SimpleRepositoryEntity(NodeRef nodeRef) {
			this.nodeRef = nodeRef;
		}

	}

	public class FormulaContext extends DataListItemSpelContext<RepositoryEntity> {
		private Map<String, Object> props;

		FormulaContext(SpelFormulaService spelFormulaService, NodeRef itemNodeRef, Map<String, Object> props) {
			super(spelFormulaService);
			this.setDataListItem(new SimpleRepositoryEntity(itemNodeRef));
			this.props = props;
		}

		public Map<String, Object> getProps() {
			return props;
		}

		public void setProps(Map<String, Object> props) {
			this.props = props;
		}
	}

	/**
	 * <p>eval.</p>
	 *
	 * @param formula a {@link java.lang.String} object.
	 * @param values a {@link java.util.Map} object.
	 * @return a {@link java.lang.Object} object.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param itemNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	protected Object eval(NodeRef entityNodeRef, NodeRef itemNodeRef, String formula, Map<String, Object> values) {

		if (formula.startsWith("dyn_")) {
			return values.get(formula);
		}


		EvaluationContext context = spelFormulaService.createCustomSpelContext(new SimpleRepositoryEntity(entityNodeRef),
				new FormulaContext(spelFormulaService, itemNodeRef, values), false);

		Expression exp = spelFormulaService.parseExpression(SpelHelper.formatFormula(formula));

		return exp.getValue(context);

	}

	/** {@inheritDoc} */
	@Override
	public boolean isDefault() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(QName itemType, String[] parameters) {
		return false;
	}

}
