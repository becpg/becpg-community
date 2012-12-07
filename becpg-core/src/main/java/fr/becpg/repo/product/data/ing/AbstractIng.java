/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.model.BaseObject;

/**
 * The Class AbstractIng.
 *
 * @author querephi
 */
public abstract class AbstractIng extends BaseObject implements Ing, Comparable<Ing> {

	/** The name. */
	protected NodeRef ing;
	
	/** The ml name. */
	protected MLText mlName;
			
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ing.Ing#getName()
	 */
	@Override
	public NodeRef getIng() {
		return ing;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ing.Ing#setName(java.lang.String)
	 */
	@Override
	public void setIng(NodeRef ing) {
		this.ing = ing;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ing.Ing#getMLName()
	 */
	@Override
	public MLText getMLName() {
		return mlName;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ing.Ing#setMLName(org.alfresco.service.cmr.repository.MLText)
	 */
	@Override
	public void setMLName(MLText mlName) {
		this.mlName = mlName;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ing.Ing#getName(java.util.Locale)
	 */
	@Override
	public String getName(Locale locale) {
		if(mlName!=null){
			return mlName.getValue(locale);
		}
		return "";
	}
	
	/**
	 * Instantiates a new abstract ing.
	 *
	 * @param name the name
	 * @param mlName the ml name
	 */
	public AbstractIng(NodeRef ing, MLText mlName){
		this.ing = ing;
		this.mlName = mlName;
	}
	
	/**
	 * Sort by qty in descending order.
	 *
	 * @param ing the ing
	 * @return the int
	 * @author querephi
	 */
	@Override
	public int compareTo(Ing ing) {
		return Double.compare(ing.getQty(), this.getQty());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ing == null) ? 0 : ing.hashCode());
		result = prime * result + ((mlName == null) ? 0 : mlName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractIng other = (AbstractIng) obj;
		if (ing == null) {
			if (other.ing != null)
				return false;
		} else if (!ing.equals(other.ing))
			return false;
		if (mlName == null) {
			if (other.mlName != null)
				return false;
		} else if (!mlName.equals(other.mlName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractIng [ing=" + ing + ", mlName=" + mlName + "]";
	}
	
	
}
