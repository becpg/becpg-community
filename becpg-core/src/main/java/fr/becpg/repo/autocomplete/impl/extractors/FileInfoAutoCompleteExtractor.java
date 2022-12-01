package fr.becpg.repo.autocomplete.impl.extractors;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.model.FileInfo;
import org.apache.commons.io.FilenameUtils;

import fr.becpg.repo.autocomplete.AutoCompleteEntry;
import fr.becpg.repo.autocomplete.AutoCompleteExtractor;

/**
 * <p>FileInfoListValueExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FileInfoAutoCompleteExtractor implements AutoCompleteExtractor<FileInfo> {

	/** {@inheritDoc} */
	@Override
	public List<AutoCompleteEntry> extract(List<FileInfo> values) {
		List<AutoCompleteEntry> suggestions = new ArrayList<>();
		if (values != null) {
			for (FileInfo value : values) {
				suggestions.add(new AutoCompleteEntry(value.getNodeRef().toString(), FilenameUtils.removeExtension(value.getName()), "file"));
			}
		}
		return suggestions;
	}

}
