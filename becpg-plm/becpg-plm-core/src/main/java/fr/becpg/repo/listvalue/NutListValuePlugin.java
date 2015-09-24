package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.listvalue.impl.EntityListValuePlugin;

@Service("nutListValuePlugin")
public class NutListValuePlugin  extends  EntityListValuePlugin{

	
	private static final String SOURCE_TYPE_NUT = "nut";
	
	@Autowired
	private NutValueExtractor nutValueExtractor;
	
	
	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_NUT };
	}
	
	@Override
	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {
			return suggestTargetAssoc(PLMModel.TYPE_NUT, query, pageNum, pageSize, null, props);
	}
	
	@Override
	protected ListValueExtractor<NodeRef> getTargetAssocValueExtractor() {
		return nutValueExtractor;
	}
	
}
