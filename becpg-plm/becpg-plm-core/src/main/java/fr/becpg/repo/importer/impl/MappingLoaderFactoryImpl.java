package fr.becpg.repo.importer.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import fr.becpg.repo.importer.MappingLoader;
import fr.becpg.repo.importer.MappingLoaderFactory;
import fr.becpg.repo.importer.MappingType;

@Service
public class MappingLoaderFactoryImpl implements MappingLoaderFactory {
	
	final List<MappingLoader> mappingLoaderList = new ArrayList<>();

	@Override
	public void register(MappingLoader mappingLoader) {
		mappingLoaderList.add(mappingLoader);
	}

	@Override
	public MappingLoader getMappingLoader(MappingType mappingType) {
		for(MappingLoader mappingLoader : mappingLoaderList) {
			if(mappingLoader.applyTo(mappingType)) {
				return mappingLoader;
			}
		}
		return null;
	}

}
