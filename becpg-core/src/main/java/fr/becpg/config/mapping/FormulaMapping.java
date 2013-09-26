package fr.becpg.config.mapping;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;


/**
 * Class that represent the mapping for importing a property formula or an association of a node
 * 
 * <column id="labelClaimFormula" type="Formula" attribute="bcpg:labelClaimFormula" />
 *
 * @author querephi
 */
public class FormulaMapping extends AbstractAttributeMapping {

	public FormulaMapping(String id, ClassAttributeDefinition attribute) {
		super(id, attribute);
	}

}
