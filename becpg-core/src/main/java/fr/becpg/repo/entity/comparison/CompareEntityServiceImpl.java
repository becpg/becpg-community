package fr.becpg.repo.entity.comparison;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.version.EntityVersionService;

/**
 * Compare several entities (properties, datalists and composite datalists). 
 *
 * @author querephi, kevin
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
	private CompareEntityServicePlugin[] plugins;

	/** {@inheritDoc} */
	@Override
	public List<CompareResultDataItem> compare(NodeRef entity1, List<NodeRef> entities, List<CompareResultDataItem> compareResult,
			Map<String, List<StructCompareResultDataItem>> structCompareResults) {

		if (entityVersionService.isVersion(entity1)) {
			entity1 = entityVersionService.extractVersion(entity1);
		}
		
		QName entityType = nodeService.getType(entity1);
		CompareEntityServicePlugin plugin = getPlugin(entityType);
		
		Map<String, CompareResultDataItem> comparisonMap = new LinkedHashMap<>();
		int pos = 1;
		int nbEntities = entities.size() + 1;

		for (NodeRef entity : entities) {
			logger.debug("compare entity " + entity1 + " with entity " + entity + " nbEntities " + nbEntities + " pos " + pos);
			
			if (entityVersionService.isVersion(entity)) {
				entity = entityVersionService.extractVersion(entity);
			}
			
			plugin.compareEntities(entity1, entity, nbEntities, pos, comparisonMap, structCompareResults);
			pos++;
		}

		for (CompareResultDataItem c : comparisonMap.values()) {
			compareResult.add(c);
		}
		
		return compareResult;
	}
	
	/** {@inheritDoc} */
	@Override
	public void compareStructDatalist(NodeRef entity1NodeRef, NodeRef entity2NodeRef, QName datalistType,
			Map<String, List<StructCompareResultDataItem>> structCompareResults) {
		QName entityType = nodeService.getType(entity1NodeRef);
		CompareEntityServicePlugin plugin = getPlugin(entityType);
		plugin.compareStructDatalist(entity1NodeRef, entity2NodeRef, datalistType, structCompareResults);
	}
	
	@Nonnull
	private CompareEntityServicePlugin getPlugin(QName entityType) {
		CompareEntityServicePlugin ret = null;

		for (CompareEntityServicePlugin plugin : plugins) {
			if (plugin.applyTo(entityType) || (plugin.isDefault() && (ret == null))) {
				ret = plugin;
			}
		}

		if (ret == null) {
			throw new IllegalStateException("No default plugin");
		}

		return ret;
	}

}
