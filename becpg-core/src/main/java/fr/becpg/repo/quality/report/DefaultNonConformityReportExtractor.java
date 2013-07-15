package fr.becpg.repo.quality.report;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.dom4j.Element;

import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.quality.data.NonConformityData;
import fr.becpg.repo.quality.data.dataList.WorkLogDataItem;
import fr.becpg.repo.report.entity.impl.AbstractEntityReportExtractor;
import fr.becpg.repo.repository.AlfrescoRepository;

public class DefaultNonConformityReportExtractor extends AbstractEntityReportExtractor {

	protected static final String TAG_WORKLOG = "workLog";
	protected static final String TAG_WORK = "work";

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
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
	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, Map<String, byte[]> images) {

		PropertyFormats propertyFormats = new PropertyFormats(true);
		NonConformityData ncData = (NonConformityData) alfrescoRepository.findOne(entityNodeRef);

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
