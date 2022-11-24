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
import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;


/**
 * <p>IngItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname="bcpg:ing")
@AlfCacheable(isCharact = true)
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
	
	private List<NodeRef> allergenList = new LinkedList<>();
	
	
	/**
	 * <p>Constructor for IngItem.</p>
	 */
	public IngItem() {
		super();
	}

	/**
	 * <p>Constructor for IngItem.</p>
	 *
	 * @param ingItem a {@link fr.becpg.repo.product.data.ing.IngItem} object.
	 */
	public IngItem(IngItem ingItem) 
	{
		super(ingItem);
		this.ingCEECode = ingItem.ingCEECode;
	    this.ingType = ingItem.ingType;
	    this.charactName = ingItem.charactName;
	}
	
	
	/**
	 * <p>Getter for the field <code>charactName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:charactName")
	public String getCharactName() {
		return charactName;
	}
	
	
	/**
	 * <p>Setter for the field <code>charactName</code>.</p>
	 *
	 * @param charactName a {@link java.lang.String} object.
	 */
	public void setCharactName(String charactName) {
		this.charactName = charactName;
	}
	
	

	/** {@inheritDoc} */
	@Override
	public String getLegalName(Locale locale) {
		String ret = MLTextHelper.getClosestValue(legalName, locale);

		if(ret==null || ret.isEmpty()){
			return charactName;
		}
		
		return ret;
	}
	
	
	/**
	 * <p>Getter for the field <code>ingCEECode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:ingCEECode")
	public String getIngCEECode() {
		return ingCEECode;
	}

	/**
	 * <p>Setter for the field <code>ingCEECode</code>.</p>
	 *
	 * @param ingCEECode a {@link java.lang.String} object.
	 */
	public void setIngCEECode(String ingCEECode) {
		this.ingCEECode = ingCEECode;
	}
	
	/**
	 * <p>Getter for the field <code>ingCASCode</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:ingCASCode")
	public String getIngCASCode() {
		return ingCASCode;
	}

	/**
	 * <p>Setter for the field <code>ingCASCode</code>.</p>
	 *
	 * @param ingCASCode a {@link java.lang.String} object.
	 */
	public void setIngCASCode(String ingCASCode) {
		this.ingCASCode = ingCASCode;
	}
	
	/**
	 * <p>Getter for the field <code>ingRID</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:ingRID")
	public String getingRID() {
		return ingRID;
	}

	/**
	 * <p>Setter for the field <code>ingRID</code>.</p>
	 *
	 * @param ingRID a {@link java.lang.String} object.
	 */
	public void setingRID(String ingRID) {
		this.ingRID = ingRID;
	}

	/**
	 * <p>Getter for the field <code>ingType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.ing.IngTypeItem} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:ingTypeV2")
	@Override
	public IngTypeItem getIngType() {
		return ingType;
	}

	/** {@inheritDoc} */
	@Override
	public void setIngType(IngTypeItem ingType) {
		this.ingType = ingType;
	}

	
	/**
	 * <p>Getter for the field <code>pluralParents</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<NodeRef> getPluralParents() {
		return pluralParents;
	}
	
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:ingAllergens")
	public List<NodeRef> getAllergenList() {
		return allergenList;
	}
	
	/**
	 * <p>Setter for the field <code>bioOrigin</code>.</p>
	 *
	 * @param bioOrigin a {@link java.util.List} object.
	 */
	public void setAllergenList(List<NodeRef> allergenList) {
		this.allergenList = allergenList;
	}
	
	/** {@inheritDoc} */
	@Override
	public IngItem createCopy()  {
		return new IngItem(this);
	}

	/** {@inheritDoc} */
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


	/** {@inheritDoc} */
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
