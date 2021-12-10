package fr.becpg.repo.product.formulation.labeling;

import javax.annotation.Nullable;

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

	private Double qty;

	private Double volume;

	/**
	 * <p>Constructor for EvaporatedDataItem.</p>
	 *
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param rate a {@link java.lang.Double} object.
	 * @param qty a {@link java.lang.Double} object.
	 */
	public EvaporatedDataItem(NodeRef productNodeRef, Double rate, Double qty, Double volume) {
		super();
		this.productNodeRef = productNodeRef;
		this.rate = rate;
		this.qty = qty;
		this.volume = volume;
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
	 * <p>Getter for the field <code>qty</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getQty() {
		return qty;
	}

	public Double getVolume() {
		return volume;
	}

	/**
	 * <p>addQty.</p>
	 *
	 * @param qty a {@link java.lang.Double} object.
	 */
	public void addQty(@Nullable Double qty) {
		if ((this.qty != null) && (qty != null)) {
			this.qty += qty;
		}

	}

	/**
	 * <p>addQty.</p>
	 *
	 * @param qty a {@link java.lang.Double} object.
	 */
	public void addVolume(@Nullable Double volume) {
		if ((this.volume != null) && (volume != null)) {
			this.volume += volume;
		}

	}

}
