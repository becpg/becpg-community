package fr.becpg.repo.report.search.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
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
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.report.search.SearchReportRenderer;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.report.client.ReportFormat;

@Service
public class ExcelReportSearchRenderer implements SearchReportRenderer {

	private static Log logger = LogFactory.getLog(ExcelReportSearchRenderer.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Override
	public void renderReport(NodeRef tplNodeRef, List<NodeRef> searchResults, ReportFormat reportFormat, OutputStream outputStream) {

		ContentReader reader = contentService.getReader(tplNodeRef, ContentModel.PROP_CONTENT);

		StopWatch watch = null;

		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		try {
			XSSFWorkbook workbook = new XSSFWorkbook(reader.getContentInputStream());

			QName mainType = null;
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				XSSFSheet sheet = workbook.getSheetAt(i);
				mainType = fillSheet(sheet, searchResults, mainType);
			}

			workbook.write(outputStream);

		} catch (ContentIOException | IOException e) {
			logger.error("Error generating xls report", e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.warn("Render excel report executed in  " + watch.getTotalTimeSeconds() + " seconds");
			}
		}

	}

	private QName fillSheet(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType) {
		sheet.setColumnHidden(0, true);
		int rownum = 0;
		Row headerRow = sheet.getRow(rownum++);

		if ("TYPE".equals(headerRow.getCell(0).getStringCellValue())) {

			QName itemType = QName.createQName(headerRow.getCell(1).getStringCellValue(), namespaceService);

			if (logger.isDebugEnabled()) {
				logger.debug("Sheet type : " + itemType.toPrefixString());
			}

			headerRow.setZeroHeight(true);
			headerRow = sheet.getRow(rownum++);
			headerRow.setZeroHeight(true);
			List<AttributeExtractorStructure> metadataFields = extractListStruct(itemType, headerRow);
			AttributeExtractorStructure keyColumn = null;
			if (entityDictionaryService.isSubClass(itemType, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
				keyColumn = metadataFields.remove(0);
				logger.debug("Datalist key column : " + keyColumn.getFieldDef().getName());
			} else {
				mainType = itemType;
			}

			rownum++;

			Map<NodeRef, Map<String, Object>> cache = new HashMap<NodeRef, Map<String, Object>>();

			for (NodeRef entityNodeRef : searchResults) {
				if (mainType.equals(nodeService.getType(entityNodeRef))) {
					if (keyColumn != null) {
						Serializable key = nodeService.getProperty(entityNodeRef, keyColumn.getFieldDef().getName());

						NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
						NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, itemType);
						if (listNodeRef != null) {
							List<NodeRef> results = entityListDAO.getListItems(listNodeRef, itemType);
							for (NodeRef itemNodeRef : results) {
								if (itemType.equals(nodeService.getType(itemNodeRef))) {
									if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
										rownum = fillRow(sheet, itemNodeRef, itemType, metadataFields, cache, rownum, key);
									}
								}
							}
						}
					} else {
						rownum = fillRow(sheet, entityNodeRef, itemType, metadataFields, cache, rownum, null);
					}
				}
			}

		}
		return mainType;

	}

	private int fillRow(XSSFSheet sheet, NodeRef itemNodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			Map<NodeRef, Map<String, Object>> cache, int rownum, Serializable key) {

		Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
		Map<String, Object> item = doExtract(itemNodeRef, itemType, metadataFields, properties, cache);
		Row row = sheet.createRow(rownum++);

		int cellNum = 0;
		Cell cell = row.createCell(cellNum++);
		cell.setCellValue("VALUES");

		if (key != null) {
			cell = row.createCell(cellNum++);
			cell.setCellValue(String.valueOf(key));
		}

		ExcelHelper.appendExcelField(metadataFields, null, item, row, cellNum);
		return rownum;
	}

	private List<AttributeExtractorStructure> extractListStruct(QName itemType, Row headerRow) {

		List<String> metadataFields = new LinkedList<String>();
		String currentNested = "";
		for (int i = 1; i < headerRow.getLastCellNum(); i++) {
			if (headerRow.getCell(i) != null) {
				String cellValue = headerRow.getCell(i).getStringCellValue();
				if (cellValue != null && !cellValue.isEmpty()) {
					if (cellValue.contains("_")) {
						if (!currentNested.isEmpty() && currentNested.startsWith(cellValue.split("_")[0])) {
							currentNested += "|" + cellValue.split("_")[1];
						} else {
							if (!currentNested.isEmpty()) {
								logger.debug("Add nested field : " + currentNested);
								metadataFields.add(currentNested);
							}
							currentNested = cellValue.replace("_", "|");
						}

					} else {
						if (!currentNested.isEmpty()) {
							logger.debug("Add nested field : " + currentNested);
							metadataFields.add(currentNested);
							currentNested = "";
						}
						logger.debug("Add field : " + cellValue);
						metadataFields.add(cellValue);
					}
				}
			}

		}

		return attributeExtractorService.readExtractStructure(itemType, metadataFields);
	}

	@Override
	public boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat) {
		return ReportFormat.XLS.equals(reportFormat)
				&& ((String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME))
						.endsWith(ReportTplService.PARAM_VALUE_XLSREPORT_EXTENSION);
	}

	private Map<String, Object> doExtract(NodeRef nodeRef, QName itemType, List<AttributeExtractorStructure> metadataFields,
			Map<QName, Serializable> properties, final Map<NodeRef, Map<String, Object>> cache) {

		return attributeExtractorService.extractNodeData(nodeRef, itemType, properties, metadataFields, AttributeExtractorMode.XLS,
				new AttributeExtractorService.DataListCallBack() {

					@Override
					public List<Map<String, Object>> extractNestedField(NodeRef nodeRef, AttributeExtractorStructure field) {
						List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
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
								List<NodeRef> assocRefs = null;
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

}
