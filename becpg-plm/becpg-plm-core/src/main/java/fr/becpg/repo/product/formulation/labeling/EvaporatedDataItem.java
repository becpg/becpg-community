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
	/**
	 * <p>Constructor for EvaporatedDataItem.</p>
	 *
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param rate a {@link java.lang.Double} object.
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


	public Double getMaxEvaporableQty() {
		return maxEvaporableQty;
	}

	public void setMaxEvaporableQty(Double maxEvaporableQty) {
		this.maxEvaporableQty = maxEvaporableQty;
	}


	public Double getMaxEvaporableVolume() {
		return maxEvaporableVolume;
	}

	public void setMaxEvaporableVolume(Double maxEvaporableVolume) {
		this.maxEvaporableVolume = maxEvaporableVolume;
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
