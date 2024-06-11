package fr.becpg.repo.glop.model;

import java.util.List;

/**
 * <p>GlopContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class GlopContext {

	private GlopTarget target;
	
	private List<GlopConstraint> constraints;
	
	private Double totalQuantity = 1d;
	
	/**
	 * <p>Constructor for GlopContext.</p>
	 *
	 * @param target a {@link fr.becpg.repo.glop.model.GlopTarget} object
	 * @param constraints a {@link java.util.List} object
	 */
	public GlopContext(GlopTarget target, List<GlopConstraint> constraints) {
		this.target = target;
		this.constraints = constraints;
	}

	/**
	 * <p>Constructor for GlopContext.</p>
	 */
	public GlopContext() {
	}

	/**
	 * <p>Getter for the field <code>target</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.glop.model.GlopTarget} object
	 */
	public GlopTarget getTarget() {
		return target;
	}

	/**
	 * <p>Setter for the field <code>target</code>.</p>
	 *
	 * @param target a {@link fr.becpg.repo.glop.model.GlopTarget} object
	 */
	public void setTarget(GlopTarget target) {
		this.target = target;
	}

	/**
	 * <p>Getter for the field <code>constraints</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	public List<GlopConstraint> getConstraints() {
		return constraints;
	}

	/**
	 * <p>Setter for the field <code>constraints</code>.</p>
	 *
	 * @param constraints a {@link java.util.List} object
	 */
	public void setConstraints(List<GlopConstraint> constraints) {
		this.constraints = constraints;
	}

	/**
	 * <p>Getter for the field <code>totalQuantity</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	public Double getTotalQuantity() {
		return totalQuantity;
	}

	/**
	 * <p>Setter for the field <code>totalQuantity</code>.</p>
	 *
	 * @param totalQuantity a {@link java.lang.Double} object
	 */
	public void setTotalQuantity(Double totalQuantity) {
		this.totalQuantity = totalQuantity;
	}
	
}
