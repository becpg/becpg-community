package fr.becpg.repo.listvalue.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.model.FileInfo;
import org.apache.commons.io.FilenameUtils;

import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;

/**
 * <p>FileInfoListValueExtractor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FileInfoListValueExtractor implements ListValueExtractor<FileInfo> {

	/** {@inheritDoc} */
	@Override
	public List<ListValueEntry> extract(List<FileInfo> values) {
		List<ListValueEntry> suggestions = new ArrayList<>();
		if (values != null) {
			for (FileInfo value : values) {
				suggestions.add(new ListValueEntry(value.getNodeRef().toString(), FilenameUtils.removeExtension(value.getName()), "file"));
			}
		}
		return suggestions;
	}

}