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
package fr.becpg.repo.autocomplete;

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
import fr.becpg.repo.autocomplete.impl.plugins.TargetAssocAutoCompletePlugin;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.entity.datalist.impl.MultiLevelExtractor;

/**
 * <p>CompoListValuePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 *
 * Autocomplete plugin that provide parent compoListItem using the multilevel extractor
 *
 * Example:
 * <pre>
 * {@code
 * <control template="/org/alfresco/components/form/controls/autocomplete.ftl">
 *		<control-param name="ds">becpg/autocomplete/compoListParentLevel
 * </control-param>
 *  }
 * </pre>
 *
 *  Datasources available:
 *
 *  Return compoListItem using the multilevel extractor
 *
 *  becpg/autocomplete/compoListParentLevel
 */
@Service("compoListAutoCompletePlugin")
public class CompoListAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	private static final Log logger = LogFactory.getLog(CompoListAutoCompletePlugin.class);

	private static final String SOURCE_TYPE_COMPOLIST_PARENT_LEVEL = "compoListParentLevel";

	@Autowired
	private MultiLevelDataListService multiLevelDataListService;

	@Autowired
	private PreferenceService preferenceService;

	@Autowired
	private EntityListDAO entityListDAO;

	@Autowired
	private PermissionService permissionService;

	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_COMPOLIST_PARENT_LEVEL };
	}

	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {


		NodeRef entityNodeRef = new NodeRef((String) props.get(AutoCompleteService.PROP_ENTITYNODEREF));
		logger.debug("CompoListValuePlugin sourceType: " + sourceType + " - entityNodeRef: " + entityNodeRef);

		if (sourceType.equals(SOURCE_TYPE_COMPOLIST_PARENT_LEVEL) ) {

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
			Map<String, String> extras = (HashMap<String, String>) props.get(AutoCompleteService.EXTRA_PARAM);
			if (extras != null) {
				if (extras.get("itemId") != null) {
					itemId = new NodeRef(extras.get("itemId"));
				}
			}

			List<AutoCompleteEntry> result = null;

			if (multiLevelExtract) {

				dataListFilter.updateMaxDepth(depthLevel);

				// need to load assoc so we use the MultiLevelDataListService
				MultiLevelListData mlld = multiLevelDataListService.getMultiLevelListData(dataListFilter, true, false);

				result = getParentsLevel(mlld, query, itemId, "");
			} else {
				result = getSimpleResults(entityNodeRef, query, itemId);
			}

			String state = (String) nodeService.getProperty(entityNodeRef, PLMModel.PROP_PRODUCT_STATE);

			result.add(new AutoCompleteEntry(entityNodeRef.toString(), (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME),
					nodeService.getType(entityNodeRef).getLocalName() + "-" + state));

			return new AutoCompletePage(result, pageNum, pageSize, null);
		}
		return null;
	}


	private List<AutoCompleteEntry> getSimpleResults(NodeRef entityNodeRef, String query, NodeRef itemId) {

		List<AutoCompleteEntry> result = new ArrayList<>();

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
									result.add(new AutoCompleteEntry(productNodeRef.toString(), variantNames + productName,
											PLMModel.TYPE_SEMIFINISHEDPRODUCT.getLocalName() + "-" + state));
								} else {
									result.add(new AutoCompleteEntry(dataListItemNodeRef.toString(), variantNames + productName,
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

		List<NodeRef> variantNodeRefs = (List<NodeRef>) nodeService.getProperty(dataListItemNodeRef, BeCPGModel.PROP_VARIANTIDS);
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

	private List<AutoCompleteEntry> getParentsLevel(MultiLevelListData mlld, String query, NodeRef itemId, String parentName) {

		List<AutoCompleteEntry> result = new ArrayList<>();

		if (!parentName.isEmpty()) {
			parentName += " > ";
		}

		if (mlld != null) {

			for (Map.Entry<NodeRef, MultiLevelListData> kv : mlld.getTree().entrySet()) {

				NodeRef productNodeRef = kv.getValue().getEntityNodeRef();
				if (productNodeRef != null && AccessStatus.ALLOWED.equals(permissionService.hasReadPermission(productNodeRef))) {
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
								result.add(new AutoCompleteEntry(productNodeRef.toString(), variantNames + parentName + productName,
										PLMModel.TYPE_SEMIFINISHEDPRODUCT.getLocalName() + "-" + state));
							} else {
								result.add(new AutoCompleteEntry(kv.getKey().toString(), variantNames + parentName + productName,
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
		Integer depth = null;
		if (!AuthenticationUtil.SYSTEM_USER_NAME.equals(username)) {
			Map<String, Serializable> prefs = preferenceService.getPreferences(username);

			depth = (Integer) prefs.get(MultiLevelExtractor.PREF_DEPTH_PREFIX + dataListFilter.getDataType().getLocalName());

		}
		return depth != null ? depth : 0;
	}

}
