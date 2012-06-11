/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * The Class AbstractIng.
 *
 * @author querephi
 */
public abstract class AbstractIng implements Ing, Comparable<Ing> {

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
}
