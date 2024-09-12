package fr.becpg.repo.product.report;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.collection.data.ProductCollectionData;
import fr.becpg.repo.collection.data.list.ProductListDataItem;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.product.helper.AllocationHelper;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>AllocationExcelReportSearchPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class AllocationExcelReportSearchPlugin extends DynamicCharactExcelReportSearchPlugin {


	@Autowired
	AlfrescoRepository<BeCPGDataObject> alfrescoRepository;

	/** {@inheritDoc} */
	@Override
	public boolean isDefault() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(QName itemType, String[] parameters) {
		String parameter = (parameters != null) && (parameters.length > 0) ? parameters[0] : null;

		return ((parameter != null) && !parameter.isEmpty() && parameter.contains("Allocation"));
	}

	/** {@inheritDoc} */
	@Override
	public int fillSheet(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum, String[] parameters,
			AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache) {

		ExcelCellStyles excelCellStyles = new ExcelCellStyles(sheet.getWorkbook());
		
		for (NodeRef entityNodeRef : searchResults) {
			QName entityType = nodeService.getType(entityNodeRef);
			if (mainType.equals(entityType) || entityDictionaryService.isSubClass(entityType, mainType)) {

				Serializable key = nodeService.getProperty(entityNodeRef, keyColumn.getFieldDef().getName());
				if (key == null) {
					key = nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CODE);
				}
				if (key == null) {
					key = nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
				}

				Map<String, Object> entityItems = getEntityProperties(entityNodeRef, mainType, metadataFields, cache);

				entityItems.putAll(getDynamicProperties(entityNodeRef, itemType));

				rownum  = extractAllocations(entityNodeRef, sheet, metadataFields, cache, rownum, key, entityItems,excelCellStyles);

			}
		}

		return rownum;
	}

	private int extractAllocations(NodeRef productNodeRef, XSSFSheet sheet, List<AttributeExtractorStructure> metadataFields,
			Map<NodeRef, Map<String, Object>> cache, int rownum, Serializable key, Map<String, Object> entityItems, ExcelCellStyles excelCellStyles) {

		if (permissionService.hasPermission(productNodeRef, "Read") == AccessStatus.ALLOWED) {

			QName entityType = nodeService.getType(productNodeRef);

			if (PLMModel.TYPE_PRODUCTCOLLECTION.equals(entityType)) {
				ProductCollectionData productCollectionData = (ProductCollectionData) alfrescoRepository.findOne(productNodeRef);
				for (ProductListDataItem product : productCollectionData.getProductList()) {
					rownum = extractAllocations(product.getProduct(), sheet, metadataFields, cache, rownum, key, entityItems,excelCellStyles);
				}

			} else {

				ProductData productData = (ProductData) alfrescoRepository.findOne(productNodeRef);
				
				if(productData.isRawMaterial()) {
					
					Map<QName, Serializable> properties = nodeService.getProperties(productNodeRef);
					Map<String, Object> item = doExtract(productNodeRef, nodeService.getType(productNodeRef), metadataFields, properties, cache);

					if (entityItems != null) {
						item.putAll(entityItems);
					}

					for (AttributeExtractorStructure metadataField : metadataFields) {
						if (metadataField.isFormulaField()) {
							if (metadataField.getFieldName().startsWith("formula") || metadataField.getFieldName().startsWith("image")) {
								item.put(metadataField.getFieldName(), eval(productNodeRef , productNodeRef, metadataField.getFormula(), item));
							} else {
								item.put(metadataField.getFieldName(), metadataField.getFormula());
							}
						}

					}

					Row row = sheet.createRow(rownum++);

					int cellNum = 0;
					Cell cell = row.createCell(cellNum++);
					cell.setCellValue("VALUES");

					if (key != null) {
						cell = row.createCell(cellNum++);
						cell.setCellValue(String.valueOf(key));
					}

					ExcelHelper.appendExcelField(metadataFields, null, item, sheet, row, cellNum, rownum, null,excelCellStyles);
					
					
				} else if(!productData.isLocalSemiFinished()) {

					Double productNetWeight = FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT);
	
					Map<NodeRef, Double> rawMaterials = new HashMap<>();
					rawMaterials = AllocationHelper.extractAllocations(productData, rawMaterials, productNetWeight, alfrescoRepository);
					Double totalQty = 0d;
					for (Double qty : rawMaterials.values()) {
						totalQty += qty;
					}
	
					// sort
					List<Map.Entry<NodeRef, Double>> sortedRawMaterials = new LinkedList<>(rawMaterials.entrySet());
					Collections.sort(sortedRawMaterials, (r1, r2) -> r2.getValue().compareTo(r1.getValue()));
	
					Map<QName, Serializable> productProps = nodeService.getProperties(productNodeRef);
					Map<String, Object> productItems = doExtract(productNodeRef, entityType, metadataFields, productProps, cache);
					productItems = productItems.entrySet().stream().filter(map -> map.getKey().contains("product_") && (map.getValue() != null))
							.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	
					// render
	
					for (Map.Entry<NodeRef, Double> entry : sortedRawMaterials) {
						Map<QName, Serializable> properties = nodeService.getProperties(entry.getKey());
						Map<String, Object> item = doExtract(entry.getKey(), nodeService.getType(entry.getKey()), metadataFields, properties, cache);
	
						if (entityItems != null) {
							item.putAll(entityItems);
						}
	
						if (productItems != null) {
							item.putAll(productItems);
						}
	
						item.put("qty", entry.getValue());
						item.put("qtyPerc", (100 * entry.getValue()) / (totalQty != 0d ? totalQty : 1d));
						item.put("qtyForProduct",
								(100 * entry.getValue()) / FormulationHelper.getNetWeight(productData, FormulationHelper.DEFAULT_NET_WEIGHT));
	
						for (AttributeExtractorStructure metadataField : metadataFields) {
							if (metadataField.isFormulaField()) {
								if (metadataField.getFieldName().startsWith("formula") || metadataField.getFieldName().startsWith("image")) {
									item.put(metadataField.getFieldName(), eval(productNodeRef, entry.getKey(), metadataField.getFormula(), item));
								} else {
									item.put(metadataField.getFieldName(), metadataField.getFormula());
								}
							}
	
						}
	
						Row row = sheet.createRow(rownum++);
	
						int cellNum = 0;
						Cell cell = row.createCell(cellNum++);
						cell.setCellValue("VALUES");
	
						if (key != null) {
							cell = row.createCell(cellNum++);
							cell.setCellValue(String.valueOf(key));
						}
	
						ExcelHelper.appendExcelField(metadataFields, null, item, sheet, row, cellNum, rownum, null,excelCellStyles);
						
					}
				
				}
			}
			
	

		}
		return rownum;
	}

}
