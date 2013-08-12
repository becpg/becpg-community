/*
 * 
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfIdentAttr;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;

@AlfType
@AlfQname(qname = "bcpg:allergenList")
public class AllergenListDataItem extends AbstractManualVariantListDataItem {

	
	private Boolean voluntary = false;
	private Boolean inVoluntary = false;
	private List<NodeRef> voluntarySources = new ArrayList<NodeRef>();
	private List<NodeRef> inVoluntarySources = new ArrayList<NodeRef>();
	private NodeRef allergen;
	
	@AlfProp
	@AlfQname(qname="bcpg:allergenListVoluntary")
	public Boolean getVoluntary() {
		return voluntary;
	}
	
	
	
	public void setVoluntary(Boolean voluntary) {
		this.voluntary = voluntary;
	}
	

	@AlfProp
	@AlfQname(qname="bcpg:allergenListInVoluntary")
	public Boolean getInVoluntary() {
		return inVoluntary;
	}
	
	
	public void setInVoluntary(Boolean inVoluntary) {
		this.inVoluntary = inVoluntary;
	}
	

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:allergenListVolSources")
	public List<NodeRef> getVoluntarySources() {
		return voluntarySources;
	}
	
	
	public void setVoluntarySources(List<NodeRef> voluntarySources) {
		this.voluntarySources = voluntarySources;
	}
	

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:allergenListInVolSources")
	public List<NodeRef> getInVoluntarySources() {
		return inVoluntarySources;
	}
	
	
	public void setInVoluntarySources(List<NodeRef> inVoluntarySources) {
		this.inVoluntarySources = inVoluntarySources;
	}
	

	@AlfSingleAssoc
	@AlfQname(qname="bcpg:allergenListAllergen")
	@AlfIdentAttr
	public NodeRef getAllergen() {
		return allergen;
	}
	
	
	public void setAllergen(NodeRef allergen) {
		this.allergen = allergen;
	}
	
	
	
	public AllergenListDataItem(){
		super();
	}
	
	public AllergenListDataItem(NodeRef nodeRef, Boolean voluntary, Boolean inVoluntary, List<NodeRef> voluntarySources, List<NodeRef> inVoluntarySources, NodeRef allergen, Boolean isManual){
		super();
		this.nodeRef = nodeRef;
		this.voluntary  = voluntary;
		this.inVoluntary = inVoluntary;
		this.voluntarySources = voluntarySources;	
		this.inVoluntarySources = inVoluntarySources;
		this.allergen = allergen;
		this.isManual = isManual;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((allergen == null) ? 0 : allergen.hashCode());
		result = prime * result + ((inVoluntary == null) ? 0 : inVoluntary.hashCode());
		result = prime * result + ((inVoluntarySources == null) ? 0 : inVoluntarySources.hashCode());
		result = prime * result + ((voluntary == null) ? 0 : voluntary.hashCode());
		result = prime * result + ((voluntarySources == null) ? 0 : voluntarySources.hashCode());
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
		AllergenListDataItem other = (AllergenListDataItem) obj;
		if (allergen == null) {
			if (other.allergen != null)
				return false;
		} else if (!allergen.equals(other.allergen))
			return false;
		if (inVoluntary == null) {
			if (other.inVoluntary != null)
				return false;
		} else if (!inVoluntary.equals(other.inVoluntary))
			return false;
		if (inVoluntarySources == null) {
			if (other.inVoluntarySources != null)
				return false;
		} else if (!inVoluntarySources.equals(other.inVoluntarySources))
			return false;
		if (voluntary == null) {
			if (other.voluntary != null)
				return false;
		} else if (!voluntary.equals(other.voluntary))
			return false;
		if (voluntarySources == null) {
			if (other.voluntarySources != null)
				return false;
		} else if (!voluntarySources.equals(other.voluntarySources))
			return false;
		return true;
	}



	@Override
	public String toString() {
		return "AllergenListDataItem [voluntary=" + voluntary + ", inVoluntary=" + inVoluntary + ", voluntarySources=" + voluntarySources + ", inVoluntarySources="
				+ inVoluntarySources + ", allergen=" + allergen + ", isManual=" + isManual + ", nodeRef=" + nodeRef + ", parentNodeRef=" + parentNodeRef + ", name=" + name + "]";
	}

	
	
	
}
