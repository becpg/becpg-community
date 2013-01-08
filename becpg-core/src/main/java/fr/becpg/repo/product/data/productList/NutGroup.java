/*
 * 
 */
package fr.becpg.repo.product.data.productList;

// TODO: Auto-generated Javadoc
/**
 * The Enum NutGroup.
 *
 * @author querephi
 */
public enum NutGroup  {
	
	//Order is important for compareTo
	
	/** The Group1. */
	Group1,
	
	/** The Group2. */
	Group2,
	
	/** The Other. */
	Other;
	
	//FR
	/** The Constant GROUP1_FR. */
	public static final String GROUP1_FR = "Groupe 1";
	
	/** The Constant GROUP2_FR. */
	public static final String GROUP2_FR = "Groupe 2";
	
	/** The Constant OTHER_FR. */
	public static final String OTHER_FR = "Autre";
	
	//EN
	/** The Constant GROUP1_EN. */
	public static final String GROUP1_EN = "Group 1";
	
	/** The Constant GROUP2_EN. */
	public static final String GROUP2_EN = "Group 2";
	
	/** The Constant OTHER_EN. */
	public static final String OTHER_EN = "Other";
	
	/**
	 * Parses the.
	 *
	 * @param nutGroup the nut group
	 * @return the nut group
	 */
	public static NutGroup parse(String nutGroup){
		
		NutGroup group = NutGroup.Other;
		
		if(nutGroup!=null){
			if(nutGroup.equals(GROUP1_FR) || nutGroup.equals(GROUP1_EN)){
				group = NutGroup.Group1;
			}
			else if(nutGroup.equals(GROUP2_FR) || nutGroup.equals(GROUP2_EN)){
				group = NutGroup.Group2;
			}
		}
		
		return group;
	}	
	
	
}
