/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.entity.datalist.impl.MultiLevelExtractor;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;

@Service
public class CompoListValuePlugin extends EntityListValuePlugin {

	private static final Log logger = LogFactory.getLog(CompoListValuePlugin.class);

	private static final String SOURCE_TYPE_COMPOLIST_PARENT_LEVEL = "compoListParentLevel";

	@Autowired
	private MultiLevelDataListService multiLevelDataListService;

	@Autowired
	private PreferenceService preferenceService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private AssociationService associationService;

	@Autowired
	private PermissionService permissionService;

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_COMPOLIST_PARENT_LEVEL };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		NodeRef entityNodeRef = new NodeRef((String) props.get(ListValueService.PROP_NODEREF));
		logger.debug("CompoListValuePlugin sourceType: " + sourceType + " - entityNodeRef: " + entityNodeRef);

		if (sourceType.equals(SOURCE_TYPE_COMPOLIST_PARENT_LEVEL) && (entityNodeRef != null)) {

			boolean multiLevelExtract = true;

			DataListFilter dataListFilter = new DataListFilter();
			dataListFilter.setDataType(PLMModel.TYPE_COMPOLIST);
			dataListFilter.setEntityNodeRefs(Collections.singletonList(entityNodeRef));

			Integer depthLevel = getDepthUserPref(dataListFilter);
			if (depthLevel == 0) {
				multiLevelExtract = false;
			}

			NodeRef itemId = null;
			@SuppressWarnings("unchecked")
			Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
			if (extras != null) {
				if (extras.get("itemId") != null) {
					itemId = new NodeRef(extras.get("itemId"));
				}
			}

			List<ListValueEntry> result = null;

			if (multiLevelExtract) {

				dataListFilter.updateMaxDepth(depthLevel);

				// need to load assoc so we use the MultiLevelDataListService
				MultiLevelListData mlld = multiLevelDataListService.getMultiLevelListData(dataListFilter, true, false);

				result = getParentsLevel(mlld, query, itemId, "");
			} else {
				result = getSimpleResults(entityNodeRef, query, itemId);
			}

			String state = (String) nodeService.getProperty(entityNodeRef, PLMModel.PROP_PRODUCT_STATE);

			result.add(new ListValueEntry(entityNodeRef.toString(), (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME),
					nodeService.getType(entityNodeRef).getLocalName() + "-" + state));

			return new ListValuePage(result, pageNum, pageSize, null);
		}
		return null;
	}

	private List<ListValueEntry> getSimpleResults(NodeRef entityNodeRef, String query, NodeRef itemId) {

		List<ListValueEntry> result = new ArrayList<>();

		NodeRef listsContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
		if (listsContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listsContainerNodeRef, PLMModel.TYPE_COMPOLIST);

			for (NodeRef dataListItemNodeRef : entityListDAO.getListItems(dataListNodeRef, PLMModel.TYPE_COMPOLIST)) {
				if (!dataListItemNodeRef.equals(itemId)) {

					NodeRef productNodeRef = associationService.getTargetAssoc(dataListItemNodeRef, PLMModel.ASSOC_COMPOLIST_PRODUCT);

					if (AccessStatus.ALLOWED.equals(permissionService.hasReadPermission(productNodeRef))) {

						QName type = nodeService.getType(productNodeRef);
						String productName = extractHierarchyFullName(productNodeRef);

						if (type.isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT) || type.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT)) {

							boolean addNode = false;

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

								String variantNames = extractVariantNames(dataListItemNodeRef);

								if (type.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT)) {
									result.add(new ListValueEntry(productNodeRef.toString(), variantNames + productName,
											PLMModel.TYPE_SEMIFINISHEDPRODUCT.getLocalName() + "-" + state));
								} else {
									result.add(new ListValueEntry(dataListItemNodeRef.toString(), variantNames + productName,
											PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.getLocalName() + "-" + state));
								}
							}
						}
					}
				}
			}

		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private String extractVariantNames(NodeRef dataListItemNodeRef) {

		List<NodeRef> variantNodeRefs = (List<NodeRef>) nodeService.getProperty(dataListItemNodeRef, PLMModel.PROP_VARIANTIDS);
		String variantNames = "";

		if (variantNodeRefs != null) {
			for (NodeRef variantNodeRef : variantNodeRefs) {
				if (!variantNames.isEmpty()) {
					variantNames += ",";
				}

				variantNames += ((String) nodeService.getProperty(variantNodeRef, ContentModel.PROP_NAME));
			}

			if (!variantNames.isEmpty()) {
				variantNames += " - ";
			}

		}

		return variantNames;
	}

	private List<ListValueEntry> getParentsLevel(MultiLevelListData mlld, String query, NodeRef itemId, String parentName) {

		List<ListValueEntry> result = new ArrayList<>();

		if (!parentName.isEmpty()) {
			parentName += " > ";
		}

		if (mlld != null) {

			for (Map.Entry<NodeRef, MultiLevelListData> kv : mlld.getTree().entrySet()) {

				NodeRef productNodeRef = kv.getValue().getEntityNodeRef();
				if (productNodeRef != null  && AccessStatus.ALLOWED.equals(permissionService.hasReadPermission(productNodeRef)) ) {
					logger.debug("productNodeRef: " + productNodeRef);

					// avoid cycle: when editing an item, cannot select itself
					// as
					// parent
					if ((itemId != null) && itemId.equals(kv.getKey())) {
						continue;
					}

					QName type = nodeService.getType(productNodeRef);
					String productName = extractHierarchyFullName(productNodeRef);

					if (type.isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT) || type.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT)) {

						boolean addNode = false;

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

							String variantNames = extractVariantNames(kv.getKey());

							if (type.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT)) {
								result.add(new ListValueEntry(productNodeRef.toString(), variantNames + parentName + productName,
										PLMModel.TYPE_SEMIFINISHEDPRODUCT.getLocalName() + "-" + state));
							} else {
								result.add(new ListValueEntry(kv.getKey().toString(), variantNames + parentName + productName,
										PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.getLocalName() + "-" + state));
							}
						}
					}

					if (kv.getValue() != null) {
						result.addAll(getParentsLevel(kv.getValue(), query, itemId, parentName + productName));
					}
				}
			}
		}

		return result;
	}

	private String extractHierarchyFullName(NodeRef hierarchy) {
		String res = (String) nodeService.getProperty(hierarchy, ContentModel.PROP_NAME);
			NodeRef parent = (NodeRef) nodeService.getProperty(hierarchy, BeCPGModel.PROP_PARENT_LEVEL);
			if (parent != null && AccessStatus.ALLOWED.equals(permissionService.hasReadPermission(parent))) {
				res = extractHierarchyFullName(parent) + " > " + res;
			}
			return res;
		
	}

	private int getDepthUserPref(DataListFilter dataListFilter) {
		String username = AuthenticationUtil.getFullyAuthenticatedUser();

		Map<String, Serializable> prefs = preferenceService.getPreferences(username);

		Integer depth = (Integer) prefs.get(MultiLevelExtractor.PREF_DEPTH_PREFIX + dataListFilter.getDataType().getLocalName());

		return depth != null ? depth : 0;
	}

}
