package fr.becpg.repo.glop.model;

import fr.becpg.repo.repository.model.SimpleCharactDataItem;

/**
 * Specifies a target function for the Glop service
 *
 * @author pierrecolin
 * @version 1.0
 * @see fr.becpg.repo.glop.GlopService
 */
public class GlopTarget {

	private SimpleCharactDataItem target;

	private String task;

	/**
	 * Builds a new target function specification
	 *
	 * @param target
	 *            the data item involved in the target
	 * @param task
	 *            the task to perform with the generated target function
	 */
	public GlopTarget(SimpleCharactDataItem target, String task) {
		this.target = target;

		if (!(task.equals("min") || task.contentEquals("max"))) {
			throw new IllegalArgumentException("Task must be \"min\" or \"max\"");
		}

		this.task = task;
	}

	/**
	 * Returns the data item in the target specification
	 *
	 * @return the data item
	 */
	public SimpleCharactDataItem getTarget() {
		return target;
	}
	
	/**
	 * <p>Getter for the field <code>task</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getTask() {
		return task;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "GlopTarget [target=" + target + ", task=" + task + "]";
	}

}
