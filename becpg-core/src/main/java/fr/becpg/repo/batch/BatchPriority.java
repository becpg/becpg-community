package fr.becpg.repo.batch;

/**
 * <p>BatchPriority class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public enum BatchPriority {

	VERY_LOW(4),
	LOW(3),
	MEDIUM(2),
	HIGH(1),
	VERY_HIGH(0);

	/**
	 * <p>Constructor for BatchPriority.</p>
	 *
	 * @param priority a int
	 */
	private int priority;

	BatchPriority(int priority) {
		this.priority = priority;
	}
	
	/**
	 * <p>priority.</p>
	 *
	 * @return a int
	 */
	public int priority() {
		return priority;
	}
	
}
