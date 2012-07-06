package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;

public class SpelEditorListValuePlugin extends EntityListValuePlugin {

	/** The logger. */
	private static Log logger = LogFactory.getLog(SpelEditorListValuePlugin.class);

	/** The Constant SOURCE_TYPE_TARGET_ASSOC. */
	private static final String SOURCE_TYPE_SPELEDITOR = "speleditor";

	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_SPELEDITOR };
	}

	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String className = (String) props.get(ListValueService.PROP_CLASS_NAME);
		// Class is a Java class
		try {
			Class<?> c = Class.forName(className);
			Field[] fields = c.getDeclaredFields();
			List<ListValueEntry> ret = new ArrayList<ListValueEntry>();
			for (int i = 0; i < fields.length; i++) {
				ret.add(new ListValueEntry(fields[i].getName(), fields[i].getName(), fields[i].getType().getSimpleName()));
			}
			return new ListValuePage(ret, pageNum, pageSize, null);

		} catch (ClassNotFoundException e) {
			logger.debug(e, e);
		}

		QName type = QName.createQName(className, namespaceService);

		if (type.equals(BeCPGModel.TYPE_DYNAMICCHARCATLIST)) {
			return suggestVariable(type, query, pageNum, pageSize, new NodeRef((String) props.get(ListValueService.PROP_NODEREF)));
		}

		return suggestTargetAssoc(type, query, pageNum, pageSize, null);

	}

	private ListValuePage suggestVariable(QName type, String query, Integer pageNum, Integer pageSize, NodeRef nodeRef) {
		String queryPath = "";

		query = prepareQuery(query);
		queryPath = String
				.format(" +TYPE:\"%s\"  +@bcpg\\:dynamicCharachTitle:(%s) +PATH:\"/%s//*\"  ", type, query, nodeService.getPath(nodeRef).toPrefixString(namespaceService));

		logger.debug("suggestVariable for query : " + queryPath);

		List<NodeRef> ret = beCPGSearchService.luceneSearch(queryPath, LuceneHelper.getSort(BeCPGModel.PROP_DYNAMICCHARCAT_TITLE), RepoConsts.MAX_SUGGESTIONS);

		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(BeCPGModel.PROP_DYNAMICCHARCAT_TITLE, nodeService));
	}

}
