package fr.becpg.repo.product.helper;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;

public class CharactHelper {

	@Deprecated
	public static Double getCharactValue(NodeRef charactNodeRef, QName charactType, ProductData productData) {
		// TODO make more generic use an annotation instead
		if (charactType.equals(BeCPGModel.TYPE_COST)) {
			return getCharactValue(charactNodeRef, productData.getCostList());
		} else if (charactType.equals(BeCPGModel.TYPE_NUT)) {
			return getCharactValue(charactNodeRef, productData.getNutList());
		} else if (charactType.equals(BeCPGModel.TYPE_ING)) {
			return getCharactValue(charactNodeRef, productData.getIngList());
		}
		return null;
	}

	public static Double getCharactValue(NodeRef charactNodeRef, List<? extends SimpleCharactDataItem> charactList) {
	
		if (charactList != null && charactNodeRef != null) {
			for (SimpleCharactDataItem charactDataListItem : charactList) {
				if (charactNodeRef.equals(charactDataListItem.getCharactNodeRef())) {
					return charactDataListItem.getValue();
				}
			}
		}
		return null;
	}

}
