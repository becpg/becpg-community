/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.service.cmr.repository.MLText;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.RepoConsts;

// TODO: Auto-generated Javadoc
/**
 * The Class CompositeIng.
 *
 * @author querephi
 */
public class CompositeIng extends AbstractIng {

	/** The Constant SPACE. */
	public static final String SPACE = " ";
	
	/** The Constant LEFT_PARENTHESES. */
	public static final String LEFT_PARENTHESES = "(";
	
	/** The Constant RIGHT_PARENTHESES. */
	public static final String RIGHT_PARENTHESES = ")";
	
	/** The Constant QTY_FORMAT. */
	public static final String  QTY_FORMAT = "0.00";
	
	/** The Constant PERCENTAGE. */
	public static final String  PERCENTAGE = "%";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CompositeIng.class);
	
	/** The ing list. */
	private Map<String, AbstractIng> ingList = new HashMap<String, AbstractIng>();
	
	/** The ing list not declared. */
	private Map<String, AbstractIng> ingListNotDeclared = new HashMap<String, AbstractIng>();	

	/* (non-Javadoc)
	 * @see fr.becpg.repo.food.ing.Ing#getQty()
	 */
	@Override
	public float getQty() {
		float qty = 0f;
		
		for(Ing ing : ingList.values())
			qty += ing.getQty();
		
		for(Ing ing : ingListNotDeclared.values())
			qty += ing.getQty();
		
		return qty;
	}
	
	/**
	 * Adds the.
	 *
	 * @param ing the ing
	 * @param isDeclared the is declared
	 */
	public void add(AbstractIng ing, boolean isDeclared){
		if(isDeclared)		
			ingList.put(ing.getName(), ing);
		else
			ingListNotDeclared.put(ing.getName(), ing);
	}
	
	/**
	 * Removes the.
	 *
	 * @param name the name
	 * @param isDeclared the is declared
	 */
	public void remove(String name, boolean isDeclared){
		if(isDeclared)		
			ingList.remove(name);
		else
			ingListNotDeclared.remove(name);
	}
	
	/**
	 * Gets the.
	 *
	 * @param name the name
	 * @param isDeclared the is declared
	 * @return the abstract ing
	 */
	public AbstractIng get(String name, boolean isDeclared){
		
		AbstractIng ing = null;		
		if(isDeclared)		
			ing = ingList.get(name);
		else
			ing = ingListNotDeclared.get(name);
		
		return ing;
	}
	
	/**
	 * Gets the ing list.
	 *
	 * @return the ing list
	 */
	public Map<String, AbstractIng> getIngList(){
		return ingList;
	}
	
	/**
	 * Gets the ing list not declared.
	 *
	 * @return the ing list not declared
	 */
	public Map<String, AbstractIng> getIngListNotDeclared() {
		return ingListNotDeclared;
	}
	
	/**
	 * Instantiates a new composite ing.
	 *
	 * @param name the name
	 * @param mlName the ml name
	 */
	public CompositeIng(String name, MLText mlName){
		super(name, mlName);		
	}
		
	/**
	 * Gets the ing labeling.
	 *
	 * @param locale the locale
	 * @return the ing labeling
	 */
	public String getIngLabeling(Locale locale){
		
		logger.debug("getIngLabeling(), ing name: " + this.getName() + "- ing list size: " + ingList.values().size());
		
		String ingredients = "";
		float totalQty = getQty();				
		List<AbstractIng> sortedIngList = new ArrayList<AbstractIng>(ingList.values());
		Collections.sort(sortedIngList);
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(locale);
		df.applyPattern(QTY_FORMAT);
		
		for(AbstractIng ing : sortedIngList){
						
			String qtyPerc = "";
			if(ing.getQty() != 0f){
				qtyPerc = SPACE + df.format(100 * ing.getQty() / totalQty) + SPACE + PERCENTAGE;
			}
			
			String ingName = ing.getName(locale);
			
			if(ingName == null){
				logger.warn("Null locale name: " + ing.getName());
			}
			
			if(!ingredients.isEmpty()){
				ingredients += RepoConsts.LABEL_SEPARATOR;
			}
			if(ing instanceof IngItem){				
				ingredients += ingName + qtyPerc;
			}
			else if(ing instanceof CompositeIng){
				String subIngredients = ((CompositeIng)ing).getIngLabeling(locale);
				logger.trace("subIngredients: " + subIngredients);
				ingredients += ingName + qtyPerc + SPACE + LEFT_PARENTHESES + subIngredients + RIGHT_PARENTHESES;
			}
			else{
				logger.error("Unsupported ing type. Name: " + ing.getName());
			}
		}
		
		logger.trace("getIngLabeling(), ingredients: " + ingredients);
		return ingredients;
	}	
}
