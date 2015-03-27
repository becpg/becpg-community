package fr.becpg.repo.product.data.packaging;

import java.math.BigDecimal;
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
	private Map<NodeRef, VariantPackagingData> variants = new HashMap<>();

	private Collection<VariantPackagingData> getVariantPackagingData(List<NodeRef> variantNodeRefs) {
		if (variantNodeRefs == null || variantNodeRefs.isEmpty()) {
			return variants.values();
		}
		List<VariantPackagingData> selectedVariants = new ArrayList<>();
		for (NodeRef variantNodeRef : variantNodeRefs) {
			selectedVariants.add(variants.get(variantNodeRef));
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

	public void addTareSecondary(List<NodeRef> variantNodeRefs, BigDecimal value) {
		if (value != null) {
			for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
				if (variantPackagingData.getTareSecondary() != null) {
					variantPackagingData.setTareSecondary(variantPackagingData.getTareSecondary().add(value));
				} else {
					variantPackagingData.setTareSecondary(value);
				}
			}
		}
	}

	public void addTareTertiary(List<NodeRef> variantNodeRefs, BigDecimal value) {
		if (value != null) {
			for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
				if (variantPackagingData.getTareTertiary() != null) {
					variantPackagingData.setTareTertiary(variantPackagingData.getTareTertiary().add(value));
				} else {
					variantPackagingData.setTareTertiary(value);
				}
			}
		}
	}

	public void setProductPerBoxes(List<NodeRef> variantNodeRefs, Integer value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setProductPerBoxes(value);
		}
	}

	public void setBoxesPerPallet(List<NodeRef> variantNodeRefs, Integer value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setBoxesPerPallet(value);
		}
	}
	
	public void setPalletNumberOnGround(List<NodeRef> variantNodeRefs, Integer value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setPalletNumberOnGround(value);
		}
	}
}
