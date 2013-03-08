package fr.becpg.repo.entity.datalist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class AbstractDataListSortPlugin implements DataListSortPlugin {

	protected static final int BEFORE = -1;
	protected static final int EQUAL = 0;
	protected static final int AFTER = 1;
	
	protected DataListSortRegistry dataListSortRegistry;
	
	protected Log logger = LogFactory.getLog(getClass());
	
	public void setDataListSortRegistry(DataListSortRegistry dataListSortRegistry) {
		this.dataListSortRegistry = dataListSortRegistry;
	}

	public void init(){
		dataListSortRegistry.addPlugin(this);
	}
}
