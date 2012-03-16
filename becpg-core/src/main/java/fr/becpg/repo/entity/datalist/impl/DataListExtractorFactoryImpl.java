package fr.becpg.repo.entity.datalist.impl;

import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;

public class DataListExtractorFactoryImpl implements DataListExtractorFactory {


	SimpleExtractor simpleExtractor;
	
	MultiLevelExtractor multiLevelExtractor;
	
	WUsedExtractor wUsedExtractor;
	
	
	public void setSimpleExtractor(SimpleExtractor simpleExtractor) {
		this.simpleExtractor = simpleExtractor;
	}


	public void setMultiLevelExtractor(MultiLevelExtractor multiLevelExtractor) {
		this.multiLevelExtractor = multiLevelExtractor;
	}


	public void setwUsedExtractor(WUsedExtractor wUsedExtractor) {
		this.wUsedExtractor = wUsedExtractor;
	}


	@Override
	public DataListExtractor getExtractor(String dataListName,QName dataType) {
		if(dataListName!=null && dataListName.equals("WUsed")){
			return wUsedExtractor;
		} else if(dataType!=null && dataType.equals(BeCPGModel.TYPE_COMPOLIST)){
			return multiLevelExtractor;
		}
		return simpleExtractor;
	}

}
