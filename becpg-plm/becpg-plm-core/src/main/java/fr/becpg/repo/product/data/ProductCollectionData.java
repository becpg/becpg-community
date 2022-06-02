package fr.becpg.repo.product.data;

import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.SystemState;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.StateableEntity;

/**
 * <p>ProductCollectionData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:productCollection")
public class ProductCollectionData extends BeCPGDataObject implements HierarchicalEntity, StateableEntity {

	private static final long serialVersionUID = -2554133542406623412L;
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private SystemState state = SystemState.Simulation;

	
	/**
	 * <p>Getter for the field <code>state</code>.</p>
	 *
	 * @return a {@link fr.becpg.model.SystemState} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:productCollectionState")
	public SystemState getState() {
		return state;
	}

	/**
	 * <p>Setter for the field <code>state</code>.</p>
	 *
	 * @param state a {@link fr.becpg.model.SystemState} object.
	 */
	public void setState(SystemState state) {
		this.state = state;
	}

	/** {@inheritDoc} */
	@Override
	public String getEntityState() {
		return state != null ? state.toString() : null;
	}

	/** {@inheritDoc} */
	@AlfProp
	@AlfQname(qname = "bcpg:productCollectionHierarchy1")
	@Override
	public NodeRef getHierarchy1() {
		return hierarchy1;
	}

	/**
	 * <p>Setter for the field <code>hierarchy1</code>.</p>
	 *
	 * @param hierarchy1 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setHierarchy1(NodeRef hierarchy1) {
		this.hierarchy1 = hierarchy1;
	}

	/** {@inheritDoc} */
	@AlfProp
	@AlfQname(qname = "bcpg:productCollectionHierarchy2")
	@Override
	public NodeRef getHierarchy2() {
		return hierarchy2;
	}

	/**
	 * <p>Setter for the field <code>hierarchy2</code>.</p>
	 *
	 * @param hierarchy2 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setHierarchy2(NodeRef hierarchy2) {
		this.hierarchy2 = hierarchy2;
	}


	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(hierarchy1, hierarchy2, state);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductCollectionData other = (ProductCollectionData) obj;
		return Objects.equals(hierarchy1, other.hierarchy1) && Objects.equals(hierarchy2, other.hierarchy2) && state == other.state;
	}

	@Override
	public String toString() {
		return "ProductCollectionData [hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", state=" + state + "]";
	}

}
