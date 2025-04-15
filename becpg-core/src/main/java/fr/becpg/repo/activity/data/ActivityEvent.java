package fr.becpg.repo.activity.data;

/**
 * <p>ActivityEvent class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
/**
 * Types of activity events that can occur in the system.
 */
public enum ActivityEvent {
	/**
	 * Represents an update operation.
	 */
	Update,
	/**
	 * Represents a creation operation.
	 */
	Create,
	/**
	 * Represents a deletion operation.
	 */
	Delete
}
