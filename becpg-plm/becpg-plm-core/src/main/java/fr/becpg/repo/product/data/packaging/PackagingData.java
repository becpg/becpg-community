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
	private final Map<NodeRef, VariantPackagingData> variants = new HashMap<>();

	private Collection<VariantPackagingData> getVariantPackagingData(List<NodeRef> variantNodeRefs) {
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
	
	public void addTarePrimary(List<NodeRef> variantNodeRefs, BigDecimal value) {
		if (value != null) {
			for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
				if (variantPackagingData.getTarePrimary() != null) {
					variantPackagingData.setTarePrimary(variantPackagingData.getTarePrimary().add(value));
				} else {
					variantPackagingData.setTarePrimary(value);
				}
			}
		}
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

	public void setPalletHeight(List<NodeRef> variantNodeRefs, Integer value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setPalletHeight(value);
		}
		
	}

	public void setPalletBoxesPerLayer(List<NodeRef> variantNodeRefs, Integer value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setPalletBoxesPerLayer(value);
		}
		
	}

	public void setPalletLayers(List<NodeRef> variantNodeRefs, Integer value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setPalletLayers(value);
		}
		
	}

	public void setTertiaryDepth(List<NodeRef> variantNodeRefs, Double value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setTertiaryDepth(value);
		}
		
	}

	public void setTertiaryWidth(List<NodeRef> variantNodeRefs, Double value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setTertiaryWidth(value);
		}
	}

	public void setWidth(List<NodeRef> variantNodeRefs, Double value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setWidth(value);
		}
		
	}

	public void setHeight(List<NodeRef> variantNodeRefs, Double value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setHeight(value);
		}
		
	}

	public void setDepth(List<NodeRef> variantNodeRefs, Double value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setDepth(value);
		}
		
	}

	public void setSecondaryWidth(List<NodeRef> variantNodeRefs, Double value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setSecondaryWidth(value);
		}
		
	}

	public void setSecondaryHeight(List<NodeRef> variantNodeRefs, Double value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setSecondaryHeight(value);
		}
		
	}

	public void setSecondaryDepth(List<NodeRef> variantNodeRefs, Double value) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setSecondaryDepth(value);
		}
		
	}

	public void setBoxesPerLastLayer(List<NodeRef> variantNodeRefs, Integer palletBoxesPerLastLayer) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(variantNodeRefs)) {
			variantPackagingData.setPalletBoxesPerLastLayer(palletBoxesPerLastLayer);
		}
		
	}

	public void setStackingMaxWeight(List<NodeRef> currentVariants, Integer palletStackingMaxWeight) {
		for (VariantPackagingData variantPackagingData : getVariantPackagingData(currentVariants)) {
			variantPackagingData.setPalletStackingMaxWeight(palletStackingMaxWeight);
		}
		
	}
}
