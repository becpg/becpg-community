/*
 * 
 */
package fr.becpg.repo.product.data.productList;

// TODO: Auto-generated Javadoc
/**
 * The Enum DeclarationType.
 *
 * @author querephi
 */
public enum DeclarationType {

	/** The UNKNOWN. */
	UNKNOWN,
	
	/** The DECLARE. */
	DECLARE,
	
	/** The OMIT. */
	OMIT,
	
	/** The DETAIL. */
	DETAIL,
	
	/** The D o_ no t_ declare. */
	DO_NOT_DECLARE;

	//FR
	/** The Constant DECLARE_FR. */
	public static final String DECLARE_FR = "Déclarer";
	
	/** The Constant OMIT_FR. */
	public static final String OMIT_FR = "Omettre";  
	
	/** The Constant DETAIL_FR. */
	public static final String DETAIL_FR = "Détailler";
	
	/** The Constant DO_NOT_DECLARE_FR. */
	public static final String DO_NOT_DECLARE_FR = "Ne pas déclarer";	
	
	//EN
	/** The Constant DECLARE_EN. */
	public static final String DECLARE_EN = "Declare";
	
	/** The Constant OMIT_EN. */
	public static final String OMIT_EN = "Omit";  
	
	/** The Constant DETAIL_EN. */
	public static final String DETAIL_EN = "Detail";
	
	/** The Constant DO_NOT_DECLARE_EN. */
	public static final String DO_NOT_DECLARE_EN = "Do not declare";
	
	/**
	 * Parses the.
	 *
	 * @param declarationType the declaration type
	 * @return the declaration type
	 */
	public static DeclarationType parse(String declarationType){
			
		DeclarationType type = DeclarationType.UNKNOWN;
		
		if(declarationType.equals(DECLARE_FR) || declarationType.equals(DECLARE_EN)){
			type = DeclarationType.DECLARE;
		}
		else if(declarationType.equals(OMIT_FR) || declarationType.equals(OMIT_EN)){
			type = DeclarationType.OMIT;
		}
		else if(declarationType.equals(DETAIL_FR) || declarationType.equals(DETAIL_EN)){
			type = DeclarationType.DETAIL;
		}
		else if(declarationType.equals(DO_NOT_DECLARE_FR) || declarationType.equals(DO_NOT_DECLARE_EN)){
			type = DeclarationType.DO_NOT_DECLARE;
		}
		
		return type;
	}	
}
