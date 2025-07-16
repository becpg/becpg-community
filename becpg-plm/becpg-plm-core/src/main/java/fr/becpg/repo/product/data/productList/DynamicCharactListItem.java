/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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

import org.alfresco.service.cmr.repository.MLText;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.ManualDataItem;
import fr.becpg.repo.repository.model.SortableDataItem;
import fr.becpg.repo.repository.model.Synchronisable;

/**
 * <p>DynamicCharactListItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:dynamicCharactList")
public class DynamicCharactListItem extends BeCPGDataObject implements Synchronisable,ManualDataItem, ColoredDataListItem, SortableDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8824174891991469677L;

	private String title;
	
	private MLText mlTitle;

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
	
	/**
	 * <p>Constructor for DynamicCharactListItem.</p>
	 */
	public DynamicCharactListItem() {
		super();
	}
	
	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.DynamicCharactListItem} object
	 */
	public static DynamicCharactListItem build() {
		return new DynamicCharactListItem();
	}
	
	/**
	 * <p>withTitle.</p>
	 *
	 * @param title a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.DynamicCharactListItem} object
	 */
	public DynamicCharactListItem withTitle(String title) {
		this.title = title;
		return this;
	}
	
	/**
	 * <p>withFormula.</p>
	 *
	 * @param formula a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.DynamicCharactListItem} object
	 */
	public DynamicCharactListItem withFormula(String formula) {
		this.formula = formula;
		return this;
	}
	
	/**
	 * <p>withColumnName.</p>
	 *
	 * @param columnName a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.DynamicCharactListItem} object
	 */
	public DynamicCharactListItem withColumnName(String columnName) {
		this.columnName = columnName;
		return this;
	}
	
	/**
	 * <p>withColor.</p>
	 *
	 * @param color a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.DynamicCharactListItem} object
	 */
	public DynamicCharactListItem withColor(String color) {
		this.color = color;
		return this;
	}
	
	/**
	 * <p>withSort.</p>
	 *
	 * @param sort a {@link java.lang.Integer} object
	 * @return a {@link fr.becpg.repo.product.data.productList.DynamicCharactListItem} object
	 */
	public DynamicCharactListItem withSort(Integer sort) {
		this.sort = sort;
		return this;
	}
	
	/**
	 * <p>withExecOrder.</p>
	 *
	 * @param execOrder a {@link fr.becpg.repo.product.data.productList.DynamicCharactExecOrder} object
	 * @return a {@link fr.becpg.repo.product.data.productList.DynamicCharactListItem} object
	 */
	public DynamicCharactListItem withExecOrder(DynamicCharactExecOrder execOrder) {
		this.execOrder = execOrder;
		return this;
	}
	
	/**
	 * <p>withMultiLevelFormula.</p>
	 *
	 * @param multiLevelFormula a {@link java.lang.Boolean} object
	 * @return a {@link fr.becpg.repo.product.data.productList.DynamicCharactListItem} object
	 */
	public DynamicCharactListItem withMultiLevelFormula(Boolean multiLevelFormula) {
		this.multiLevelFormula = multiLevelFormula;
		return this;
	}
	
	/**
	 * <p>Getter for the field <code>sort</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:sort")
	public Integer getSort() {
		return sort;
	}

	/** {@inheritDoc} */
	public void setSort(Integer sort) {
		this.sort = sort;
	}
	
	
	/**
	 * <p>Getter for the field <code>title</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:dynamicCharactTitle")
	public String getTitle() {
		return title;
	}

	/**
	 * <p>Setter for the field <code>title</code>.</p>
	 *
	 * @param title a {@link java.lang.String} object.
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * <p>Getter for the field <code>title</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfMlText
	@AlfProp
	@AlfQname(qname = "cm:title")
	public MLText getMlTitle() {
		return mlTitle;
	}

	/**
	 * <p>Setter for the field <code>mlTitle</code>.</p>
	 *
	 * @param mlTitle a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public void setMlTitle(MLText mlTitle) {
		this.mlTitle = mlTitle;
	}

	/**
	 * <p>Getter for the field <code>color</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:color")
	public String getColor() {
		return color;
	}



	/** {@inheritDoc} */
	public void setColor(String color) {
		this.color = color;
	}

	/**
	 * <p>Getter for the field <code>errorLog</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactErrorLog")
	public String getErrorLog() {
		return errorLog;
	}

	/**
	 * <p>Setter for the field <code>errorLog</code>.</p>
	 *
	 * @param errorLog a {@link java.lang.String} object.
	 */
	public void setErrorLog(String errorLog) {
		this.errorLog = errorLog;
	}

	/**
	 * <p>Getter for the field <code>formula</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:dynamicCharactFormula")
	public String getFormula() {
		return formula;
	}

	/**
	 * <p>Setter for the field <code>formula</code>.</p>
	 *
	 * @param formula a {@link java.lang.String} object.
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:dynamicCharactValue")
	public Object getValue() {
		return value;
	}
	
	//usefull for spel
	/**
	 * <p>getProtectedValue.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	public Object getProtectedValue() {
		return value;
	}

	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param value a {@link java.lang.Object} object.
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * <p>Getter for the field <code>groupColor</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactGroupColor")
	public String getGroupColor() {
		return groupColor;
	}

	/**
	 * <p>Setter for the field <code>groupColor</code>.</p>
	 *
	 * @param groupColor a {@link java.lang.String} object.
	 */
	public void setGroupColor(String groupColor) {
		this.groupColor = groupColor;
	}

	/**
	 * <p>Getter for the field <code>columnName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactColumn")
	public String getColumnName() {
		return columnName;
	}

	/**
	 * <p>Setter for the field <code>columnName</code>.</p>
	 *
	 * @param columnName a {@link java.lang.String} object.
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	

	/**
	 * <p>Getter for the field <code>synchronisableState</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.SynchronisableState} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactSynchronisableState")
	public SynchronisableState getSynchronisableState() {
		return synchronisableState;
	}

	/**
	 * <p>Setter for the field <code>synchronisableState</code>.</p>
	 *
	 * @param synchronisableState a {@link fr.becpg.repo.product.data.productList.SynchronisableState} object.
	 */
	public void setSynchronisableState(SynchronisableState synchronisableState) {
		this.synchronisableState = synchronisableState;
	}


	/**
	 * <p>Getter for the field <code>execOrder</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.DynamicCharactExecOrder} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactExecOrder")
	public DynamicCharactExecOrder getExecOrder() {
		return execOrder;
	}

	/**
	 * <p>Setter for the field <code>execOrder</code>.</p>
	 *
	 * @param execOrder a {@link fr.becpg.repo.product.data.productList.DynamicCharactExecOrder} object.
	 */
	public void setExecOrder(DynamicCharactExecOrder execOrder) {
		if(execOrder == null){
			this.execOrder = DynamicCharactExecOrder.Post;
		} else {
			this.execOrder = execOrder;
		}
	}

	
	/**
	 * <p>Getter for the field <code>multiLevelFormula</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:dynamicCharactMultiLevel")
	public Boolean getMultiLevelFormula() {
		return multiLevelFormula;
	}

	/**
	 * <p>Setter for the field <code>multiLevelFormula</code>.</p>
	 *
	 * @param isMultiLevelFormula a {@link java.lang.Boolean} object.
	 */
	public void setMultiLevelFormula(Boolean isMultiLevelFormula) {
		this.multiLevelFormula = isMultiLevelFormula;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSynchronisable() {
		return !SynchronisableState.Template.equals(synchronisableState);
	}

	

	/** {@inheritDoc} */
	@Override
	@InternalField
	public Boolean getIsManual() {
		return SynchronisableState.Manual.equals(synchronisableState);
	}

	/** {@inheritDoc} */
	@Override
	public void setIsManual(Boolean isManual) {
		if(Boolean.TRUE.equals(isManual)){
			this.synchronisableState = SynchronisableState.Manual;
		} else {
			this.synchronisableState = SynchronisableState.Synchronized;
		}
		
	}
	

	/**
	 * <p>isColumn.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	public Boolean isColumn() {
		return columnName!=null && !columnName.isBlank();
	}


	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "DynamicCharactListItem [title=" + title + ", formula=" + formula + ", value=" + value + ", multiLevelFormula=" + multiLevelFormula
				+ ", groupColor=" + groupColor + ", columnName=" + columnName + ", errorLog=" + errorLog + ", color=" + color + ", sort=" + sort
				+ ", synchronisableState=" + synchronisableState + ", execOrder=" + execOrder + "]";
	}

}
