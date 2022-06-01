package fr.becpg.repo.glop.model;

import java.util.List;

public class GlopContext {

	private GlopTarget target;
	
	private List<GlopConstraint> constraints;
	
	private Double totalQuantity = 1d;
	
	public GlopContext(GlopTarget target, List<GlopConstraint> constraints) {
		this.target = target;
		this.constraints = constraints;
	}

	public GlopContext() {
	}

	public GlopTarget getTarget() {
		return target;
	}

	public void setTarget(GlopTarget target) {
		this.target = target;
	}

	public List<GlopConstraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<GlopConstraint> constraints) {
		this.constraints = constraints;
	}

	public Double getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(Double totalQuantity) {
		this.totalQuantity = totalQuantity;
	}
	
}
