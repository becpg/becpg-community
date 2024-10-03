package fr.becpg.repo.search.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.search.AdvSearchPlugin;

/**
 * <p>NestedAdvSearchPlugin class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("nestedAdvSearchPlugin")
public class NestedAdvSearchPlugin implements AdvSearchPlugin {

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	private static final String NESTED_PROP = "nested_";
	private static final String DATALIST_PROP = "dataList_";

	private static final String PROP_KEY = "_" + AttributeExtractorService.PROP_SUFFIX;
	private static final String ASSOC_KEY = "_" + AttributeExtractorService.ASSOC_SUFFIX;

	private static final Log logger = LogFactory.getLog(NestedAdvSearchPlugin.class);

	/**
	 * <p>extractNested.</p>
	 *
	 * @param criteriaMap a {@link java.util.Map} object.
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, Map<String, String>> extractNested(Map<String, String> criteriaMap) {
		Map<String, Map<String, String>> nested = new HashMap<>();
		if (criteriaMap != null) {
			for (String key : criteriaMap.keySet()) {
				if (key.startsWith(NESTED_PROP) && !key.contains(DATALIST_PROP)) {
					String nestedPropName = null;
					String nestedAssoc = null;

					if (key.contains(PROP_KEY)) {
						nestedPropName = AttributeExtractorService.PROP_SUFFIX + key.split(PROP_KEY)[1];
						nestedAssoc = key.split(PROP_KEY)[0].replace(NESTED_PROP, "").replace("_", ":");
					} else if (key.contains(ASSOC_KEY)) {
						nestedPropName = AttributeExtractorService.ASSOC_SUFFIX + key.split(ASSOC_KEY)[1];
						nestedAssoc = key.split(ASSOC_KEY)[0].replace(NESTED_PROP, "").replace("_", ":");
					}

					if (nestedPropName != null) {
						Map<String, String> nestedCriterias = new HashMap<>();
						if (nested.containsKey(nestedAssoc)) {
							nestedCriterias = nested.get(nestedAssoc);
						}
						nestedCriterias.put(nestedPropName, criteriaMap.get(key));

						nested.put(nestedAssoc, nestedCriterias);
					}

				}
			}
		}
		return nested;
	}

	/**
	 * <p>cleanCriteria.</p>
	 *
	 * @param criteriaMap a {@link java.util.Map} object.
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, String> cleanCriteria(Map<String, String> criteriaMap) {
		Map<String, String> ret = new HashMap<>();

		for (String key : criteriaMap.keySet()) {
			if (criteriaMap.get(key) != null && !criteriaMap.get(key).isEmpty()) {
				if (!key.equals(DataListFilter.PROP_DEPTH_LEVEL)) {
					if (!key.startsWith(AttributeExtractorService.ASSOC_SUFFIX) && !key.startsWith(NESTED_PROP)) {
						ret.put(key.replace(AttributeExtractorService.PROP_SUFFIX, "").replace("_", ":"),
								criteriaMap.get(key) != null ? criteriaMap.get(key).replace("=", "") : null);
					} else if (key.endsWith("_added")) {
						ret.put(key.replace(AttributeExtractorService.ASSOC_SUFFIX, "").replace(NESTED_PROP, "").replace("_added", "").replace("_",
								":"), criteriaMap.get(key));
					}
				}
			}
		}

		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public List<NodeRef> filter(List<NodeRef> nodes, QName datatype, Map<String, String> criteria, SearchConfig searchConfig) {
		if (criteria != null && !criteria.isEmpty()) {
			Map<String, Map<String, String>> nested = extractNested(criteria);

			if (!nested.isEmpty()) {
				filterWithNested(nodes, nested);
			}
		}

		return nodes;
	}

	private void filterWithNested(List<NodeRef> nodes, Map<String, Map<String, String>> nested) {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		for (Map.Entry<String, Map<String, String>> nestedEntry : nested.entrySet()) {
			String assocName = nestedEntry.getKey();
			QName assocQName = QName.createQName(assocName, namespaceService);
			Map<String, String> criteriaMap = cleanCriteria(nestedEntry.getValue());

			if (!criteriaMap.isEmpty()) {
				for (Iterator<NodeRef> iterator = nodes.iterator(); iterator.hasNext();) {
					NodeRef nodeRef = iterator.next();
					if (nodeService.exists(nodeRef)) {

						List<AssociationRef> assocRefs = nodeService.getTargetAssocs(nodeRef, assocQName);

						boolean foundMatch = false;

						for (AssociationRef assocRef : assocRefs) {
							if (match(assocRef.getTargetRef(), criteriaMap)) {
								foundMatch = true;
								break;
							}
						}

						if (!foundMatch) {
							iterator.remove();
						}
					}
				}
			}
		}

		if (logger.isDebugEnabled() && watch != null) {
			watch.stop();
			logger.debug("filterWithNested executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

	}

	/**
	 * <p>match.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param criteriaMap a {@link java.util.Map} object.
	 * @return a boolean.
	 */
	public boolean match(NodeRef nodeRef, Map<String, String> criteriaMap) {
		return attributeExtractorService.matchCriteria(nodeRef, criteriaMap);
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> getIgnoredFields(QName datatype, SearchConfig searchConfig) {
		return new HashSet<>();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSearchFiltered(Map<String, String> criteria) {
		return criteria != null && !extractNested(criteria).isEmpty();
	}

}
