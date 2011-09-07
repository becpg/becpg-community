/*
 * 
 */
package fr.becpg.repo.importer;

// TODO: Auto-generated Javadoc
/**
 * Define the type of import.
 *
 * @author querephi
 */
public enum ImportType {

	/** The node with QName properties and associations */
	Node, 
	/** The product with characteristics (costs, nuts, etc...) */
	Product,  
	/** The entity datalists as compoList, ingList (not characteristics) */
	EntityListItem,
	/** The entity list aspect (product template or product microbio criteria), it doesn't inherite from product type */
	EntityListAspect
}
