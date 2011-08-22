package fr.becpg.config.mapping;

import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;


/**
 * Class that represent the mapping for importing a property or an association of a node
 * 
 * <column id="modifier" attribute="cm:modifier" />
 * <column id="suppliers" attribute="bcpg:supplierAssoc" />.
 *
 * @author querephi
 */
public class AttributeMapping extends AbstractAttributeMapping {

	public AttributeMapping(String id, ClassAttributeDefinition attribute) {
		super(id, attribute);
	}

}
