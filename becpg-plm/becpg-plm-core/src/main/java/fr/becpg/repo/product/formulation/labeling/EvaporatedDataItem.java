package fr.becpg.repo.product.formulation.labeling;

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
		this.rate = rate;
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



}
