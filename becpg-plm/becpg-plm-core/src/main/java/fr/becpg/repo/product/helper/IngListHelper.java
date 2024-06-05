package fr.becpg.repo.product.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

/**
 * <p>IngListHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class IngListHelper {

	private IngListHelper() {
		//Do Nothing
	}

	/**
	 * <p>extractParentList.</p>
	 *
	 * @param ingList a {@link java.util.List} object
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @return a {@link java.util.List} object
	 */
	public static List<IngListDataItem> extractParentList(List<IngListDataItem> ingList, AssociationService associationService,
			AlfrescoRepository<RepositoryEntity> alfrescoRepository) {

		List<IngListDataItem> ret = new ArrayList<>();

		for (IngListDataItem ingListDataItem : ingList) {
			addParentList(ret, ingListDataItem, associationService, alfrescoRepository);
		}

		return ret;
	}

	private static List<IngListDataItem> addParentList(List<IngListDataItem> ret, IngListDataItem ingListDataItem,
			AssociationService associationService, AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		
		ret.add(ingListDataItem);
		NodeRef parentEntity = associationService.getTargetAssoc(ingListDataItem.getIng(), BeCPGModel.ASSOC_PARENT_ENTITY);
		
		Map<IngListDataItem, IngListDataItem> parentCache = new HashMap<>();
		
		if (parentEntity != null) {
			ProductData componentProductData = (ProductData) alfrescoRepository.findOne(parentEntity);
			if ((componentProductData.getIngList() != null) && !componentProductData.getIngList().isEmpty()) {
				for (IngListDataItem subIngListDataItem : componentProductData.getIngList()) {
					
					IngListDataItem toAdd = subIngListDataItem.copy();
					parentCache.put(subIngListDataItem, toAdd);
					
					if(toAdd.getParent() == null) {
						toAdd.setParent(ingListDataItem);
					} else {
						toAdd.setParent(parentCache.get(toAdd.getParent()));
					}
					if ((toAdd != null) && (toAdd.getQtyPerc() != null) && (ingListDataItem != null)
							&& (ingListDataItem.getQtyPerc() != null)) {
						toAdd.setQtyPerc((toAdd.getQtyPerc() * ingListDataItem.getQtyPerc()) / 100d);
					}

					if ((toAdd != null) && (toAdd.getVolumeQtyPerc() != null) && (ingListDataItem != null)
							&& (ingListDataItem.getVolumeQtyPerc() != null)) {
						toAdd.setVolumeQtyPerc((toAdd.getVolumeQtyPerc() * ingListDataItem.getVolumeQtyPerc()) / 100d);
					}

					if ((toAdd != null) && (toAdd.getMaxi() != null) && (ingListDataItem != null)
							&& (ingListDataItem.getMaxi() != null)) {
						toAdd.setMaxi((toAdd.getMaxi() * ingListDataItem.getMaxi()) / 100d);
					}

					if ((toAdd != null) && (toAdd.getMini() != null) && (ingListDataItem != null)
							&& (ingListDataItem.getMini() != null)) {
						toAdd.setMini((toAdd.getMini() * ingListDataItem.getMini()) / 100d);
					}

					addParentList(ret, toAdd, associationService, alfrescoRepository);
				}
			}

		}
		return ret;

	}

}
