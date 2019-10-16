package fr.becpg.repo.importer;

public interface MappingLoaderFactory {

	void register(MappingLoader mappingLoader);
	
	MappingLoader getMappingLoader(MappingType mappingType);
}
