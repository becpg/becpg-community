/*
 *
 */
package fr.becpg.repo.product.data.productList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.regulatory.RegulatoryEntityItem;
import fr.becpg.repo.regulatory.RequirementType;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.CopiableDataItem;
import fr.becpg.repo.repository.model.MinMaxValueDataItem;
import fr.becpg.repo.repository.model.SimpleListDataItem;

/**
 * <p>ToxListDataItem class.</p>
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:toxList")
public class ToxListDataItem extends AbstractManualDataItem implements SimpleListDataItem, RegulatoryEntityItem, MinMaxValueDataItem {

	/** Constant <code>serialVersionUID=8297326459126736070L</code> */
	private static final long serialVersionUID = 8297326459126736070L;

	private NodeRef tox;

	private Double value;

	private Double mini;

	private Double maxi;

	private RequirementType regulatoryType;

	private MLText regulatoryMessage;

	private List<NodeRef> regulatoryCountriesRef = new ArrayList<>();

	private List<NodeRef> regulatoryUsagesRef = new ArrayList<>();

	/**
	 * <p>Constructor for ToxListDataItem.</p>
	 */
	public ToxListDataItem() {
	}

	/**
	 * <p>Constructor for ToxListDataItem.</p>
	 *
	 * @param other a {@link fr.becpg.repo.product.data.productList.ToxListDataItem} object
	 */
	public ToxListDataItem(ToxListDataItem other) {
		this.tox = other.tox;
		this.value = other.value;
		this.mini = other.mini;
		this.maxi = other.maxi;
		this.regulatoryType = other.regulatoryType;
		this.regulatoryMessage = other.regulatoryMessage;
		if (other.regulatoryCountriesRef != null) {
			this.regulatoryCountriesRef = new ArrayList<>(other.regulatoryCountriesRef);
		}
		if (other.regulatoryUsagesRef != null) {
			this.regulatoryUsagesRef = new ArrayList<>(other.regulatoryUsagesRef);
		}
	}

	/**
	 * <p>Getter for the field <code>tox</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:toxListTox")
	@DataListIdentifierAttr
	public NodeRef getTox() {
		return tox;
	}

	/**
	 * <p>Setter for the field <code>tox</code>.</p>
	 *
	 * @param tox a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setTox(NodeRef tox) {
		this.tox = tox;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:toxListValue")
	public Double getValue() {
		return value;
	}

	/** {@inheritDoc} */
	public void setValue(Double value) {
		this.value = value;
	}

	/**
	 * <p>Getter for the field <code>mini</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:toxListMini")
	public Double getMini() {
		return mini;
	}

	/** {@inheritDoc} */
	@Override
	public void setMini(Double mini) {
		this.mini = mini;
	}

	/**
	 * <p>Getter for the field <code>maxi</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object
	 */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:toxListMaxi")
	public Double getMaxi() {
		return maxi;
	}

	/** {@inheritDoc} */
	@Override
	public void setMaxi(Double maxi) {
		this.maxi = maxi;
	}

	/**
	 * <p>Getter for the field <code>regulatoryCountriesRef</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@Override
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryCountries")
	public List<NodeRef> getRegulatoryCountriesRef() {
		return regulatoryCountriesRef;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryCountriesRef(List<NodeRef> regulatoryCountries) {
		this.regulatoryCountriesRef = regulatoryCountries;
	}

	/**
	 * <p>Getter for the field <code>regulatoryUsagesRef</code>.</p>
	 *
	 * @return a {@link java.util.List} object
	 */
	@Override
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:regulatoryUsageRef")
	public List<NodeRef> getRegulatoryUsagesRef() {
		return regulatoryUsagesRef;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryUsagesRef(List<NodeRef> regulatoryUsages) {
		this.regulatoryUsagesRef = regulatoryUsages;
	}

	/**
	 * <p>Getter for the field <code>regulatoryType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.regulatory.RequirementType} object
	 */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:regulatoryType")
	public RequirementType getRegulatoryType() {
		return regulatoryType;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryType(RequirementType regulatoryType) {
		this.regulatoryType = regulatoryType;
	}

	/**
	 * <p>Getter for the field <code>regulatoryMessage</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	@Override
	@AlfProp
	@AlfMlText
	@AlfQname(qname = "bcpg:regulatoryText")
	public MLText getRegulatoryMessage() {
		return regulatoryMessage;
	}

	/** {@inheritDoc} */
	@Override
	public void setRegulatoryMessage(MLText regulatoryMessage) {
		this.regulatoryMessage = regulatoryMessage;
	}

	/** {@inheritDoc} */
	@Override
	public CopiableDataItem copy() {
		ToxListDataItem ret = new ToxListDataItem(this);
		ret.setName(null);
		ret.setNodeRef(null);
		ret.setParentNodeRef(null);
		return ret;
	}

	/** {@inheritDoc} */
	@Override
	public void setCharactNodeRef(NodeRef tox) {
		setTox(tox);
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getCharactNodeRef() {
		return getTox();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(maxi, mini, regulatoryCountriesRef, regulatoryMessage, regulatoryType, regulatoryUsagesRef, tox, value);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ToxListDataItem other = (ToxListDataItem) obj;
		return Objects.equals(maxi, other.maxi) && Objects.equals(mini, other.mini)
				&& Objects.equals(regulatoryCountriesRef, other.regulatoryCountriesRef) && Objects.equals(regulatoryMessage, other.regulatoryMessage)
				&& regulatoryType == other.regulatoryType && Objects.equals(regulatoryUsagesRef, other.regulatoryUsagesRef)
				&& Objects.equals(tox, other.tox) && Objects.equals(value, other.value);
	}
	
}
