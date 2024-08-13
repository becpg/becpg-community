package fr.becpg.repo.product.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.report.search.impl.DefaultExcelReportSearchPlugin;

/**
 * <p>DynamicCharactExcelReportSearchPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class DynamicCharactExcelReportSearchPlugin extends DefaultExcelReportSearchPlugin {

	/** {@inheritDoc} */
	@Override
	public int fillSheet(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum, String[] parameters,
			AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache) {

		ExcelCellStyles excelCellStyles = new ExcelCellStyles(sheet.getWorkbook());
		
		boolean addDynCharact = false;
		for (AttributeExtractorStructure field : metadataFields) {
			if (field.getFieldName().startsWith("dyn_")) {
				addDynCharact = true;
				break;
			}
		}

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

						if (addDynCharact) {
							entityItems.putAll(getDynamicProperties(entityNodeRef, itemType));
						}

						List<NodeRef> results = entityListDAO.getListItems(listNodeRef, itemType);
						for (NodeRef itemNodeRef : results) {
							if (itemType.equals(nodeService.getType(itemNodeRef))) {
								if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
									rownum = fillRow(sheet, entityNodeRef, itemNodeRef, itemType, metadataFields, cache, rownum, key, entityItems,excelCellStyles);
								}
							}
						}
					}
				} else {
					Map<String, Object> entityItems = new HashMap<>();
					if (addDynCharact) {
						entityItems.putAll(getDynamicProperties(entityNodeRef, null));
					}

					rownum = fillRow(sheet, entityNodeRef, entityNodeRef, itemType, metadataFields, cache, rownum, null, entityItems,excelCellStyles);
				}
			}
		}

		return rownum;
		
	}

	Map<String, Object> getDynamicProperties(NodeRef entityNodeRef, QName listType) {
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		List<NodeRef> listNodeRefs = new ArrayList<>();
		List<NodeRef> results = new ArrayList<>();

		if (((listType != null) && PLMModel.TYPE_COMPOLIST.equals(listType)) || PLMModel.TYPE_PACKAGINGLIST.equals(listType)
				|| MPMModel.TYPE_PROCESSLIST.equals(listType)) {
			listNodeRefs.add(entityListDAO.getList(listContainerNodeRef, listType));
		} else {
			listNodeRefs.add(entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COMPOLIST));
			listNodeRefs.add(entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_PACKAGINGLIST));
			listNodeRefs.add(entityListDAO.getList(listContainerNodeRef, MPMModel.TYPE_PROCESSLIST));
		}

		listNodeRefs.forEach((nodeRef) -> {
			if (nodeRef != null) {
				results.addAll(entityListDAO.getListItems(nodeRef, PLMModel.TYPE_DYNAMICCHARACTLIST));
			}
		});
		Map<String, Object> item = new HashMap<>();
		results.forEach(nodeRef -> {
			Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
			if ((properties.get(PLMModel.PROP_DYNAMICCHARACT_TITLE) != null) && (properties.get(PLMModel.PROP_DYNAMICCHARACT_VALUE) != null)) {
				item.put("dyn_" + ((String) properties.get(PLMModel.PROP_DYNAMICCHARACT_TITLE)).replace(" ", "_"),
						properties.get(PLMModel.PROP_DYNAMICCHARACT_VALUE));
			}

		});
		return item;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(QName itemType, String[] parameters) {
		String parameter = (parameters != null) && (parameters.length > 0) ? parameters[0] : null;
		return (entityDictionaryService.isSubClass(itemType, PLMModel.TYPE_PRODUCT) || PLMModel.TYPE_COMPOLIST.equals(itemType)
				|| MPMModel.TYPE_PROCESSLIST.equals(itemType)) && ((parameter == null) || (!parameter.contains("Level") && !parameter.contains("Allocation"))) ;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDefault() {
		return false;
	}

}
