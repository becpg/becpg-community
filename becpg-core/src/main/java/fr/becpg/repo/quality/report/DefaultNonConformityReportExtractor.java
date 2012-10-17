package fr.becpg.repo.quality.report;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Element;

import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.BeCPGDao;
import fr.becpg.repo.quality.data.NonConformityData;
import fr.becpg.repo.quality.data.dataList.WorkLogDataItem;
import fr.becpg.repo.report.entity.impl.AbstractEntityReportExtractor;

public class DefaultNonConformityReportExtractor extends AbstractEntityReportExtractor {

	protected static final String TAG_WORKLOG = "workLog";
	protected static final String TAG_WORK = "work";

	private BeCPGDao<NonConformityData> nonConformityDAO;

	public void setNonConformityDAO(BeCPGDao<NonConformityData> nonConformityDAO) {
		this.nonConformityDAO = nonConformityDAO;
	}

	/**
	 * load the datalists of the product data.
	 * 
	 * @param productData
	 *            the product data
	 * @param dataListsElt
	 *            the data lists elt
	 * @return the element
	 */
	@Override
	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt) {

		PropertyFormats propertyFormats = new PropertyFormats(true);
		NonConformityData ncData = nonConformityDAO.find(entityNodeRef);

		// workLog
		if (ncData.getWorkLog() != null) {
			Element workLogElt = dataListsElt.addElement(TAG_WORKLOG);

			for (WorkLogDataItem dataItem : ncData.getWorkLog()) {

				Element workElt = workLogElt.addElement(TAG_WORK);
				workElt.addAttribute(QualityModel.PROP_WL_STATE.getLocalName(), dataItem.getState());
				workElt.addAttribute(QualityModel.PROP_WL_COMMENT.getLocalName(), dataItem.getComment());
				workElt.addAttribute(ContentModel.PROP_CREATOR.getLocalName(), dataItem.getCreator());
				workElt.addAttribute(ContentModel.PROP_CREATED.getLocalName(), attributeExtractorService
						.getStringValue(dictionaryService.getProperty(ContentModel.PROP_CREATED),
								dataItem.getCreated(), propertyFormats));
			}
		}
	}
}
