package fr.becpg.repo.entity.remote.extractor;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;

public class RemoteHelper {
	

	public static QName getPropName(QName type) {
		if(BeCPGModel.TYPE_LINKED_VALUE.equals(type)){
			return BeCPGModel.PROP_LKV_VALUE;
		}
		return ContentModel.PROP_NAME;
	}


}
