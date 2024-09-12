package fr.becpg.repo.product.report;

import java.io.Serializable;
import java.util.ArrayList;
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
 * <p>DynamicCharactListExcelReportSearchPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class DynamicCharactListExcelReportSearchPlugin extends DefaultExcelReportSearchPlugin {

	/** {@inheritDoc} */
	@Override
	public int fillSheet(XSSFSheet sheet, List<NodeRef> searchResults, QName mainType, QName itemType, int rownum, String[] parameters,
			AttributeExtractorStructure keyColumn, List<AttributeExtractorStructure> metadataFields, Map<NodeRef, Map<String, Object>> cache) {

		ExcelCellStyles excelCellStyles = new ExcelCellStyles(sheet.getWorkbook());

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

					Map<String, Object> entityItems = getEntityProperties(entityNodeRef, mainType, metadataFields, cache);

					NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
					List<NodeRef> listNodeRefs = new ArrayList<>();
					List<NodeRef> results = new ArrayList<>();

					listNodeRefs.add(entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COMPOLIST));
					listNodeRefs.add(entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_PACKAGINGLIST));
					listNodeRefs.add(entityListDAO.getList(listContainerNodeRef, MPMModel.TYPE_PROCESSLIST));

					listNodeRefs.forEach((nodeRef) -> {
						if (nodeRef != null) {
							results.addAll(entityListDAO.getListItems(nodeRef, PLMModel.TYPE_DYNAMICCHARACTLIST));
						}
					});

					for (NodeRef itemNodeRef : results) {
						if (itemType.equals(nodeService.getType(itemNodeRef))) {
							if (permissionService.hasPermission(itemNodeRef, "Read") == AccessStatus.ALLOWED) {
								rownum = fillRow(sheet, entityNodeRef, itemNodeRef, itemType, metadataFields, cache, rownum, key, entityItems,
										excelCellStyles);
							}
						}
					}

				} else {
					rownum = fillRow(sheet, entityNodeRef, entityNodeRef, itemType, metadataFields, cache, rownum, null, null, excelCellStyles);
				}
			}
		}

		return rownum;

	}

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(QName itemType, String[] parameters) {
		return PLMModel.TYPE_DYNAMICCHARACTLIST.equals(itemType);
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDefault() {
		return false;
	}

}
