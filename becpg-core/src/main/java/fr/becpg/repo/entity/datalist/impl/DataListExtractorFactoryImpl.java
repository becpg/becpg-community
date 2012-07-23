package fr.becpg.repo.entity.datalist.impl;

import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.data.DataListFilter;

@Service
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
	public DataListExtractor getExtractor(DataListFilter dataListFilter,String dataListName) {
		if(!dataListFilter.isSimpleItem()){
			if(dataListName!=null && dataListName.equals("WUsed")){
				return wUsedExtractor;
			} else if(dataListFilter.getDataType()!=null && dataListFilter.getDataType().equals(BeCPGModel.TYPE_COMPOLIST)){
				return multiLevelExtractor;
			}
		}
		return simpleExtractor;
	}

}
