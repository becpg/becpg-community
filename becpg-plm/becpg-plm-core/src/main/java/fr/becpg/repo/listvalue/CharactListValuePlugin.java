package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;

@Service("charactListValuePlugin")
public class CharactListValuePlugin extends EntityListValuePlugin {

	private static final String SOURCE_TYPE_NUT = "nut";
	private static final String SOURCE_TYPE_PHYSICO_CHEM = "physicoChem";

	@Autowired
	private CharactValueExtractor charactValueExtractor;

	@Override
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_NUT, SOURCE_TYPE_PHYSICO_CHEM };
	}

	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
		if (SOURCE_TYPE_NUT.equals(sourceType)) {
			return suggestTargetAssoc(null, PLMModel.TYPE_NUT, query, pageNum, pageSize, null, props);
		} else {
			return suggestTargetAssoc(null, PLMModel.TYPE_PHYSICO_CHEM, query, pageNum, pageSize, null, props);
		}
	}

	@Override
	protected ListValueExtractor<NodeRef> getTargetAssocValueExtractor() {
		return charactValueExtractor;
	}

}
