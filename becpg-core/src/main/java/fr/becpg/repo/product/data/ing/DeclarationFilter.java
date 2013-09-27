package fr.becpg.repo.product.data.ing;

import fr.becpg.repo.product.data.productList.DeclarationType;

/**
 * 
 * @author matthieu
 *
 */
public class DeclarationFilter {

	private String formula;
	
	private DeclarationType declarationType;
	

	public DeclarationFilter(String formula, DeclarationType declarationType) {
		super();
		this.formula = formula;
		this.declarationType = declarationType;
	}


	public String getFormula() {
		return formula;
	}


	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	
}
