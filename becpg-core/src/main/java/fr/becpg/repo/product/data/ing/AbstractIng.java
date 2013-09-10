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
	
	protected Double qty = 0d;
	
	protected String ingType;
			
	@Override
	public NodeRef getIng() {
		return ing;
	}
	
	@Override
	public void setIng(NodeRef ing) {
		this.ing = ing;
	}
	
	@Override
	public MLText getMLName() {
		return mlName;
	}
	
	@Override
	public void setMLName(MLText mlName) {
		this.mlName = mlName;
	}
	
	@Override
	public String getName(Locale locale) {
		if(mlName!=null){
			return mlName.getValue(locale);
		}
		return "";
	}
	
	@Override
	public Double getQty() {
		return qty;
	}		
	
	public void setQty(Double qty) {
		this.qty = qty;
	}
	
	@Override
	public String getIngType() {
		return ingType;
	}

	public void setIngType(String ingType) {
		this.ingType = ingType;
	}

	/**
	 * Instantiates a new abstract ing.
	 *
	 * @param name the name
	 * @param mlName the ml name
	 */
	public AbstractIng(NodeRef ing, MLText mlName, Double qty, String ingType){
		this.ing = ing;
		this.mlName = mlName;
		this.qty = qty;
		this.ingType = ingType;
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
		if(ing.getQty() != null && this.getQty() != null){
			return Double.compare(ing.getQty(), this.getQty());		
		}
		else if(this.getQty() == null && ing.getQty() != null){
			return 1; //after
		}
		else if(this.getQty() != null && ing.getQty() == null){
			return -1; //before
		}
		return 0;//equals
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ing == null) ? 0 : ing.hashCode());
		result = prime * result + ((ingType == null) ? 0 : ingType.hashCode());
		result = prime * result + ((mlName == null) ? 0 : mlName.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
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
		if (ingType == null) {
			if (other.ingType != null)
				return false;
		} else if (!ingType.equals(other.ingType))
			return false;
		if (mlName == null) {
			if (other.mlName != null)
				return false;
		} else if (!mlName.equals(other.mlName))
			return false;
		if (qty == null) {
			if (other.qty != null)
				return false;
		} else if (!qty.equals(other.qty))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractIng [ing=" + ing + ", mlName=" + mlName + ", qty=" + qty + ", ingType=" + ingType + "]";
	}
	
	
}
