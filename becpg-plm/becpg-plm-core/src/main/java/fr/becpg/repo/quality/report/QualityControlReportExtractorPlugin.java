/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.quality.report;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.quality.data.QualityControlData;
import fr.becpg.repo.report.entity.EntityReportExtractorPlugin;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@Service
public class QualityControlReportExtractorPlugin extends DefaultEntityReportExtractor {

	@Autowired
	private EntityReportService entityReportService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

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
		QualityControlData qualityControlData = (QualityControlData) alfrescoRepository.findOne(entityNodeRef);

		Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(qualityControlData);

		if ((datalists != null) && !datalists.isEmpty()) {

			for (QName dataListQName : datalists.keySet()) {

				if (alfrescoRepository.hasDataList(entityNodeRef, dataListQName)) {
					Element dataListElt = dataListsElt.addElement(dataListQName.getLocalName() + "s");

					@SuppressWarnings({ "unchecked" })
					List<BeCPGDataObject> dataListItems = (List<BeCPGDataObject>) datalists.get(dataListQName);

					for (BeCPGDataObject dataListItem : dataListItems) {
						Element nodeElt = dataListElt.addElement(dataListQName.getLocalName());
						loadDataListItemAttributes(dataListItem, nodeElt, images);

					}
				}
			}
		}

	}

	@Override
	protected boolean loadTargetAssoc(NodeRef entityNodeRef, AssociationDefinition assocDef, Element entityElt, Map<String, byte[]> images) {

		if ((assocDef != null) && (assocDef.getName() != null)) {
			if (assocDef.getName().equals(QualityModel.ASSOC_PRODUCT)) {
				List<NodeRef> nodeRefs = associationService.getTargetAssocs(entityNodeRef, assocDef.getName());
				for (NodeRef nodeRef : nodeRefs) {
					QName qName = nodeService.getType(nodeRef);
					Element pjtEntityElt = entityElt.addElement("product");
					Element nodeElt = pjtEntityElt.addElement(qName.getLocalName());
					EntityReportExtractorPlugin extractor = entityReportService.retrieveExtractor(nodeRef);
					if ((extractor != null) && (extractor instanceof DefaultEntityReportExtractor)) {
						((DefaultEntityReportExtractor) extractor).extractEntity(nodeRef, nodeElt, images);
					} else {
						extractEntity(nodeRef, nodeElt, images);
					}
				}
				return true;
			}
		}

		return false;
	}

	@Override
	protected boolean isMultiLinesAttribute(QName attribute) {
		return false;
	}

	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return QualityModel.TYPE_QUALITY_CONTROL.equals(type) ? EntityReportExtractorPriority.NORMAL : EntityReportExtractorPriority.NONE;
	}

}
