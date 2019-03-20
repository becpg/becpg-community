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
 * @author quere
 *
 */
public class PackagingData {
	private final Map<NodeRef, VariantPackagingData> variants = new HashMap<>();

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

	public Map<NodeRef, VariantPackagingData> getVariants() {
		return variants;
	}
	
}
