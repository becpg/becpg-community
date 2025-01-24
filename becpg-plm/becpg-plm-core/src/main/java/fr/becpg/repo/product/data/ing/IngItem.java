/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
@AlfQname(qname = "bcpg:ing")
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

	private String ingAllergensQtyPerc;

	private IngTypeItem ingType;

	private Boolean isSubstanceOfVeryHighConcern;

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
	public IngItem(IngItem ingItem) {
		super(ingItem);
		this.ingCEECode = ingItem.ingCEECode;
		this.ingType = ingItem.ingType;
		this.charactName = ingItem.charactName;
		this.ingAllergensQtyPerc = ingItem.ingAllergensQtyPerc;
		this.isSubstanceOfVeryHighConcern = ingItem.isSubstanceOfVeryHighConcern;
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

		if (ret == null || ret.isEmpty()) {
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
	@AlfQname(qname = "bcpg:ingCEECode")
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
	@AlfQname(qname = "bcpg:casNumber")
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
	@AlfQname(qname = "bcpg:ingRID")
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
	 * <p>Getter for the field <code>ingAllergensQtyPerc</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingAllergensQtyPerc")
	public String getIngAllergensQtyPerc() {
		return ingAllergensQtyPerc;
	}

	/**
	 * <p>Setter for the field <code>ingAllergensQtyPerc</code>.</p>
	 *
	 * @param ingAllergensQtyPerc a {@link java.lang.String} object
	 */
	public void setIngAllergensQtyPerc(String ingAllergensQtyPerc) {
		this.ingAllergensQtyPerc = ingAllergensQtyPerc;
	}

	/**
	 * <p>getAllergensQtyMap.</p>
	 *
	 * @return a {@link java.util.Map} object
	 */
	public Map<String, Double> getAllergensQtyMap() {
		Map<String, Double> allergenMap = new HashMap<>();

		if (ingAllergensQtyPerc != null && !ingAllergensQtyPerc.isBlank()) {
			if (!ingAllergensQtyPerc.contains(",") && !ingAllergensQtyPerc.contains("|")) {
				allergenMap.put("ALL", Double.parseDouble(ingAllergensQtyPerc));
			} else {
				String[] allergenEntries = ingAllergensQtyPerc.split(",");
				for (String entry : allergenEntries) {
					String[] parts = entry.split("\\|");
					if (parts.length == 2) {
						String allergenCode = parts[0];
						Double allergenQty = Double.parseDouble(parts[1]);
						allergenMap.put(allergenCode, allergenQty);
					}
				}
			}
		}

		return allergenMap;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>ingType</code>.</p>
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:ingTypeV2")
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
	 * <p>Getter for the field <code>isSubstanceOfVeryHighConcern</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:isSubstanceOfVeryHighConcern")
	public Boolean getIsSubstanceOfVeryHighConcern() {
		return isSubstanceOfVeryHighConcern;
	}

	/**
	 * <p>Setter for the field <code>isSubstanceOfVeryHighConcern</code>.</p>
	 *
	 * @param isSubstanceOfVeryHighConcern a {@link java.lang.Boolean} object
	 */
	public void setIsSubstanceOfVeryHighConcern(Boolean isSubstanceOfVeryHighConcern) {
		this.isSubstanceOfVeryHighConcern = isSubstanceOfVeryHighConcern;
	}

	/**
	 * <p>Getter for the field <code>pluralParents</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<NodeRef> getPluralParents() {
		return pluralParents;
	}

	/**
	 * <p>Getter for the field <code>allergenList</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:ingAllergens")
	public List<NodeRef> getAllergenList() {
		return allergenList;
	}

	/**
	 * <p>Setter for the field <code>bioOrigin</code>.</p>
	 *
	 * @param allergenList a {@link java.util.List} object
	 */
	public void setAllergenList(List<NodeRef> allergenList) {
		this.allergenList = allergenList;
	}

	/** {@inheritDoc} */
	@Override
	public IngItem createCopy() {
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
		result = prime * result + ((isSubstanceOfVeryHighConcern == null) ? 0 : isSubstanceOfVeryHighConcern.hashCode());
		result = prime * result + ((ingAllergensQtyPerc == null) ? 0 : ingAllergensQtyPerc.hashCode());

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
		if (ingAllergensQtyPerc == null) {
			if (other.ingAllergensQtyPerc != null)
				return false;
		} else if (!ingAllergensQtyPerc.equals(other.ingAllergensQtyPerc))
			return false;
		if (ingType == null) {
			if (other.ingType != null)
				return false;
		} else if (!ingType.equals(other.ingType))
			return false;
		if (isSubstanceOfVeryHighConcern == null) {
			if (other.isSubstanceOfVeryHighConcern != null)
				return false;
		} else if (!isSubstanceOfVeryHighConcern.equals(other.isSubstanceOfVeryHighConcern))
			return false;
		return true;
	}

}
