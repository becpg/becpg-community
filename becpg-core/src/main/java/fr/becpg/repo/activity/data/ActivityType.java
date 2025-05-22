package fr.becpg.repo.activity.data;

/**
 * <p>ActivityType class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
/**
 * Activity types representing different types of operations in the system.
 */
public enum ActivityType {
	/**
	 * Represents state changes in entities.
	 */
	State,
	/**
	 * Represents entity-related activities.
	 */
	Entity,
	/**
	 * Represents datalist-related activities.
	 */
	Datalist,
	/**
	 * Represents datalist copy operations.
	 */
	DatalistCopy,
	/**
	 * Represents formulation-related activities.
	 */
	Formulation,
	/**
	 * Represents report-related activities.
	 */
	Report,
	/**
	 * Represents comment-related activities.
	 */
	Comment,
	/**
	 * Represents content-related activities.
	 */
	Content,
	/**
	 * Represents merge operations.
	 */
	Merge,
	/**
	 * Represents version-related activities.
	 */
	Version,
	/**
	 * Represents export operations.
	 */
	Export,
	/**
	 * Represents change order operations.
	 */
	ChangeOrder,
	/**
	 * Represents addition of aspects.
	 */
	AspectsAddition,
	/**
	 * Represents removal of aspects.
	 */
	AspectsRemoval
}
