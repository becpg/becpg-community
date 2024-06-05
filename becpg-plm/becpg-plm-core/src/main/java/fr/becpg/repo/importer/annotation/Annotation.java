package fr.becpg.repo.importer.annotation;

import java.util.Objects;

/**
 * <p>Annotation class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class Annotation {

	protected String id;
	protected String attribute;
	protected String targetClass;
	protected String targetKey;
	protected String type;
	protected String key;
	

	/**
	 * <p>Constructor for Annotation.</p>
	 */
	public Annotation() {
		super();
	}

	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getId() {
		return id;
	}

	/**
	 * <p>Setter for the field <code>id</code>.</p>
	 *
	 * @param id a {@link java.lang.String} object.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * <p>Getter for the field <code>attribute</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * <p>Setter for the field <code>attribute</code>.</p>
	 *
	 * @param attribute a {@link java.lang.String} object.
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	/**
	 * <p>Getter for the field <code>targetClass</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getTargetClass() {
		return targetClass;
	}

	/**
	 * <p>Setter for the field <code>targetClass</code>.</p>
	 *
	 * @param targetClass a {@link java.lang.String} object.
	 */
	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}

	/**
	 * <p>Getter for the field <code>type</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getType() {
		return type != null ? type : getTargetClass();
	}


	/**
	 * <p>Setter for the field <code>type</code>.</p>
	 *
	 * @param type a {@link java.lang.String} object.
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * <p>Getter for the field <code>targetKey</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getTargetKey() {
		return targetKey != null ? targetKey : getAttribute();
	}

	/**
	 * <p>Setter for the field <code>targetKey</code>.</p>
	 *
	 * @param targetKey a {@link java.lang.String} object.
	 */
	public void setTargetKey(String targetKey) {
		this.targetKey = targetKey;
	}
	
	/**
	 * <p>Getter for the field <code>key</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getKey() {
		return key != null ? key : getTargetKey();
	}

	/**
	 * <p>Setter for the field <code>key</code>.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 */
	public void setKey(String key) {
		this.key = key;
	}

	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Annotation [id=" + id + ", type=" + type + ", key=" + key + "]";
	}
	

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(attribute, id, key, targetClass, targetKey, type);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Annotation other = (Annotation) obj;
		return Objects.equals(attribute, other.attribute) && Objects.equals(id, other.id) && Objects.equals(key, other.key)
				&& Objects.equals(targetClass, other.targetClass) && Objects.equals(targetKey, other.targetKey) && Objects.equals(type, other.type);
	}

}
