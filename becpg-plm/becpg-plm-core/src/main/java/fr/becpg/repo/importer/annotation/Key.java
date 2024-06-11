package fr.becpg.repo.importer.annotation;


/**
 * <p>Key class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class Key extends Attribute {

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Key [id=" + id + ", attribute=" + attribute + ", targetClass=" + targetClass + ", targetKey=" + targetKey + ", type=" + type
				+ ", key=" + key + "]";
	}
	
}
