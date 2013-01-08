package fr.becpg.repo.repository.model;



public interface SimpleListDataItem extends IManualDataItem, SimpleCharactDataItem {


	public Double getMini();

	public void setMini(Double value);

	public Double getMaxi();

	public void setMaxi(Double value);
	
	public Integer getSort();
	
	public void setSort(Integer sort);
}
