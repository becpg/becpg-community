package fr.becpg.repo.autocomplete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.repo.autocomplete.impl.extractors.NodeRefAutoCompleteExtractor;
import fr.becpg.repo.search.BeCPGQueryBuilder;

@Service
public class DocumentAutoCompletePlugin implements AutoCompletePlugin {

	private static final String EXTENSIONS = "extensions";
	
	@Autowired
	private NodeService nodeService;
	
	public String[] getHandleSourceTypes() {
		return new String[] { "document" };
	}

	@SuppressWarnings("unchecked")
	@Override
	public AutoCompletePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofExactType(ContentModel.TYPE_CONTENT);
		String ftsQuery = (query != null && !query.isBlank() && !query.equals("*")) ? " AND +@cm\\:name:\"" + query + "\"" : "";
		if (props.containsKey(AutoCompleteService.EXTRA_PARAM)) {
			Map<String, String> extras = (Map<String, String>) props.get(AutoCompleteService.EXTRA_PARAM);
			if (extras.containsKey(EXTENSIONS)) {
				List<String> extensions = new ArrayList<>();
				extensions.addAll(Arrays.asList(extras.get(EXTENSIONS).split(",")));
				List<String> extensionsQueryParts = new ArrayList<>();
				for (String ext : extensions) {
					extensionsQueryParts.add("+@cm\\:name:\"*." + ext.trim() + "\"");
				}
				
				String extensionsQuery = String.join(" OR ", extensionsQueryParts);
				
				ftsQuery = "(" + extensionsQuery + ")" + ftsQuery;
			}
		}
		queryBuilder.andFTSQuery(ftsQuery);
		List<NodeRef> docs = queryBuilder.list();
		return new AutoCompletePage(docs, pageNum, pageSize, new NodeRefAutoCompleteExtractor(ContentModel.PROP_NAME, nodeService));
	}

	
}
