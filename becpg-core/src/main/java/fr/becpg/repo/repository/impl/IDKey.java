package fr.becpg.repo.repository.impl;


/**
 * Wrap an identity key (System.identityHashCode())
 */
public class IDKey {
	private Object value = null;
	private int id = 0;

	/**
	 * Constructor for IDKey
	 * 
	 * @param _value
	 */
	public IDKey(Object _value) {
		// This is the Object hashcode
		id = System.identityHashCode(_value);
		// There have been some cases (bug 11706) that return the
		// same identity hash code for different objects. So
		// the value is also added to disambiguate these cases.
		value = _value;
	}

	/**
	 * returns hashcode
	 * 
	 * @return
	 */
	public int hashCode() {
		return id;
	}

	/**
	 * checks if instances are equal
	 * 
	 * @param other
	 * @return
	 */
	public boolean equals(Object other) {
		if (!(other instanceof IDKey)) {
			return false;
		}
		IDKey idKey = (IDKey) other;
		if (id != idKey.id) {
			return false;
		}
		// Note that identity equals is used.
		return value == idKey.value;
	}
}