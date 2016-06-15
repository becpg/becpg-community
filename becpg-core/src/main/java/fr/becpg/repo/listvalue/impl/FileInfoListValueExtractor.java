package fr.becpg.repo.listvalue.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.model.FileInfo;

import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;

public class FileInfoListValueExtractor implements ListValueExtractor<FileInfo> {

	@Override
	public List<ListValueEntry> extract(List<FileInfo> values) {
		List<ListValueEntry> suggestions = new ArrayList<>();
		if (values != null) {
			for (FileInfo value : values) {
				suggestions.add(new ListValueEntry(value.getNodeRef().toString(), value.getName(), "file"));
			}
		}
		return suggestions;
	}

}
