package fr.becpg.repo.copy;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.model.EffectiveDataItem;

@Component
public class EffectiveFilterCopyRestrictionPlugin implements CopyRestrictionPlugin {

	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private EntityService entityService;

	@Override
	public boolean shouldCopy(QName sourceClassQName, NodeRef sourceNodeRef, NodeRef targetNodeRef, String typeToReset) {
		if (typeToReset.contains("@")) {
			String[] split = typeToReset.split("@");
			if (split.length > 1 && split[0].equals(entityDictionaryService.toPrefixString(sourceClassQName))
					&& split[1].startsWith(EffectiveFilters.class.getSimpleName())) {
				String effectiveFilter = split[1].replace(EffectiveFilters.class.getSimpleName() + ".", "");
				RepositoryEntity sourceData = alfrescoRepository.findOne(sourceNodeRef);
				if (sourceData instanceof EffectiveDataItem effectiveDataItem) {
					NodeRef entityNodeRef = entityService.getEntityNodeRef(sourceNodeRef, sourceClassQName);
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