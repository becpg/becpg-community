package fr.becpg.repo.web.scripts.entity.datalist;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.entity.datalist.impl.MultiLevelExtractor;

/**
 * <p>MultiLevelDataListWebscript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MultiLevelDataListWebscript extends AbstractWebScript {

	private static final String PARAM_NODEREF = "nodeRef";
	private static final String PARAM_EXPAND = "expand";
	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";
	private static final String PARAM_LIST_TYPE = "listType";

	private MultiLevelDataListService multiLevelDataListService;

	private NamespaceService namespaceService;

	private EntityDictionaryService entityDictionaryService;

	private NodeService nodeService;

	private PreferenceService preferenceService;

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>multiLevelDataListService</code>.</p>
	 *
	 * @param multiLevelDataListService a {@link fr.becpg.repo.entity.datalist.MultiLevelDataListService} object.
	 */
	public void setMultiLevelDataListService(MultiLevelDataListService multiLevelDataListService) {
		this.multiLevelDataListService = multiLevelDataListService;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>preferenceService</code>.</p>
	 *
	 * @param preferenceService a {@link org.alfresco.service.cmr.preference.PreferenceService} object.
	 */
	public void setPreferenceService(PreferenceService preferenceService) {
		this.preferenceService = preferenceService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String nodeRef = req.getParameter(PARAM_NODEREF);
		String entityNodeRef = req.getParameter(PARAM_ENTITY_NODEREF);
		String dataListType = req.getParameter(PARAM_LIST_TYPE);

		boolean expand = "true".equals(req.getParameter(PARAM_EXPAND));

		if (nodeRef != null) {

			NodeRef entityToExpand = new NodeRef(nodeRef);

			if (nodeService.exists(entityToExpand)) {

				if ((entityNodeRef != null) && (dataListType != null)
						&& !entityDictionaryService.isSubClass(nodeService.getType(entityToExpand), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {

					DataListFilter dataListFilter = new DataListFilter();
					dataListFilter.setDataType(QName.createQName(dataListType, namespaceService));
					dataListFilter.setEntityNodeRefs(Collections.singletonList(new NodeRef(entityNodeRef)));
					dataListFilter.updateMaxDepth(getDepthUserPref(dataListFilter));

					MultiLevelListData mlld = multiLevelDataListService.getMultiLevelListData(dataListFilter);

					entityToExpand = getEntityToExpand(mlld, entityToExpand);

				}

				if (entityToExpand != null) {
					multiLevelDataListService.expandOrColapseNode(entityToExpand, expand);
				} else {
					throw new WebScriptException("Cannot find node to expand");
				}
				JSONObject ret = new JSONObject();

				try {
					ret.put("nodeRef", entityToExpand);
					ret.put("success", true);
					res.setContentType("application/json");
					res.setContentEncoding("UTF-8");
					ret.write(res.getWriter());
				} catch (JSONException e) {
					throw new WebScriptException("Unable to parse JSON", e);
				}

			} else {
				throw new WebScriptException("nodeRef is mandatory");
			}
		}
	}

	private NodeRef getEntityToExpand(MultiLevelListData mlld, NodeRef productNodeRef) {

		NodeRef ret = null;

		if (mlld != null) {
			for (Map.Entry<NodeRef, MultiLevelListData> kv : mlld.getTree().entrySet()) {

				if (kv.getValue() != null) {
					if (kv.getValue().getEntityNodeRef().equals(productNodeRef) || kv.getKey().equals(productNodeRef)) {
						return kv.getKey();
					} else {
						ret = getEntityToExpand(kv.getValue(), productNodeRef);
						if (ret != null) {
							return ret;
						}
					}
				}
			}
		}
		return ret;
	}

	private int getDepthUserPref(DataListFilter dataListFilter) {
		String username = AuthenticationUtil.getFullyAuthenticatedUser();

		Map<String, Serializable> prefs = preferenceService.getPreferences(username);

		Integer depth = (Integer) prefs.get(MultiLevelExtractor.PREF_DEPTH_PREFIX + dataListFilter.getDataType().getLocalName());

		return depth != null ? depth : 1;
	}

}
