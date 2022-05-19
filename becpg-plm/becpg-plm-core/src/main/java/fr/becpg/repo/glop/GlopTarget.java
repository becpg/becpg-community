package fr.becpg.repo.glop;

import org.json.JSONException;
import org.json.JSONObject;

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
	 * Add the target task into a JSON representation. Intended to be used by
	 * {@link fr.becpg.repo.glop.impl.GlopServiceImpl}.
	 *
	 * @param obj
	 *            the JSON to add the target to
	 * @throws org.json.JSONException
	 *             if the operation failed
	 */
	public void putIntoJson(JSONObject obj) throws JSONException {
		obj.put("task", task);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "GlopTargetSpecification [target=" + target + ", task=" + task + "]";
	}

}
