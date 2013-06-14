package fr.becpg.repo.product.data.productList;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:dynamicCharactList")
public class DynamicCharactListItem extends BeCPGDataObject {

	private String title;

	private String formula;

	private Object value;

	private String groupColor;

	private String columnName;
	
	private String errorLog;

	@AlfProp
	@AlfQname(qname = "bcpg:dynamicCharactTitle")
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	

	@AlfProp
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

	public void setValue(Object value) {
		this.value = value;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:dynamicCharactGroupColor")
	public String getGroupColor() {
		return groupColor;
	}

	public void setGroupColor(String groupColor) {
		this.groupColor = groupColor;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:dynamicCharactColumn")
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
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
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result + ((errorLog == null) ? 0 : errorLog.hashCode());
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
		result = prime * result + ((groupColor == null) ? 0 : groupColor.hashCode());
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
		return "DynamicCharactListItem [title=" + title + ", formula=" + formula + ", value=" + value + ", groupColor=" + groupColor + ", columnName=" + columnName + ", errorLog="
				+ errorLog + "]";
	}

}
