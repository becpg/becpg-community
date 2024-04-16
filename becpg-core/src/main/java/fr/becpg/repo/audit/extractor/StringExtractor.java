package fr.becpg.repo.audit.extractor;

import java.io.Serializable;

import org.alfresco.repo.audit.extractor.AbstractDataExtractor;

public class StringExtractor extends AbstractDataExtractor {

	@Override
	public boolean isSupported(Serializable data) {
		return data != null;
	}

	@Override
	public Serializable extractData(Serializable value) throws Throwable {
		return value.toString();
	}

}
