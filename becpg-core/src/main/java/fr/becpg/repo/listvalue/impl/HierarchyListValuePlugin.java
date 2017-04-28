package fr.becpg.repo.listvalue.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.hierarchy.HierarchyService;
import fr.becpg.repo.listvalue.ListValuePage;

@Service
public class HierarchyListValuePlugin extends EntityListValuePlugin {

	private final static Log logger = LogFactory.getLog(HierarchyListValuePlugin.class);
	private static final String SOURCE_TYPE_HIERARCHY_MULTI_LEVEL = "hierarchyMultiLevel";

	@Autowired
	private HierarchyService hierarchyService;

	@Autowired
	private HierarchyValueExtractor hierarchyValueExtractor;

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_HIERARCHY_MULTI_LEVEL };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		logger.debug("suggest src=" + sourceType + ", query=" + query + ", props=" + props);

		String path = (String) props.get("path");

		List<NodeRef> allHierarchies = hierarchyService.getAllHierarchiesByPath(path, "*"+prepareQuery(query));
		logger.debug("Found " + allHierarchies.size() + " hierarchies");
		return new ListValuePage(allHierarchies, pageNum, pageSize, hierarchyValueExtractor);
	}
}
