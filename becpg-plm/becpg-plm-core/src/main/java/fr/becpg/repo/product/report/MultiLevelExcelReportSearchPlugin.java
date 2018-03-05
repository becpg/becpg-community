package fr.becpg.repo.product.report;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import fr.becpg.model.PackModel;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.helper.ExcelHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.formulation.FormulationHelper;
import fr.becpg.repo.report.search.impl.DefaultExcelReportSearchPlugin;

@Service
public class MultiLevelExcelReportSearchPlugin extends DefaultExcelReportSearchPlugin {

	// Allowed Parameter _AllLevel _MaxLevel2 _OnlyLevel2

	@Autowired
	MultiLevelDataListService multiLevelDataListService;

	@Override
	public boolean isDefault() {
		return false;
	}

	@Override
	public boolean isApplicable(QName itemType, String parameter) {
		return PLMModel.TYPE_PACKAGINGLIST.equals(itemType) || ((parameter != null) && !parameter.isEmpty() && parameter.contains("Level"));
	}

	@Override
	public void fillSheet(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum, String parameter,
			AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache) {

	
		String depthLevel = parameter != null ? parameter.replaceAll("Level", "").replaceAll("Max", "").replaceAll("Only", "") : "All";

		for (NodeRef entityNodeRef : searchResults) {
			if (mainType.equals(nodeService.getType(entityNodeRef))) {
				Serializable key = nodeService.getProperty(entityNodeRef, keyColumn.getFieldDef().getName());
				if (key == null) {
					key = nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CODE);
				}
				if (key == null) {
					key = nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
				}

				DataListFilter dataListFilter = new DataListFilter();
				dataListFilter.setDataType(itemType);
				Map<String, String> criteriaMap = new HashMap<>();
				criteriaMap.put(DataListFilter.PROP_DEPTH_LEVEL, depthLevel);
				dataListFilter.setCriteriaMap(criteriaMap);
				dataListFilter.setEntityNodeRefs(Collections.singletonList(entityNodeRef));

				
				MultiLevelListData listData = multiLevelDataListService.getMultiLevelListData(dataListFilter);
				

				Map<String, Object> entityItems = getEntityProperties(entityNodeRef, mainType, metadataFields, cache);
				
				rownum = appendNextLevel(listData, sheet, itemType, metadataFields, cache, rownum, key, null, parameter, entityItems);

			}
		}
	}

	protected int appendNextLevel(MultiLevelListData listData, XSSFSheet sheet, QName itemType, List<AttributeExtractorStructure> metadataFields,
			Map<NodeRef, Map<String, Object>> cache, int rownum, Serializable key, Double parentQty, String parameter, Map<String, Object> entityItems) {

		for (Entry<NodeRef, MultiLevelListData> entry : listData.getTree().entrySet()) {
			NodeRef itemNodeRef = entry.getKey();
			if (itemType.equals(nodeService.getType(itemNodeRef))) {
				if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {

					Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
					Map<String, Object> item = doExtract(itemNodeRef, itemType, metadataFields, properties, cache);

					if(entityItems != null){
						item.putAll(entityItems);
					}
					
					item.put("prop_bcpg_depthLevel", entry.getValue().getDepth());
					item.put("prop_bcpg_parent", nodeService.getProperty(listData.getEntityNodeRef(), BeCPGModel.PROP_CODE));

					
					Double qty = null;

					if (PLMModel.TYPE_PACKAGINGLIST.equals(itemType)) {
						qty = (Double) item.get("prop_bcpg_packagingListQty");

						if ((qty != null) && PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(listData.getEntityNodeRef()))
								&& PackagingLevel.Tertiary.toString()
										.equals(nodeService.getProperty(itemNodeRef, PLMModel.PROP_PACKAGINGLIST_PKG_LEVEL))) {
							Integer nbByPalet = (Integer) nodeService.getProperty(listData.getEntityNodeRef(),
									PackModel.PROP_PALLET_BOXES_PER_PALLET);
							if ((nbByPalet != null) && (nbByPalet > 0)) {
								qty = qty / (nbByPalet * 1d);
							}
						}

						if ((qty != null) && (parentQty != null)) {
							qty = qty / parentQty;
							item.put("prop_bcpg_packagingListQty", qty);
						}

					} else if (PLMModel.TYPE_COMPOLIST.equals(itemType)) {
						qty = (Double) item.get("prop_bcpg_compoListQty");
						if((qty != null) && (parentQty != null)) {
							
							Double parentNetWeight = FormulationHelper.getNetWeight(listData.getEntityNodeRef(), nodeService, FormulationHelper.DEFAULT_NET_WEIGHT) ;
							
							if ( parentNetWeight!=null && parentNetWeight!=0d) {
								qty = parentQty * qty / parentNetWeight;
								item.put("prop_bcpg_compoListQty", qty);
							} else {
								qty = 0d;
							}
						}

					}
				

					if ((parameter == null) || !parameter.contains("OnlyLevel") || parameter.equals("OnlyLevel" + entry.getValue().getDepth())) {
						Row row = sheet.createRow(rownum++);
						
						int cellNum = 0;
						Cell cell = row.createCell(cellNum++);
						cell.setCellValue("VALUES");

						if (key != null) {
							cell = row.createCell(cellNum++);
							cell.setCellValue(String.valueOf(key));
						}

						cellNum = ExcelHelper.appendExcelField(metadataFields, null, item, sheet.getWorkbook(), row, cellNum, null);

					}
					rownum = appendNextLevel(entry.getValue(), sheet, itemType, metadataFields, cache, rownum, key, qty, parameter, entityItems);

				}
			}

		}
		return rownum;
	}

}
