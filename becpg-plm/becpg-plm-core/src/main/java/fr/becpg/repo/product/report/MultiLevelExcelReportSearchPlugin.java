package fr.becpg.repo.product.report;

import java.io.Serializable;
import java.util.*;
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
import fr.becpg.repo.report.search.impl.DefaultExcelReportSearchPlugin;

@Service
public class MultiLevelExcelReportSearchPlugin extends DefaultExcelReportSearchPlugin {

	@Autowired
	MultiLevelDataListService multiLevelDataListService;
	
	@Override
	public boolean isDefault() {
		return false;
	}
	
	
	@Override
	public boolean isApplicable(QName itemType) {
		return PLMModel.TYPE_PACKAGINGLIST.equals(itemType);
	}
	
	
	@Override
	public void fillSheet(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum,
			AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache) {

		for (NodeRef entityNodeRef : searchResults) {
			if (mainType.equals(nodeService.getType(entityNodeRef))) {
				    Serializable key = nodeService.getProperty(entityNodeRef, keyColumn.getFieldDef().getName());
				    if(key==null){
				    	key = nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_CODE);
				    }
				    if(key==null){
				    	key = nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
				    }

					DataListFilter dataListFilter = new DataListFilter();
					dataListFilter.setDataType(itemType);
					Map<String, String> criteriaMap = new HashMap<>();
					criteriaMap.put(DataListFilter.PROP_DEPTH_LEVEL, "All");
					dataListFilter.setCriteriaMap(criteriaMap);
					dataListFilter.setEntityNodeRefs(Collections.singletonList(entityNodeRef));
					
					MultiLevelListData listData = multiLevelDataListService.getMultiLevelListData(dataListFilter);
					
					rownum = appendNextLevel(listData, sheet,  itemType, metadataFields, cache, rownum, key, null);

			}
		}
	}
	
	
	protected int appendNextLevel(MultiLevelListData listData, XSSFSheet sheet, QName itemType,
			List<AttributeExtractorStructure> metadataFields,  Map<NodeRef, Map<String, Object>> cache, int rownum, Serializable key, Double parentQty) {

		for (Entry<NodeRef, MultiLevelListData> entry : listData.getTree().entrySet()) {
			NodeRef itemNodeRef = entry.getKey();
			if (itemType.equals(nodeService.getType(itemNodeRef))) {
				if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
					
					Map<QName, Serializable> properties = nodeService.getProperties(itemNodeRef);
					Map<String, Object> item = doExtract(itemNodeRef, itemType, metadataFields, properties, cache);
					
					item.put("prop_bcpg_depthLevel", entry.getValue().getDepth());
					item.put("prop_bcpg_parent", nodeService.getProperty(listData.getEntityNodeRef(), BeCPGModel.PROP_CODE));
					
					
				
					Double qty = (Double) item.get("prop_bcpg_packagingListQty");
					
					if(qty!=null  && PLMModel.TYPE_PACKAGINGKIT.equals(nodeService.getType(listData.getEntityNodeRef()))
							&& PackagingLevel.Tertiary.toString().equals(nodeService.getProperty(itemNodeRef, PLMModel.PROP_PACKAGINGLIST_PKG_LEVEL))){
						Integer nbByPalet = (Integer) nodeService.getProperty(listData.getEntityNodeRef(), PackModel.PROP_PALLET_BOXES_PER_PALLET);
						if(nbByPalet!=null && nbByPalet>0){
							qty = qty/(nbByPalet*1d);
						}
					}
					
					if(qty!=null && parentQty!=null){
						qty = qty / parentQty;
						item.put("prop_bcpg_packagingListQty",qty); 
					}
					
					
					Row row = sheet.createRow(rownum++);

					int cellNum = 0;
					Cell cell = row.createCell(cellNum++);
					cell.setCellValue("VALUES");

					if (key != null) {
						cell = row.createCell(cellNum++);
						cell.setCellValue(String.valueOf(key));
					}

					cellNum = ExcelHelper.appendExcelField(metadataFields, null, item, row, cellNum);

//					Row headerRow = sheet.getRow(1);
//					if(cellNum < headerRow.getLastCellNum()){
//						for (int i = cellNum; i < headerRow.getLastCellNum(); i++) {
//							if(headerRow.getCell(i).getCellType() == Cell.CELL_TYPE_FORMULA){
//								 cell = row.createCell(i);
//								 cell.setCellFormula(headerRow.getCell(i).getCellFormula());
//							}
//						}
//					}

					rownum = appendNextLevel(entry.getValue(),sheet, itemType,  metadataFields, cache,rownum, key, qty);
					
				}
			}
					
			
		}
		return rownum;
	}
	
	
	
	
}
