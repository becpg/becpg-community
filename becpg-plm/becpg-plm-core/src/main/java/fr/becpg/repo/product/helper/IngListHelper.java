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

	/**
	 * <p>Constructor for IngListHelper.</p>
	 */
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

	/**
	 * Returns a copy of the ingredient list where the percentages of depth-declared
	 * sub-ingredients (items whose parent belongs to the same list) are rescaled by their parent
	 * percentage.
	 * <p>
	 * In a manually entered ingredient list, sub-ingredient percentages are expressed relative to
	 * their parent ingredient (children sum to 100 %), exactly like the sub-entity lists handled by
	 * {@link #extractParentList(List, AssociationService, AlfrescoRepository)}. This method converts
	 * them into absolute percentages of the owning entity so the formulation aggregates children
	 * consistently with their parent (see #34702). Children of a parent declared at 0 % (or without
	 * percentage) are kept as-is: such a parent is a label-only wrapper and scaling would zero the
	 * real percentages carried by its children. Top-level items are returned as-is; children are
	 * defensive copies, so the persisted component list is never mutated. Lists produced by the
	 * formulation already hold absolute child percentages and must not go through this method.
	 *
	 * @param ingList the manually entered ingredient list
	 * @return the list with children rescaled by their parent percentage, or the original list when
	 *         it contains no depth-declared children
	 */
	public static List<IngListDataItem> scaleRelativeDepthChildren(List<IngListDataItem> ingList) {

		boolean hasDepthChildren = false;
		for (IngListDataItem item : ingList) {
			if (item.getParent() != null) {
				hasDepthChildren = true;
				break;
			}
		}
		if (!hasDepthChildren) {
			return ingList;
		}

		Map<IngListDataItem, IngListDataItem> scaledItems = new HashMap<>();
		List<IngListDataItem> ret = new ArrayList<>(ingList.size());
		for (IngListDataItem item : ingList) {
			ret.add(scaleDepthChild(item, scaledItems));
		}
		return ret;
	}

	private static IngListDataItem scaleDepthChild(IngListDataItem item, Map<IngListDataItem, IngListDataItem> scaledItems) {

		IngListDataItem scaled = scaledItems.get(item);
		if (scaled != null) {
			return scaled;
		}

		if (item.getParent() == null) {
			scaledItems.put(item, item);
			return item;
		}

		IngListDataItem scaledParent = scaleDepthChild(item.getParent(), scaledItems);

		// A parent declared at 0 % (or without percentage) is a label-only wrapper: its children
		// carry the real percentages and must be kept as-is instead of being zeroed
		if ((scaledParent.getQtyPerc() == null) || (scaledParent.getQtyPerc() <= 0d)) {
			if (scaledParent == item.getParent()) {
				scaledItems.put(item, item);
				return item;
			}
			IngListDataItem unscaled = item.copy();
			unscaled.setParent(scaledParent);
			scaledItems.put(item, unscaled);
			return unscaled;
		}

		IngListDataItem copy = item.copy();
		copy.setParent(scaledParent);

		if (copy.getQtyPerc() != null) {
			copy.setQtyPerc((copy.getQtyPerc() * scaledParent.getQtyPerc()) / 100d);
		}

		if (copy.getQtyPercWithYield() != null) {
			Double parentQtyPercWithYield = scaledParent.getQtyPercWithYield() != null ? scaledParent.getQtyPercWithYield()
					: scaledParent.getQtyPerc();
			if ((parentQtyPercWithYield != null) && (parentQtyPercWithYield > 0d)) {
				copy.setQtyPercWithYield((copy.getQtyPercWithYield() * parentQtyPercWithYield) / 100d);
			}
		}

		if ((copy.getVolumeQtyPerc() != null) && (scaledParent.getVolumeQtyPerc() != null) && (scaledParent.getVolumeQtyPerc() > 0d)) {
			copy.setVolumeQtyPerc((copy.getVolumeQtyPerc() * scaledParent.getVolumeQtyPerc()) / 100d);
		}

		if ((copy.getMini() != null) && (scaledParent.getMini() != null) && (scaledParent.getMini() > 0d)) {
			copy.setMini((copy.getMini() * scaledParent.getMini()) / 100d);
		}

		if ((copy.getMaxi() != null) && (scaledParent.getMaxi() != null) && (scaledParent.getMaxi() > 0d)) {
			copy.setMaxi((copy.getMaxi() * scaledParent.getMaxi()) / 100d);
		}

		scaledItems.put(item, copy);
		return copy;
	}

	/**
	 * <p>addParentList.</p>
	 *
	 * @param ret a {@link java.util.List} object
	 * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @return a {@link java.util.List} object
	 */
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

					if ((toAdd != null) && (toAdd.getQtyPercWithYield() != null) && (ingListDataItem != null)) {
						Double parentQtyPercWithYield = ingListDataItem.getQtyPercWithYield() != null ? ingListDataItem.getQtyPercWithYield()
								: ingListDataItem.getQtyPerc();
						if (parentQtyPercWithYield != null) {
							toAdd.setQtyPercWithYield((toAdd.getQtyPercWithYield() * parentQtyPercWithYield) / 100d);
						}
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
