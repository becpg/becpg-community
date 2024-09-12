/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.dom4j.Element;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.product.data.CurrentLevelQuantities;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.report.ProductReportExtractorPlugin;
import fr.becpg.repo.quality.data.BatchData;
import fr.becpg.repo.report.entity.impl.DefaultExtractorContext;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>BatchReportExtractorPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class BatchReportExtractorPlugin extends ProductReportExtractorPlugin {

	/** Constant <code>DATALIST_SPECIFIC_EXTRACTOR</code> */
	protected static final List<QName> DATALIST_SPECIFIC_EXTRACTOR = Arrays.asList(PLMModel.TYPE_COMPOLIST, PLMModel.TYPE_REQCTRLLIST);

	/**
	 * {@inheritDoc}
	 *
	 * load the datalists of the product data.
	 */
	@Override
	protected void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, DefaultExtractorContext context) {
		loadDataLists(entityNodeRef, dataListsElt, context, true);
	}

	private void loadDataLists(NodeRef entityNodeRef, Element dataListsElt, DefaultExtractorContext context, boolean isExtractedProduct) {
		BatchData batchData = (BatchData) alfrescoRepository.findOne(entityNodeRef);

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listContainerNodeRef != null) {

			List<NodeRef> listNodeRefs = entityListDAO.getExistingListsNodeRef(listContainerNodeRef);
			Map<QName, List<? extends RepositoryEntity>> datalists = repositoryEntityDefReader.getDataLists(batchData);

			for (NodeRef listNodeRef : listNodeRefs) {
				QName dataListQName = QName.createQName((String) nodeService.getProperty(listNodeRef, DataListModel.PROP_DATALISTITEMTYPE),
						namespaceService);

				if (!DATALIST_SPECIFIC_EXTRACTOR.contains(dataListQName)) {

					if ((datalists != null) && datalists.containsKey(dataListQName)) {
						// use entityRepository for performances
						@SuppressWarnings({ "rawtypes", "unchecked" })
						List<BeCPGDataObject> dataListItems = (List) datalists.get(dataListQName);

						if ((dataListItems != null) && !dataListItems.isEmpty()) {

							Element dataListElt = dataListsElt.addElement(dataListQName.getLocalName() + "s");
							addDataListStateAndName(dataListElt, listNodeRef);

							for (BeCPGDataObject dataListItem : dataListItems) {
								Element nodeElt = dataListElt.addElement(dataListQName.getLocalName());
								loadDataListItemAttributes(dataListItem, nodeElt, context);

							}
						}
					} else if (!BeCPGModel.TYPE_ACTIVITY_LIST.equals(dataListQName)) {
						// extract specific datalists
						loadDataList(dataListsElt, listNodeRef, dataListQName, context);
					}
				}
			}
		}

		if (isExtractedProduct) {
			loadCompoList(batchData, dataListsElt, context);
		}

	}

	private void loadCompoList(BatchData batchData, Element dataListsElt, DefaultExtractorContext context) {
		// compoList
		if (batchData.hasCompoListEl()) {
			Element compoListElt = dataListsElt.addElement(PLMModel.TYPE_COMPOLIST.getLocalName() + "s");
			addDataListStateAndName(compoListElt, batchData.getCompoList().get(0).getParentNodeRef());

			for (CompoListDataItem dataItem : batchData.getCompoList()) {
				if ((dataItem.getProduct() != null) && nodeService.exists(dataItem.getProduct())) {
					loadCompoListItem(batchData.getNodeRef(), null, compoListElt, 1, 
					new CurrentLevelQuantities(alfrescoRepository, packagingHelper, dataItem), context);
				}
			}

			loadDynamicCharactList(batchData.getCompoListView().getDynamicCharactList(), compoListElt);
			loadReqCtrlList(context, batchData.getReqCtrlList(), compoListElt);
		}

	}

	/** {@inheritDoc} */
	@Override
	public EntityReportExtractorPriority getMatchPriority(QName type) {
		return QualityModel.TYPE_BATCH.equals(type) ? EntityReportExtractorPriority.NORMAL : EntityReportExtractorPriority.NONE;
	}

}
