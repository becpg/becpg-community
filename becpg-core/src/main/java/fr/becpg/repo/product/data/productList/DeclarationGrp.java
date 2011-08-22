/*
 * 
 */
package fr.becpg.repo.product.data.productList;

// TODO: Auto-generated Javadoc
/**
 * The Enum DeclarationGrp.
 *
 * @author querephi
 */
public enum DeclarationGrp {

	/** The EMPTY. */
	EMPTY,
	
	/** The ITSELF. */
	ITSELF,
	
	/** The TEXT. */
	TEXT;

	//FR
	/** The Constant ITSELF_FR. */
	public static final String ITSELF_FR = "Lui-même";	
	
	//EN
	/** The Constant ITSELF_EN. */
	public static final String ITSELF_EN = "Itself";
	
	/**
	 * Parses the.
	 *
	 * @param declarationGrp the declaration grp
	 * @return the declaration grp
	 */
	public static DeclarationGrp parse(String declarationGrp){
			
		DeclarationGrp grp = DeclarationGrp.TEXT;
		
		if(declarationGrp == ""){
			grp = DeclarationGrp.EMPTY;
		}
		else if(declarationGrp.equals(ITSELF_FR) || declarationGrp.equals(ITSELF_EN)){
			grp = DeclarationGrp.ITSELF;
		}		
		
		return grp;
	}	
}
