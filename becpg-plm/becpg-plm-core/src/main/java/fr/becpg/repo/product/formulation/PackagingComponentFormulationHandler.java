package fr.becpg.repo.product.formulation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.PackMaterialListDataItem;
import fr.becpg.repo.product.data.productList.PackagingComponentListDataItem;

/**
 * <p>
 * Derives the tare and the packaging materials of a packaging entity from the components it is
 * made of.
 * </p>
 * <p>
 * A component carries its own weight: the hierarchy describes an assembly, not a sum, so a
 * parent line is added like any other. Lines declared as {@link DeclarationType#Omit} are left
 * out, which is how a reusable or returnable component is excluded.
 * </p>
 * <p>
 * The handler is inert as long as no component is declared, so a packaging whose materials are
 * entered by hand keeps behaving exactly as before.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PackagingComponentFormulationHandler extends FormulationBaseHandler<ProductData> {

	private static final Log logger = LogFactory.getLog(PackagingComponentFormulationHandler.class);

	private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100d);

	private static final BigDecimal KG_TO_G = BigDecimal.valueOf(1000d);

	/**
	 * Weight of a material and the share of it that comes from recycled matter, both in kg.
	 *
	 * @param weight a {@link java.math.BigDecimal} object
	 * @param recycledWeight a {@link java.math.BigDecimal} object
	 */
	private record MaterialAmount(BigDecimal weight, BigDecimal recycledWeight) {

		MaterialAmount add(MaterialAmount other) {
			return new MaterialAmount(weight.add(other.weight), recycledWeight.add(other.recycledWeight));
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean process(ProductData formulatedProduct) {

		if (!shouldProcess(formulatedProduct)) {
			return true;
		}

		List<PackagingComponentListDataItem> components = declaredComponents(formulatedProduct);

		BigDecimal totalWeight = totalWeight(components);

		if (logger.isDebugEnabled()) {
			logger.debug("Packaging components of " + formulatedProduct.getName() + ": " + components.size() + " line(s), " + totalWeight + " kg");
		}

		applyTare(formulatedProduct, totalWeight);
		applyMaterials(formulatedProduct, aggregateMaterials(components), totalWeight);

		return true;
	}

	/**
	 * <p>shouldProcess.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a boolean
	 */
	private boolean shouldProcess(ProductData formulatedProduct) {
		if (formulatedProduct.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL) || (formulatedProduct instanceof ProductSpecificationData)) {
			return false;
		}
		List<PackagingComponentListDataItem> componentList = formulatedProduct.getPackagingComponentList();
		return (componentList != null) && !componentList.isEmpty();
	}

	/**
	 * <p>declaredComponents.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @return a {@link java.util.List} object
	 */
	private List<PackagingComponentListDataItem> declaredComponents(ProductData formulatedProduct) {
		List<PackagingComponentListDataItem> declared = new ArrayList<>();
		for (PackagingComponentListDataItem component : formulatedProduct.getPackagingComponentList()) {
			if (!DeclarationType.Omit.equals(component.getDeclType())) {
				declared.add(component);
			}
		}
		return declared;
	}

	/**
	 * <p>totalWeight.</p>
	 *
	 * @param components a {@link java.util.List} object
	 * @return the weight of every component, in kg
	 */
	private BigDecimal totalWeight(List<PackagingComponentListDataItem> components) {
		BigDecimal total = BigDecimal.ZERO;
		for (PackagingComponentListDataItem component : components) {
			total = total.add(componentWeight(component));
		}
		return total;
	}

	/**
	 * <p>componentWeight.</p>
	 *
	 * @param component a {@link fr.becpg.repo.product.data.productList.PackagingComponentListDataItem} object
	 * @return the weight of one component line, in kg
	 */
	private BigDecimal componentWeight(PackagingComponentListDataItem component) {
		BigDecimal unitWeight = FormulationHelper.getTareInKg(component.getTare(), component.getTareUnit());
		if (unitWeight == null) {
			return BigDecimal.ZERO;
		}
		Double qty = component.getQty();
		return unitWeight.multiply(BigDecimal.valueOf((qty == null) || (qty == 0d) ? 1d : qty));
	}

	/**
	 * <p>aggregateMaterials.</p>
	 *
	 * @param components a {@link java.util.List} object
	 * @return a {@link java.util.Map} object
	 */
	private Map<NodeRef, MaterialAmount> aggregateMaterials(List<PackagingComponentListDataItem> components) {
		Map<NodeRef, MaterialAmount> amounts = new LinkedHashMap<>();
		for (PackagingComponentListDataItem component : components) {
			if (component.getMaterial() == null) {
				continue;
			}
			MaterialAmount amount = materialAmount(component);
			amounts.merge(component.getMaterial(), amount, MaterialAmount::add);
		}
		return amounts;
	}

	/**
	 * <p>materialAmount.</p>
	 *
	 * @param component a {@link fr.becpg.repo.product.data.productList.PackagingComponentListDataItem} object
	 * @return a {@link fr.becpg.repo.product.formulation.PackagingComponentFormulationHandler.MaterialAmount} object
	 */
	private MaterialAmount materialAmount(PackagingComponentListDataItem component) {
		BigDecimal weight = componentWeight(component);
		Double recycledPerc = component.getRecycledPerc();
		BigDecimal recycledWeight = recycledPerc == null ? BigDecimal.ZERO
				: weight.multiply(BigDecimal.valueOf(recycledPerc)).divide(ONE_HUNDRED, MathContext.DECIMAL64);
		return new MaterialAmount(weight, recycledWeight);
	}

	/**
	 * <p>applyTare.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param totalWeight a {@link java.math.BigDecimal} object
	 */
	private void applyTare(ProductData formulatedProduct, BigDecimal totalWeight) {
		if (totalWeight.doubleValue() == 0d) {
			return;
		}
		if (totalWeight.doubleValue() < 1d) {
			formulatedProduct.setTare(totalWeight.multiply(KG_TO_G).doubleValue());
			formulatedProduct.setTareUnit(TareUnit.g);
		} else {
			formulatedProduct.setTare(totalWeight.doubleValue());
			formulatedProduct.setTareUnit(TareUnit.kg);
		}
	}

	/**
	 * <p>applyMaterials.</p>
	 *
	 * @param formulatedProduct a {@link fr.becpg.repo.product.data.ProductData} object
	 * @param amounts a {@link java.util.Map} object
	 * @param totalWeight a {@link java.math.BigDecimal} object
	 */
	private void applyMaterials(ProductData formulatedProduct, Map<NodeRef, MaterialAmount> amounts, BigDecimal totalWeight) {

		if (formulatedProduct.getPackMaterialList() == null) {
			formulatedProduct.setPackMaterialList(new ArrayList<>());
		}

		Map<NodeRef, MaterialAmount> toCreate = new LinkedHashMap<>(amounts);
		List<PackMaterialListDataItem> toRemove = new ArrayList<>();

		for (PackMaterialListDataItem material : formulatedProduct.getPackMaterialList()) {
			if (Boolean.TRUE.equals(material.getIsManual())) {
				toCreate.remove(material.getPmlMaterial());
			} else if (toCreate.containsKey(material.getPmlMaterial())) {
				updateMaterial(material, toCreate.remove(material.getPmlMaterial()), totalWeight);
			} else {
				toRemove.add(material);
			}
		}

		formulatedProduct.getPackMaterialList().removeAll(toRemove);

		for (Map.Entry<NodeRef, MaterialAmount> entry : toCreate.entrySet()) {
			PackMaterialListDataItem material = PackMaterialListDataItem.build().withMaterial(entry.getKey());
			updateMaterial(material, entry.getValue(), totalWeight);
			formulatedProduct.getPackMaterialList().add(material);
		}
	}

	/**
	 * <p>updateMaterial.</p>
	 *
	 * @param material a {@link fr.becpg.repo.product.data.productList.PackMaterialListDataItem} object
	 * @param amount a {@link fr.becpg.repo.product.formulation.PackagingComponentFormulationHandler.MaterialAmount} object
	 * @param totalWeight a {@link java.math.BigDecimal} object
	 */
	private void updateMaterial(PackMaterialListDataItem material, MaterialAmount amount, BigDecimal totalWeight) {
		material.setPmlWeight(amount.weight().multiply(KG_TO_G).doubleValue());
		material.setPmlPerc(sharePerc(amount.weight(), totalWeight));
		material.setPmlRecycledPercentage(sharePerc(amount.recycledWeight(), amount.weight()));
	}

	/**
	 * <p>sharePerc.</p>
	 *
	 * @param part a {@link java.math.BigDecimal} object
	 * @param total a {@link java.math.BigDecimal} object
	 * @return a {@link java.lang.Double} object
	 */
	private Double sharePerc(BigDecimal part, BigDecimal total) {
		if ((total == null) || (total.doubleValue() == 0d)) {
			return null;
		}
		return part.divide(total, MathContext.DECIMAL64).multiply(ONE_HUNDRED).doubleValue();
	}

}
