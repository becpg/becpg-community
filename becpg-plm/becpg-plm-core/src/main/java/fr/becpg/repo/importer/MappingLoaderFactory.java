package fr.becpg.repo.importer;

/**
 * <p>MappingLoaderFactory interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface MappingLoaderFactory {

	/**
	 * <p>register.</p>
	 *
	 * @param mappingLoader a {@link fr.becpg.repo.importer.MappingLoader} object.
	 */
	void register(MappingLoader mappingLoader);
	
	/**
	 * <p>getMappingLoader.</p>
	 *
	 * @param mappingType a {@link fr.becpg.repo.importer.MappingType} object.
	 * @return a {@link fr.becpg.repo.importer.MappingLoader} object.
	 */
	MappingLoader getMappingLoader(MappingType mappingType);
}
