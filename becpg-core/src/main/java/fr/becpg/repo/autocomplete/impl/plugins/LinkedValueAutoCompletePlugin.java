package fr.becpg.repo.autocomplete.impl.plugins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.api.BeCPGPublicApi;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.autocomplete.AutoCompletePage;
import fr.becpg.repo.autocomplete.AutoCompleteService;
import fr.becpg.repo.autocomplete.impl.extractors.LinkedValueAutoCompleteExtractor;
import fr.becpg.repo.autocomplete.impl.extractors.NodeRefAutoCompleteExtractor;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.hierarchy.HierarchyService;

/**
 * <p>LinkedValueAutoCompletePlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 *
 * Autocomplete plugin to retrieve linked values ( hierachy)
 *
 * Example:
 * <pre>
 * {@code
 *	<control template="/org/alfresco/components/form/controls/autocomplete.ftl">
 *		<control-param name="ds">becpg/autocomplete/linkedvalue/values/System/QualityLists/bcpg:entityLists/claimOrigin_Hierarchy</control-param>
 *		<control-param name="parent">qa_claimOriginHierarchy1</control-param>
 *	</control>
 *
 *	<control template="/org/alfresco/components/form/controls/autocomplete.ftl">
 *		<control-param name="ds">becpg/autocomplete/allLinkedvalue/values/System/ProductHierarchy/bcpg:entityLists?extra.depthLevel=1&amp;extra.paths=finishedProduct_Hierarchy,semiFinishedProduct_Hierarchy,rawMaterialProduct_Hierarchy
 *		</control-param>
 *	</control>
 * }
 * </pre>
 *
 *  Datasources:
 *
 *  ds: becpg/autocomplete/linkedvalue/values/{path}
 *  param : {path} return hierarchy at depth level 0
 *  control-param: {parent} return hierarchy with parent
 *
 *  ds: becpg/autocomplete/allLinkedvalue/values/{path}?extra.depthLevel={depthLevel?}&amp;extra.paths={paths?}&amp;extra.list={lists?}
 *  param : {path} return all hierarchy starting at depth level path
 *  url-param: {paths} look hierachy in coma separated paths
 *  url-param: {depthLevel} specify start depth-level
 *  url-param: {list}  look hierachy in spefic list in the current entity
 */
@Service("linkedValueAutoCompletePlugin")
@BeCPGPublicApi
public class LinkedValueAutoCompletePlugin extends TargetAssocAutoCompletePlugin {

	/** Constant <code>SOURCE_TYPE_LINKED_VALUE="linkedvalue"</code> */
	protected static final String SOURCE_TYPE_LINKED_VALUE = "linkedvalue";
	/** Constant <code>SOURCE_TYPE_LINKED_VALUE_ALL="allLinkedvalue"</code> */
	protected static final String SOURCE_TYPE_LINKED_VALUE_ALL = "allLinkedvalue";
	
	@Autowired
	private HierarchyService hierarchyService;
	@Autowired
	private EntityListDAO entityListDAO;
	
	/** {@inheritDoc} */
	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_LINKED_VALUE, SOURCE_TYPE_LINKED_VALUE_ALL };
	}
	
	/** {@inheritDoc} */
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String path = (String) props.get(AutoCompleteService.PROP_PATH);

		switch (sourceType) {
		case SOURCE_TYPE_LINKED_VALUE:
			return suggestLinkedValue(path, query, pageNum, pageSize, props, false);
		case SOURCE_TYPE_LINKED_VALUE_ALL:
			return suggestLinkedValue(path, query, pageNum, pageSize, props, true);
		default:
			return null;
		}

	}

	
	/**
	 * Suggest linked value according to query
	 *
	 * Query path : +PATH:
	 * "/app:company_home/cm:System/cm:LinkedLists/cm:Hierarchy/cm:Hierarchy1_Hierarchy2*"
	 * +TYPE:"bcpg:LinkedValue" +@cm\:lkvPrevValue:"hierar*".
	 *
	 * @param path
	 *            the path
	 * @param parent
	 *            the parent
	 * @param query
	 *            the query
	 * @param b
	 * @return the map
	 */
	private AutoCompletePage suggestLinkedValue(String path, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props,
			boolean all) {

		NodeRef itemIdNodeRef = null;

		@SuppressWarnings("unchecked")
		Map<String, String> extras = (HashMap<String, String>) props.get(AutoCompleteService.EXTRA_PARAM);

		if (path == null) {
			NodeRef entityNodeRef = null;

			if (extras != null) {
				if ((extras.get(AutoCompleteService.EXTRA_PARAM_DESTINATION) != null) && NodeRef.isNodeRef(extras.get(AutoCompleteService.EXTRA_PARAM_DESTINATION))) {
					entityNodeRef = new NodeRef(extras.get(AutoCompleteService.EXTRA_PARAM_DESTINATION));
				} else if ((extras.get(AutoCompleteService.EXTRA_PARAM_ITEMID) != null) && NodeRef.isNodeRef(extras.get(AutoCompleteService.EXTRA_PARAM_ITEMID))) {
					itemIdNodeRef = new NodeRef(extras.get(AutoCompleteService.EXTRA_PARAM_ITEMID));
					entityNodeRef = nodeService.getPrimaryParent(itemIdNodeRef).getParentRef();
				} else if (extras.get(AutoCompleteService.EXTRA_PARAM_LIST) != null) {
					QName dataListQName = QName.createQName(extras.get(AutoCompleteService.EXTRA_PARAM_LIST), namespaceService);
					entityNodeRef = new NodeRef((String) props.get(AutoCompleteService.PROP_ENTITYNODEREF));
					NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
					if (listContainerNodeRef != null) {
						entityNodeRef = entityListDAO.getList(listContainerNodeRef, dataListQName);
					}
				}

				if (entityNodeRef != null) {
					path = nodeService.getPath(entityNodeRef).toPrefixString(namespaceService);
				}
			}
		}

		query = prepareQuery(query);
		List<NodeRef> ret = null;

		if (!all) {

			String parent = (String) props.get(AutoCompleteService.PROP_PARENT);
			if ((parent != null) && !NodeRef.isNodeRef(parent)) {
				ret = new ArrayList<>();
			} else {
				NodeRef parentNodeRef = (parent != null) && NodeRef.isNodeRef(parent) ? new NodeRef(parent) : null;
				Boolean includeDeleted = props.containsKey(AutoCompleteService.PROP_INCLUDE_DELETED) && (Boolean) props.get(AutoCompleteService.PROP_INCLUDE_DELETED);
				ret = hierarchyService.getHierarchiesByPath(path, parentNodeRef, query, includeDeleted != null && includeDeleted.booleanValue());
			}
		} else if ((extras != null) && extras.containsKey(AutoCompleteService.EXTRA_PARAM_DEPTH_LEVEL)) {
			String depthLevel = extras.get(AutoCompleteService.EXTRA_PARAM_DEPTH_LEVEL);
			if (extras.containsKey(AutoCompleteService.EXTRA_PARAM_PATHS)) {
				ret = new LinkedList<>();
				for (String subPath : extras.get(AutoCompleteService.EXTRA_PARAM_PATHS).split(",")) {
					ret.addAll(hierarchyService.getAllHierarchiesByDepthLevel(path + "/" + subPath, query, depthLevel));
				}
			} else {
				ret = hierarchyService.getAllHierarchiesByDepthLevel(path, query, depthLevel);
			}
		} else {
			if (extras.containsKey(AutoCompleteService.EXTRA_PARAM_PATHS)) {
				ret = new LinkedList<>();
				for (String subPath : extras.get(AutoCompleteService.EXTRA_PARAM_PATHS).split(",")) {
					ret.addAll(hierarchyService.getAllHierarchiesByPath(path + "/" + subPath, query));
				}
			} else {
				ret = hierarchyService.getAllHierarchiesByPath(path, query);
			}
		}

		// avoid cycle: when editing an item, cannot select itself as parent
		if ((itemIdNodeRef != null) && ret.contains(itemIdNodeRef)) {
			ret.remove(itemIdNodeRef);
		}

		return new AutoCompletePage(ret, pageNum, pageSize,
				all ? new LinkedValueAutoCompleteExtractor(nodeService) : new NodeRefAutoCompleteExtractor(BeCPGModel.PROP_LKV_VALUE, nodeService));
	}
}
