package fr.becpg.repo.product.formulation.labeling;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>EvaporatedDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EvaporatedDataItem {

	private NodeRef productNodeRef;

	private Double rate;
	
	private Double maxEvaporableQty;
	private Double maxEvaporableVolume;

	private boolean heldByParent = false;
	/**
	 * <p>Constructor for EvaporatedDataItem.</p>
	 *
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param rate a {@link java.lang.Double} object.
	 * @param maxEvaporableQty a {@link java.lang.Double} object
	 * @param maxEvaporableVolume a {@link java.lang.Double} object
	 */
	public EvaporatedDataItem(NodeRef productNodeRef, Double rate, Double maxEvaporableQty, Double maxEvaporableVolume) {
		super();
		this.productNodeRef = productNodeRef;
		if (rate == null) {
			this.rate = 100d;
		} else {
			this.rate = rate;
		}
		this.maxEvaporableQty = maxEvaporableQty;
		this.maxEvaporableVolume =maxEvaporableVolume;
	}

	/**
	 * <p>Getter for the field <code>productNodeRef</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getProductNodeRef() {
		return productNodeRef;
	}
	/**
	 * <p>Getter for the field <code>rate</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getRate() {
		return rate;
	}


	/**
	 * <p>Getter for the field <code>maxEvaporableQty</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getMaxEvaporableQty() {
		return maxEvaporableQty;
	}

	/**
	 * <p>Setter for the field <code>maxEvaporableQty</code>.</p>
	 *
	 * @param maxEvaporableQty a {@link java.lang.Double} object
	 */
	public void setMaxEvaporableQty(Double maxEvaporableQty) {
		this.maxEvaporableQty = maxEvaporableQty;
	}


	/**
	 * <p>Getter for the field <code>maxEvaporableVolume</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getMaxEvaporableVolume() {
		return maxEvaporableVolume;
	}

	/**
	 * <p>Setter for the field <code>maxEvaporableVolume</code>.</p>
	 *
	 * @param maxEvaporableVolume a {@link java.lang.Double} object
	 */
	public void setMaxEvaporableVolume(Double maxEvaporableVolume) {
		this.maxEvaporableVolume = maxEvaporableVolume;
	}

	/**
	 * Tells whether the evaporating ingredient is not shown on the label and loses its water through the composite
	 * ingredient holding it, in which case the max evaporable quantities are the ingredient own ones.
	 *
	 * @return true when the composite ingredient holding the ingredient carries its loss
	 */
	public boolean isHeldByParent() {
		return heldByParent;
	}

	/**
	 * Marks the evaporating ingredient as carried by the composite ingredient holding it.
	 *
	 * @param heldByParent true when the composite ingredient holding the ingredient carries its loss
	 */
	public void setHeldByParent(boolean heldByParent) {
		this.heldByParent = heldByParent;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(productNodeRef, rate);
	}
	
	

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EvaporatedDataItem other = (EvaporatedDataItem) obj;
		return Objects.equals(productNodeRef, other.productNodeRef) && Objects.equals(rate, other.rate);
	}
	
}
