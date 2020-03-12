/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;


@AlfType
@AlfQname(qname="bcpg:ing")
public class IngItem extends CompositeLabeling {	

	/**
	 * 
	 */
	private static final long serialVersionUID = -958461714975420707L;

	private String charactName;
	
	private String ingCEECode;
	
	private String ingCASCode;
	
	private String ingRID;
	
	private IngTypeItem ingType;
	
	private Set<NodeRef> pluralParents = new HashSet<>();

	
	
	public IngItem() {
		super();
	}

	public IngItem(IngItem ingItem) 
	{
		super(ingItem);
		this.ingCEECode = ingItem.ingCEECode;
	    this.ingType = ingItem.ingType;
	    this.charactName = ingItem.charactName;
	}
	
	
	@AlfProp
	@AlfQname(qname = "bcpg:charactName")
	public String getCharactName() {
		return charactName;
	}
	
	
	public void setCharactName(String charactName) {
		this.charactName = charactName;
	}
	

	@Override
	public String getLegalName(Locale locale) {
		String ret = MLTextHelper.getClosestValue(legalName, locale);

		if(ret==null || ret.isEmpty()){
			return charactName;
		}
		
		return ret;
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
	@AlfQname(qname="bcpg:ingCASCode")
	public String getIngCASCode() {
		return ingCASCode;
	}

	public void setIngCASCode(String ingCASCode) {
		this.ingCASCode = ingCASCode;
	}
	
	@AlfProp
	@AlfQname(qname="bcpg:ingRID")
	public String getingRID() {
		return ingRID;
	}

	public void setingRID(String ingRID) {
		this.ingRID = ingRID;
	}

	@AlfProp
	@AlfQname(qname="bcpg:ingTypeV2")
	public IngTypeItem getIngType() {
		return ingType;
	}

	public void setIngType(IngTypeItem ingType) {
		this.ingType = ingType;
	}

	
	public Set<NodeRef> getPluralParents() {
		return pluralParents;
	}
	
	@Override
	public IngItem clone()  {
		return new IngItem(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((charactName == null) ? 0 : charactName.hashCode());
		result = prime * result + ((ingCASCode == null) ? 0 : ingCASCode.hashCode());
		result = prime * result + ((ingCEECode == null) ? 0 : ingCEECode.hashCode());
		result = prime * result + ((ingRID == null) ? 0 : ingRID.hashCode());
		result = prime * result + ((ingType == null) ? 0 : ingType.hashCode());
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
		if (charactName == null) {
			if (other.charactName != null)
				return false;
		} else if (!charactName.equals(other.charactName))
			return false;
		if (ingCASCode == null) {
			if (other.ingCASCode != null)
				return false;
		} else if (!ingCASCode.equals(other.ingCASCode))
			return false;
		if (ingCEECode == null) {
			if (other.ingCEECode != null)
				return false;
		} else if (!ingCEECode.equals(other.ingCEECode))
			return false;
		if (ingRID == null) {
			if (other.ingRID != null)
				return false;
		} else if (!ingRID.equals(other.ingRID))
			return false;
		if (ingType == null) {
			if (other.ingType != null)
				return false;
		} else if (!ingType.equals(other.ingType))
			return false;
		return true;
	}

	

}
