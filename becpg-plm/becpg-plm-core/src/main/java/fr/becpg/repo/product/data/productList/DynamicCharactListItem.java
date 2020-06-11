/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.product.data.productList;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.ManualDataItem;
import fr.becpg.repo.repository.model.SortableDataItem;
import fr.becpg.repo.repository.model.Synchronisable;

@AlfType
@AlfQname(qname = "bcpg:dynamicCharactList")
public class DynamicCharactListItem extends BeCPGDataObject implements Synchronisable,ManualDataItem, ColoredDataListItem, SortableDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8824174891991469677L;

	private String title;

	private String formula;

	private Object value;
	
	private Boolean multiLevelFormula;

	private String groupColor;

	private String columnName;
	
	private String errorLog;
	
	private String color;
	
	protected Integer sort;
	
	private SynchronisableState synchronisableState = SynchronisableState.Synchronized;

	private DynamicCharactExecOrder execOrder = DynamicCharactExecOrder.Post;
	
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:sort")
	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}
	
	
	@AlfProp
	@AlfQname(qname = "bcpg:dynamicCharactTitle")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:color")
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactErrorLog")
	public String getErrorLog() {
		return errorLog;
	}

	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:dynamicCharactFormula")
	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:dynamicCharactValue")
	public Object getValue() {
		return value;
	}
	
	//usefull for spel
	public Object getProtectedValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactGroupColor")
	public String getGroupColor() {
		return groupColor;
	}

	public void setGroupColor(String groupColor) {
		this.groupColor = groupColor;
	}

	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactColumn")
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	

	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactSynchronisableState")
	public SynchronisableState getSynchronisableState() {
		return synchronisableState;
	}

	public void setSynchronisableState(SynchronisableState synchronisableState) {
		this.synchronisableState = synchronisableState;
	}


	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactExecOrder")
	public DynamicCharactExecOrder getExecOrder() {
		return execOrder;
	}

	public void setExecOrder(DynamicCharactExecOrder execOrder) {
		if(execOrder == null){
			this.execOrder = DynamicCharactExecOrder.Post;
		} else {
			this.execOrder = execOrder;
		}
	}

	
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactMultiLevel")
	public Boolean getMultiLevelFormula() {
		return multiLevelFormula;
	}

	public void setMultiLevelFormula(Boolean isMultiLevelFormula) {
		this.multiLevelFormula = isMultiLevelFormula;
	}

	@Override
	public boolean isSynchronisable() {
		return !SynchronisableState.Template.equals(synchronisableState);
	}

	

	@Override
	@InternalField
	public Boolean getIsManual() {
		return SynchronisableState.Manual.equals(synchronisableState);
	}

	@Override
	public void setIsManual(Boolean isManual) {
		if(Boolean.TRUE.equals(isManual)){
			this.synchronisableState = SynchronisableState.Manual;
		} else {
			this.synchronisableState = SynchronisableState.Synchronized;
		}
		
	}
	
	public DynamicCharactListItem() {
		super();
	}

	
	public DynamicCharactListItem(String dynamicCharactTitle, String dynamicCharactFormula) {
		super();
		this.title = dynamicCharactTitle;
		this.formula = dynamicCharactFormula;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result + ((errorLog == null) ? 0 : errorLog.hashCode());
		result = prime * result + ((execOrder == null) ? 0 : execOrder.hashCode());
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
		result = prime * result + ((groupColor == null) ? 0 : groupColor.hashCode());
		result = prime * result + ((multiLevelFormula == null) ? 0 : multiLevelFormula.hashCode());
		result = prime * result + ((sort == null) ? 0 : sort.hashCode());
		result = prime * result + ((synchronisableState == null) ? 0 : synchronisableState.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DynamicCharactListItem other = (DynamicCharactListItem) obj;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (errorLog == null) {
			if (other.errorLog != null)
				return false;
		} else if (!errorLog.equals(other.errorLog))
			return false;
		if (execOrder != other.execOrder)
			return false;
		if (formula == null) {
			if (other.formula != null)
				return false;
		} else if (!formula.equals(other.formula))
			return false;
		if (groupColor == null) {
			if (other.groupColor != null)
				return false;
		} else if (!groupColor.equals(other.groupColor))
			return false;
		if (multiLevelFormula == null) {
			if (other.multiLevelFormula != null)
				return false;
		} else if (!multiLevelFormula.equals(other.multiLevelFormula))
			return false;
		if (sort == null) {
			if (other.sort != null)
				return false;
		} else if (!sort.equals(other.sort))
			return false;
		if (synchronisableState != other.synchronisableState)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DynamicCharactListItem [title=" + title + ", formula=" + formula + ", value=" + value + ", multiLevelFormula=" + multiLevelFormula
				+ ", groupColor=" + groupColor + ", columnName=" + columnName + ", errorLog=" + errorLog + ", color=" + color + ", sort=" + sort
				+ ", synchronisableState=" + synchronisableState + ", execOrder=" + execOrder + "]";
	}

}
