package fr.becpg.repo.audit.extractor;

import java.io.Serializable;

import org.alfresco.repo.audit.extractor.AbstractDataExtractor;

/**
 * <p>StringExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class StringExtractor extends AbstractDataExtractor {

	/** {@inheritDoc} */
	@Override
	public boolean isSupported(Serializable data) {
		return data != null;
	}

	/** {@inheritDoc} */
	@Override
	public Serializable extractData(Serializable value) throws Throwable {
		return value.toString();
	}

}
