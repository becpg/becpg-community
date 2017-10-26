package fr.becpg.repo.search.impl;

import java.util.ArrayList;
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
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;
import fr.becpg.repo.search.AdvSearchPlugin;

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
	private static final String PROP_SUFFIX = "prop_";
	private static final String PROP_KEY = "_"+PROP_SUFFIX;
	private static final String ASSOC_SUFFIX = "assoc_";
	private static final String ASSOC_KEY = "_"+ASSOC_SUFFIX;

	private static final Log logger = LogFactory.getLog(NestedAdvSearchPlugin.class);

	public Map<String, Map<String, String>> extractNested(Map<String, String> criteriaMap) {
		Map<String, Map<String, String>> nested = new HashMap<>();

		for (String key : criteriaMap.keySet()) {
			if (key.startsWith(NESTED_PROP) && !key.contains(DATALIST_PROP)) {
				String nestedPropName = null;
				String nestedAssoc = null;

				if (key.contains(PROP_KEY)) {
					nestedPropName = PROP_SUFFIX + key.split(PROP_KEY)[1];
					nestedAssoc = key.split(PROP_KEY)[0].replace(NESTED_PROP, "").replace("_", ":");
				} else if (key.contains(ASSOC_KEY)) {
					nestedPropName = ASSOC_SUFFIX + key.split(ASSOC_KEY)[1];
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

		return nested;
	}

	public Map<String, String> cleanCriteria(Map<String, String> criteriaMap) {
		Map<String, String> ret = new HashMap<>();

		for (String key : criteriaMap.keySet()) {
			if (criteriaMap.get(key) != null && !criteriaMap.get(key).isEmpty()) {
				if (!key.equals(DataListFilter.PROP_DEPTH_LEVEL)) {
					if (!key.startsWith(ASSOC_SUFFIX) && !key.startsWith(NESTED_PROP)) {
						ret.put(key.replace(PROP_SUFFIX, "").replace("_", ":"), criteriaMap.get(key));
					} else if (key.endsWith("_added")) {
						ret.put(key.replace(ASSOC_SUFFIX, "").replace(NESTED_PROP, "")
								.replace("_added", "").replace("_", ":"), criteriaMap.get(key));
					}
				}
			}
		}

		return ret;
	}

	@Override
	public List<NodeRef> filter(List<NodeRef> nodes, QName datatype, Map<String, String> criteria) {
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
							}
						}

						if (!foundMatch) {
							iterator.remove();
						}
					}
				}
			}
		}

		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("filterWithNested executed in  " + watch.getTotalTimeSeconds() + " seconds ");
		}

	}

	@SuppressWarnings("unchecked")
	public boolean match(NodeRef nodeRef, Map<String, String> criteriaMap) {
		if (!criteriaMap.isEmpty()) {
			
			Map<String, Object> comp = attributeExtractorService.extractNodeData(nodeRef, nodeService.getType(nodeRef),
					new ArrayList<>(criteriaMap.keySet()), AttributeExtractorMode.JSON);
			for (String key : comp.keySet()) {
				String critKey = key.replace(PROP_SUFFIX, "").replace(ASSOC_SUFFIX, "").replace("_", ":");

				Object tmp = comp.get(key);
				if (tmp != null) {
					Map<String, Object> data = null;
					if (tmp instanceof ArrayList<?>) {
						if (((ArrayList<?>) tmp).size() > 0) {
							data = (Map<String, Object>) ((ArrayList<?>) tmp).get(0);
						}
					} else {
						data = (Map<String, Object>) tmp;
					}

					if (data == null || data.get("value") == null) {
						return false;
					}

					if (logger.isTraceEnabled()) {
						logger.trace("Test Match on: " + critKey);
						logger.trace("Test Match : " + data.get("value").toString().toLowerCase() + " - " + criteriaMap.get(critKey).toLowerCase());
					}

					String compValue = criteriaMap.get(critKey).toLowerCase();
					String value = data.get("value").toString().toLowerCase();
					String displayValue = data.get("displayValue").toString().toLowerCase();
					if (compValue.startsWith("\"") && compValue.endsWith("\"")) {
						compValue = compValue.replaceAll("\"", "");
						if (!value.equals(compValue.toLowerCase()) && !displayValue.equals(compValue)) {
							return false;
						}
					}

					if (!value.contains(compValue.toLowerCase()) && !displayValue.contains(compValue)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	@Override
	public Set<String> getIgnoredFields(QName datatype) {
		return new HashSet<>();
	}

}
