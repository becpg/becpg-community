/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;


@AlfType
@AlfQname(qname="bcpg:ing")
public class IngItem extends AbstractLabelingComponent {	

	private String ingCEECode;
	
	private IngTypeItem ingType;

	private List<IngItem> subIngs  = new ArrayList<>();
	
	
	public IngItem() {
		super();
	}

	public IngItem(IngItem ingItem) 
	{
		super(ingItem);
		this.ingCEECode = ingItem.ingCEECode;
	    this.ingType = ingItem.ingType;
	    this.subIngs = ingItem.subIngs;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:ingCEECode")
	public String getIngCEECode() {
		return ingCEECode;
	}

	public void setIngCEECode(String ingCEECode) {
		this.ingCEECode = ingCEECode;
	}


	@AlfProp
	@AlfQname(qname="bcpg:ingTypeV2")
	public IngTypeItem getIngType() {
		return ingType;
	}

	public void setIngType(IngTypeItem ingType) {
		this.ingType = ingType;
	}

	public List<IngItem> getSubIngs() {
		return subIngs;
	}

	public void setSubIngs(List<IngItem> subIngs) {
		this.subIngs = subIngs;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ingCEECode == null) ? 0 : ingCEECode.hashCode());
		result = prime * result + ((ingType == null) ? 0 : ingType.hashCode());
		result = prime * result + ((subIngs == null) ? 0 : subIngs.hashCode());
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
		IngItem other = (IngItem) obj;
		if (ingCEECode == null) {
			if (other.ingCEECode != null)
				return false;
		} else if (!ingCEECode.equals(other.ingCEECode))
			return false;
		if (ingType == null) {
			if (other.ingType != null)
				return false;
		} else if (!ingType.equals(other.ingType))
			return false;
		if (subIngs == null) {
			if (other.subIngs != null)
				return false;
		} else if (!subIngs.equals(other.subIngs))
			return false;
		return true;
	}

	

}
