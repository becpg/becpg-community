package fr.becpg.repo.report.entity.impl;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import fr.becpg.repo.report.entity.EntityReportData;

/**
 * <p>NoXmlEntityReportExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class NoXmlEntityReportExtractor extends DefaultEntityReportExtractor{

	/** {@inheritDoc} */
	@Override
	public EntityReportData extract(NodeRef entityNodeRef, Map<String, String> preferences) {
		
		

		DefaultExtractorContext context = new DefaultExtractorContext(preferences);

		
		Document document = DocumentHelper.createDocument();
		Element entityElt = document.addElement(TAG_ENTITY);

		Element imgsElt = entityElt.addElement(TAG_IMAGES);
		extractEntityImages(entityNodeRef, imgsElt, context, null);

		context.getReportData().setXmlDataSource(entityElt);

		return context.getReportData();
	}
	
	
	/** {@inheritDoc} */
	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return EntityReportExtractorPriority.NONE;
	}
}
