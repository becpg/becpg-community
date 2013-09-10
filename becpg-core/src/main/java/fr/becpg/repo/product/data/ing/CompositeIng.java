/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.RepoConsts;

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
	
	public static final String LABEL_ING_TYPE_SEPARATOR = " : ";
	
	/** The Constant QTY_FORMAT. */
	public static final String  QTY_FORMAT = "0.00";
	
	/** The Constant PERCENTAGE. */
	public static final String  PERCENTAGE = "%";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CompositeIng.class);
	
	/** The ing list. */
	private Map<NodeRef, AbstractIng> ingList = new LinkedHashMap<NodeRef, AbstractIng>();
	
	/** The ing list not declared. */
	private Map<NodeRef, AbstractIng> ingListNotDeclared = new LinkedHashMap<NodeRef, AbstractIng>();	
	
	private Double qtyRMUsed = 0d;
	
	public Double getQtyRMUsed() {
		return qtyRMUsed;		
	}

	public void setQtyRMUsed(Double qtyRMUsed) {
		this.qtyRMUsed = qtyRMUsed;
	}
	
	/**
	 * Adds the.
	 *
	 * @param ing the ing
	 * @param isDeclared the is declared
	 */
	public void add(AbstractIng ing, boolean isDeclared){
		if(isDeclared)		
			ingList.put(ing.getIng(), ing);
		else
			ingListNotDeclared.put(ing.getIng(), ing);
	}
	
	/**
	 * Removes the.
	 *
	 * @param name the name
	 * @param isDeclared the is declared
	 */
	public void remove(NodeRef ing, boolean isDeclared){
		if(isDeclared)		
			ingList.remove(ing);
		else
			ingListNotDeclared.remove(ing);
	}
	
	/**
	 * Gets the.
	 *
	 * @param grpNodeRef nodeRef of the group
	 * @param isDeclared the is declared
	 * @return the abstract ing
	 */
	public AbstractIng get(NodeRef grpNodeRef, boolean isDeclared){
		
		AbstractIng ing = null;		
		if(isDeclared)		
			ing = ingList.get(grpNodeRef);
		else
			ing = ingListNotDeclared.get(grpNodeRef);
		
		return ing;
	}
	
	/**
	 * Gets the ing list.
	 *
	 * @return the ing list
	 */
	public Map<NodeRef, AbstractIng> getIngList(){
		return ingList;
	}
	
	/**
	 * Gets the ing list not declared.
	 *
	 * @return the ing list not declared
	 */
	public Map<NodeRef, AbstractIng> getIngListNotDeclared() {
		return ingListNotDeclared;
	}
	
	/**
	 * Instantiates a new composite ing.
	 *
	 * @param ing the nodeRef of the ing
	 * @param mlName the ml name
	 */
	public CompositeIng(NodeRef ing, MLText mlName, Double qty, String ingType){
		super(ing, mlName, qty, ingType);
	}
	
	public Set<Locale> getLocales(){
		
		Set<Locale> temp = new HashSet<Locale>();
		
		for(AbstractIng ing : ingList.values()){
			
			if(ing.getMLName() != null){
				temp.addAll(ing.getMLName().getLocales());
			}				
		}
		
		Set<Locale> locales = new HashSet<Locale>();		
		for(Locale locale : temp){
			locales.add(new Locale(locale.getLanguage()));
		}		
		return locales;
	}
		
	/**
	 * Gets the ing labeling.
	 *
	 * @param locale the locale
	 * @return the ing labeling
	 */
	public String getIngLabeling(Locale locale){				
		
		String ingredients = "";
		List<AbstractIng> sortedIngList = new ArrayList<AbstractIng>(ingList.values());
		Collections.sort(sortedIngList);
		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(locale);
		df.applyPattern(QTY_FORMAT);
		Map<String, List<AbstractIng>> sortedIngListByType = new LinkedHashMap<String, List<AbstractIng>>();
		
		for(AbstractIng ing : sortedIngList){
			List<AbstractIng> subSortedList = sortedIngListByType.get(ing.getIngType());
			
			if(subSortedList == null){
				subSortedList = new LinkedList<AbstractIng>(); 
				sortedIngListByType.put(ing.getIngType(), subSortedList);
			}
			subSortedList.add(ing);
		}
		
		for(Map.Entry<String, List<AbstractIng>> kv : sortedIngListByType.entrySet()){
			
			boolean addLabelSeparator = true;
			
			if(kv.getKey() != null){
				
				if(!ingredients.isEmpty()){
					ingredients += RepoConsts.LABEL_SEPARATOR;
				}
				
				ingredients += kv.getKey() + LABEL_ING_TYPE_SEPARATOR;
				addLabelSeparator = false;
			}
								
			for(AbstractIng ing : kv.getValue()){
				
				String qtyPerc = "";
				String ingName = ing.getName(locale);
				
				if(ing.getQty() != null && ing.getQty() != 0d){
					//Double ingQty = (ing instanceof CompositeIng) ? ((CompositeIng)ing).getQtyUsed() : ing.getQty();
					qtyPerc = SPACE + df.format(100 * ing.getQty() / getQtyRMUsed()) + SPACE + PERCENTAGE;
				}				
				
				if(logger.isDebugEnabled()){
					logger.debug(ingName + " qtyRMUsed: " + getQtyRMUsed() + " qtyPerc " + qtyPerc);
				}
				
				if(ingName == null){
					logger.warn("Ing '" + ing.getIng() + "' doesn't have a value for this locale '" + locale + "'.");
				}
				
				if(!ingredients.isEmpty()){
					if(addLabelSeparator){
						ingredients += RepoConsts.LABEL_SEPARATOR;
					}
					else{
						addLabelSeparator = true;
					}
				}
				if(ing instanceof IngItem){				
					ingredients += ingName + qtyPerc;
				}
				else if(ing instanceof CompositeIng){
					String subIngredients = ((CompositeIng)ing).getIngLabeling(locale);
					ingredients += ingName + qtyPerc + SPACE + LEFT_PARENTHESES + subIngredients + RIGHT_PARENTHESES;
				}
				else{
					logger.error("Unsupported ing type. Name: " + ing.getIng());
				}
			}
		}
		
		if(logger.isDebugEnabled()){
			logger.debug("getIngLabeling(), ing: " + this.getName(I18NUtil.getContentLocaleLang()) + "- ing list size: " + ingList.values().size() + " - getQtyRMUsed: " + getQtyRMUsed());
			logger.debug("getIngLabeling(), ingredients: " + ingredients);
		}
				
		return ingredients;
	}
}
