package fr.becpg.model;

/**
 * What a supplier may do with a document of a given type outside a task
 * (<code>bcpg:docTypeSupplierUpdateMode</code>). An unset mode keeps the
 * historical behaviour: every new version waits for validation.
 *
 * @author matthieu
 */
public enum DocumentSupplierUpdateMode {
	/** In force at once, nobody is told. */
	Free,
	/** In force at once, listed in the daily recap. */
	Notify,
	/** Waits for validation, listed in the daily recap. */
	Validate,
	/** Refused outside a task: the supplier asks the customer for an update. */
	OnRequest
}
