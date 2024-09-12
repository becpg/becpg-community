package fr.becpg.repo.importer.annotation;

/**
 * <p>DataListKey class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DataListKey extends Annotation {

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "DataListKey [id=" + id + ", attribute=" + attribute + ", targetClass=" + targetClass + ", targetKey=" + targetKey + ", type=" + type
				+ ", key=" + key + "]";
	}
	
}
