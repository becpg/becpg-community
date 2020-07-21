package fr.becpg.repo.importer.annotation;

/**
 * <p>Assoc class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class Assoc extends Annotation {
	private String path;
	
	/**
	 * <p>Constructor for Assoc.</p>
	 */
	public Assoc() {
		super();
	}

	/**
	 * <p>Getter for the field <code>path</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * <p>Setter for the field <code>path</code>.</p>
	 *
	 * @param path a {@link java.lang.String} object.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Assoc [path=" + path + ", id=" + id + ", attribute=" + attribute + ", targetClass=" + targetClass
				+ ", targetKey=" + targetKey + ", type=" + type + ", key=" + key + "]";
	}

	

	
}
