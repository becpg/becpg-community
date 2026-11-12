package fr.becpg.repo.product.formulation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;

/**
 * Composite packaging materials of a material list. A composite (layers that cannot
 * be separated, such as a PET/PE lidding film) is a line with component lines under
 * it. The composite counts as one material: its weight is the sum of its components
 * and its recycled share is their weight-weighted average, while
 * the component lines only detail it and must never be counted on their own.
 *
 * @author matthieu
 */
public final class CompositePackMaterials {

	private static final BigDecimal HUNDRED = BigDecimal.valueOf(100d);

	private CompositePackMaterials() {
		// Static helper
	}

	/**
	 * Whether the line details a composite material instead of standing for a material
	 * of its own.
	 *
	 * @param packMaterial a material line
	 * @return {@code true} for a component line
	 */
	public static boolean isComponent(PackMaterialListDataItem packMaterial) {
		return packMaterial.getParent() != null;
	}

	/**
	 * The materials to count in a list: every line but the components of a composite.
	 *
	 * @param packMaterials the material list, may be {@code null}
	 * @return the mono-material and composite lines
	 */
	public static List<PackMaterialListDataItem> materialsOf(List<PackMaterialListDataItem> packMaterials) {
		List<PackMaterialListDataItem> materials = new ArrayList<>();
		if (packMaterials != null) {
			for (PackMaterialListDataItem packMaterial : packMaterials) {
				if (!isComponent(packMaterial)) {
					materials.add(packMaterial);
				}
			}
		}
		return materials;
	}

	/**
	 * The component lines of a composite.
	 *
	 * @param composite the candidate composite line
	 * @param packMaterials the material list it belongs to
	 * @return its components, empty for a mono-material
	 */
	public static List<PackMaterialListDataItem> componentsOf(PackMaterialListDataItem composite, List<PackMaterialListDataItem> packMaterials) {
		List<PackMaterialListDataItem> components = new ArrayList<>();
		for (PackMaterialListDataItem packMaterial : packMaterials) {
			if (isComponent(packMaterial) && isSameLine(packMaterial.getParent(), composite)) {
				components.add(packMaterial);
			}
		}
		return components;
	}

	/**
	 * Computes every composite line of the list from its components.
	 *
	 * @param packMaterials the material list, may be {@code null}
	 */
	public static void rollUp(List<PackMaterialListDataItem> packMaterials) {
		if (packMaterials == null) {
			return;
		}
		for (PackMaterialListDataItem packMaterial : packMaterials) {
			List<PackMaterialListDataItem> components = componentsOf(packMaterial, packMaterials);
			if (!components.isEmpty()) {
				rollUp(packMaterial, components);
			}
		}
	}

	private static void rollUp(PackMaterialListDataItem composite, List<PackMaterialListDataItem> components) {
		BigDecimal weight = BigDecimal.ZERO;
		BigDecimal recycledWeight = BigDecimal.ZERO;

		for (PackMaterialListDataItem component : components) {
			if (component.getPmlWeight() != null) {
				BigDecimal componentWeight = BigDecimal.valueOf(component.getPmlWeight());
				weight = weight.add(componentWeight);
				recycledWeight = recycledWeight.add(shareOf(componentWeight, component.getPmlRecycledPercentage()));
			}
		}

		composite.setPmlWeight(weight.doubleValue());
		composite.setPmlRecycledPercentage(percentageOf(recycledWeight, weight));
	}

	private static BigDecimal shareOf(BigDecimal weight, Double percentage) {
		if (percentage == null) {
			return BigDecimal.ZERO;
		}
		return weight.multiply(BigDecimal.valueOf(percentage)).divide(HUNDRED, MathContext.DECIMAL64);
	}

	private static Double percentageOf(BigDecimal share, BigDecimal weight) {
		if (weight.signum() == 0) {
			return null;
		}
		return share.multiply(HUNDRED).divide(weight, MathContext.DECIMAL64).doubleValue();
	}

	private static boolean isSameLine(PackMaterialListDataItem parent, PackMaterialListDataItem candidate) {
		if (parent == candidate) {
			return true;
		}
		return (parent.getNodeRef() != null) && Objects.equals(parent.getNodeRef(), candidate.getNodeRef());
	}
}
