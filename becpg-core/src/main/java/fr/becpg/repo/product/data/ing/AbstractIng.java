/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;

// TODO: Auto-generated Javadoc
/**
 * The Class AbstractIng.
 *
 * @author querephi
 */
public abstract class AbstractIng implements Ing, Comparable<Ing> {

	/** The name. */
	protected String name;
	
	/** The ml name. */
	protected MLText mlName;
			
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ing.Ing#getName()
	 */
	@Override
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ing.Ing#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		this.name = name;
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
		return mlName.getValue(locale);
	}
	
	/**
	 * Instantiates a new abstract ing.
	 *
	 * @param name the name
	 * @param mlName the ml name
	 */
	public AbstractIng(String name, MLText mlName){
		this.name = name;
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
		
		return Float.compare(ing.getQty(), this.getQty());
	}
}
