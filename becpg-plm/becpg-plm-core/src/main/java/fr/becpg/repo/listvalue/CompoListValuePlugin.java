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
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;

@Service
public class CompoListValuePlugin extends EntityListValuePlugin {

	private static final Log logger = LogFactory.getLog(CompoListValuePlugin.class);

	private static final String SOURCE_TYPE_COMPOLIST_PARENT_LEVEL = "compoListParentLevel";

	@Autowired
	private MultiLevelDataListService multiLevelDataListService;

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_COMPOLIST_PARENT_LEVEL };
	}

	public void setMultiLevelDataListService(MultiLevelDataListService multiLevelDataListService) {
		this.multiLevelDataListService = multiLevelDataListService;
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		NodeRef entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_NODEREF));
		logger.debug("CompoListValuePlugin sourceType: " + sourceType + " - entityNodeRef: " + entityNodeRef);

		if (sourceType.equals(SOURCE_TYPE_COMPOLIST_PARENT_LEVEL)) {

			DataListFilter dataListFilter = new DataListFilter();
			dataListFilter.setDataType(PLMModel.TYPE_COMPOLIST);
			dataListFilter.setEntityNodeRefs(Collections.singletonList(entityNodeRef));

			// need to load assoc so we use the MultiLevelDataListService
			MultiLevelListData mlld = multiLevelDataListService.getMultiLevelListData(dataListFilter);

			NodeRef itemId = null;
			@SuppressWarnings("unchecked")
			Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
			if (extras != null) {
				if (extras.get("itemId") != null) {
					itemId = new NodeRef(extras.get("itemId"));
				}
			}

			List<ListValueEntry> result = getParentsLevel(mlld, query, itemId);

			return new ListValuePage(result, pageNum, pageSize, null);
		}
		return null;
	}

	private List<ListValueEntry> getParentsLevel(MultiLevelListData mlld, String query, NodeRef itemId) {

		List<ListValueEntry> result = new ArrayList<>();

		if (mlld != null) {

			for (Map.Entry<NodeRef, MultiLevelListData> kv : mlld.getTree().entrySet()) {

				NodeRef productNodeRef = kv.getValue().getEntityNodeRef();
				if (productNodeRef != null) {
					logger.debug("productNodeRef: " + productNodeRef);

					// avoid cycle: when editing an item, cannot select itself
					// as
					// parent
					if ((itemId != null) && itemId.equals(kv.getKey())) {
						continue;
					}

					if (nodeService.getType(productNodeRef).isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)) {

						boolean addNode = false;
						String productName = (String) nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME);
						logger.debug("productName: " + productName + " - query: " + query);

						if (!query.isEmpty()) {

							if (productName != null) {
								if (isQueryMatch(query, productName)) {
									addNode = true;
								}
							}
						} else {
							addNode = true;
						}

						if (addNode) {
							logger.debug("add node productName: " + productName);
							String state = (String) nodeService.getProperty(productNodeRef, PLMModel.PROP_PRODUCT_STATE);
							result.add(new ListValueEntry(kv.getKey().toString(), productName,
									PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.getLocalName() + "-" + state));
						}
					}

					if (kv.getValue() != null) {
						result.addAll(getParentsLevel(kv.getValue(), query, itemId));
					}
				}
			}
		}

		return result;
	}

}
