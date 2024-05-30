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
	/**
	 * <p>Constructor for EvaporatedDataItem.</p>
	 *
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param rate a {@link java.lang.Double} object.
	 * @param qty a {@link java.lang.Double} object.
	 */
	public EvaporatedDataItem(NodeRef productNodeRef, Double rate) {
		super();
		this.productNodeRef = productNodeRef;
		if (rate == null) {
			this.rate = 100d;
		} else {
			this.rate = rate;
		}
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

	@Override
	public int hashCode() {
		return Objects.hash(productNodeRef, rate);
	}

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
