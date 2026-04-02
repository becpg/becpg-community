package fr.becpg.repo.copy;

import java.util.Date;

import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.EffectiveDataItem;

/**
 * <p>EffectiveFilterBeCPGCopyPlugin class.</p>
 *
 * @author matthieu
 */
@Component
public class EffectiveFilterBeCPGCopyPlugin implements BeCPGCopyPlugin {

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityService entityService;

	/** {@inheritDoc} */
	@Override
	public boolean shouldCopy(String typeToReset, CopyDetails copyDetails) {
		if (typeToReset.contains("@")) {
			String[] split = typeToReset.split("@");
			if (split.length > 1 && split[0].equals(entityDictionaryService.toPrefixString(copyDetails.getSourceNodeTypeQName()))
					&& split[1].startsWith(EffectiveFilters.class.getSimpleName())) {
				String effectiveFilter = split[1].replace(EffectiveFilters.class.getSimpleName() + ".", "");
				RepositoryEntity sourceData = alfrescoRepository.findOne(copyDetails.getSourceNodeRef());
				if (sourceData instanceof EffectiveDataItem effectiveDataItem) {
					NodeRef entityNodeRef = entityService.getEntityNodeRef(copyDetails.getSourceNodeRef(), copyDetails.getSourceNodeTypeQName());
					Date startEffectivity = (Date) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
					Date endEffectivity = (Date) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_END_EFFECTIVITY);
					EffectiveFilters<EffectiveDataItem> filter = new EffectiveFilters<>(effectiveFilter);
					if (!filter.createPredicate(startEffectivity, endEffectivity).test(effectiveDataItem)) {
						return false;
					}
				}
			}
		}
		return true;
	}

}
