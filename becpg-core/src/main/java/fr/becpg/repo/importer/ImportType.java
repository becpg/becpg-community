package fr.becpg.repo.importer;

/**
 * Define the type of import.
 *
 * @author querephi
 */
public enum ImportType {

	/** The node with QName properties and associations */
	Node, 
	/** The entity datalists as compoList, ingList (not characteristics) */
	EntityListItem,
	/** The entity with characteristics (costs, nuts, etc...) */
	EntityListAspect, 
	/** Add Comments to product */
	Comments
}
