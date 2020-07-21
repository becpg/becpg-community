package fr.becpg.repo.product.data.packaging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.variant.model.VariantData;

/**
 * Manage packaging data with variants
 *
 * @author quere
 * @version $Id: $Id
 */
public class PackagingData {
	private final Map<NodeRef, VariantPackagingData> variants = new HashMap<>();

	/**
	 * <p>getVariantPackagingData.</p>
	 *
	 * @param variantNodeRefs a {@link java.util.List} object.
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<VariantPackagingData> getVariantPackagingData(List<NodeRef> variantNodeRefs) {
		if (variantNodeRefs == null || variantNodeRefs.isEmpty()) {
			return variants.values();
		}
		List<VariantPackagingData> selectedVariants = new ArrayList<>();
		for (NodeRef variantNodeRef : variantNodeRefs) {
			if(variants.get(variantNodeRef)!=null){
				selectedVariants.add(variants.get(variantNodeRef));
			}
		}
		return selectedVariants;
	}

	/**
	 * <p>Constructor for PackagingData.</p>
	 *
	 * @param variantDataList a {@link java.util.List} object.
	 */
	public PackagingData(List<VariantData> variantDataList) {
		boolean hasDefaultVariant = false;

		if(variantDataList!=null){
			for (VariantData variantData : variantDataList) {
				variants.put(variantData.getNodeRef(), new VariantPackagingData());
				if (variantData.getIsDefaultVariant()) {
					hasDefaultVariant = true;
				}
			}
		}

		if (!hasDefaultVariant) {
			variants.put(null, new VariantPackagingData());
		}
	}

	/**
	 * <p>Getter for the field <code>variants</code>.</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<NodeRef, VariantPackagingData> getVariants() {
		return variants;
	}
	
}
