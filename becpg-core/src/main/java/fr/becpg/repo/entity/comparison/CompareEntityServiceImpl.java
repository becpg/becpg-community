package fr.becpg.repo.entity.comparison;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.entity.version.VersionHelper;

/**
 * Compare several entities (properties, datalists and composite datalists).
 *
 * @author querephi
 * @version $Id: $Id
 */
@Service("compareEntityService")
public class CompareEntityServiceImpl implements CompareEntityService {
	
	private static final Log logger = LogFactory.getLog(CompareEntityServiceImpl.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityVersionService entityVersionService;
	
	@Autowired
	private CompareEntityServicePlugin[] compareEntityServicePlugins;

	/** {@inheritDoc} */
	@Override
	public List<CompareResultDataItem> compare(NodeRef entity1, List<NodeRef> entities, List<CompareResultDataItem> compareResult,
			Map<String, List<StructCompareResultDataItem>> structCompareResults) {

		if (VersionHelper.isVersion(entity1)) {
			entity1 = entityVersionService.extractVersion(entity1);
		}
		
		Map<String, CompareResultDataItem> comparisonMap = new LinkedHashMap<>();
		int pos = 1;
		int nbEntities = entities.size() + 1;
		
		CompareEntityServicePlugin compareEntityServicePlugin = getPlugin(nodeService.getType(entity1));

		for (NodeRef entity : entities) {
			logger.debug("compare entity " + entity1 + " with entity " + entity + " nbEntities " + nbEntities + " pos " + pos);
			
			if (VersionHelper.isVersion(entity)) {
				entity = entityVersionService.extractVersion(entity);
			}
			
			compareEntityServicePlugin.compareEntities(entity1, entity, nbEntities, pos, comparisonMap, structCompareResults);
			pos++;
		}

		for (CompareResultDataItem c : comparisonMap.values()) {
			compareResult.add(c);
		}
		return compareResult;
	}

	private CompareEntityServicePlugin getPlugin(QName type) {
		CompareEntityServicePlugin defaultPlugin = null;
		for (CompareEntityServicePlugin plugin : compareEntityServicePlugins) {
			if (plugin.applyTo(type)) {
				return plugin;
			}
			if (plugin.isDefault()) {
				defaultPlugin = plugin;
			}
		}
		if (defaultPlugin == null) {
			throw new IllegalStateException("No default compare entity plugin!");
		}
		return defaultPlugin;
	}


}
