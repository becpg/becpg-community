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
package fr.becpg.repo.product.data.ing;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.constraints.PlaceOfActivityTypeCode;
import fr.becpg.repo.product.formulation.labeling.FootNoteRule;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>Abstract LabelingComponent class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class LabelingComponent extends BeCPGDataObject implements RepositoryEntity, Comparable<LabelingComponent> {

	private static final long serialVersionUID = 270866664168102414L;

	protected Double qty = 0d;

	protected Double qtyWithYield = 0d;

	protected Double volume = 0d;

	protected Double volumeWithYield = 0d;

	protected MLText legalName;

	private boolean isPlural = false;

	protected MLText pluralLegalName;

	private Set<NodeRef> allergens = new HashSet<>();
	
	private Map<PlaceOfActivityTypeCode,Set<NodeRef>> geoOriginsByPlaceOfActivity = new EnumMap<>(PlaceOfActivityTypeCode.class);

	private Set<NodeRef> bioOrigins = new HashSet<>();
	
	private Set<FootNoteRule> footNotes = new HashSet<>();

	/**
	 * <p>Constructor for LabelingComponent.</p>
	 */
	protected LabelingComponent() {
		super();
	}

	/**
	 * <p>Constructor for LabelingComponent.</p>
	 *
	 * @param abstractLabelingComponent a {@link fr.becpg.repo.product.data.ing.LabelingComponent} object.
	 */
	protected LabelingComponent(LabelingComponent abstractLabelingComponent) {
		super(abstractLabelingComponent);
		this.pluralLegalName = abstractLabelingComponent.pluralLegalName;
		this.qty = abstractLabelingComponent.qty;
		this.qtyWithYield = abstractLabelingComponent.qtyWithYield;
		this.volume = abstractLabelingComponent.volume;
		this.volumeWithYield = abstractLabelingComponent.volumeWithYield;
		this.legalName = abstractLabelingComponent.legalName;
		this.isPlural = abstractLabelingComponent.isPlural;
		this.footNotes = new HashSet<>(abstractLabelingComponent.footNotes);
		this.allergens = new HashSet<>(abstractLabelingComponent.allergens);
		this.geoOriginsByPlaceOfActivity = new EnumMap<>(abstractLabelingComponent.geoOriginsByPlaceOfActivity);
		this.bioOrigins = new HashSet<>(abstractLabelingComponent.bioOrigins);
	}

	/**
	 * <p>Getter for the field <code>legalName</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfMlText
	@AlfProp
	@AlfQname(qname = "bcpg:legalName")
	public MLText getLegalName() {
		return legalName;
	}

	/**
	 * <p>Setter for the field <code>legalName</code>.</p>
	 *
	 * @param legalName a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setLegalName(MLText legalName) {
		this.legalName = legalName;
	}

	/**
	 * <p>Getter for the field <code>legalName</code>.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getLegalName(Locale locale) {
		String ret = MLTextHelper.getClosestValue(legalName, locale);

		if ((ret == null) || ret.isEmpty()) {
			return name;
		}

		return ret;
	}

	/**
	 * <p>Getter for the field <code>pluralLegalName</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfMlText
	@AlfProp
	@AlfQname(qname = "bcpg:pluralLegalName")
	public MLText getPluralLegalName() {
		return pluralLegalName;
	}

	/**
	 * <p>Setter for the field <code>pluralLegalName</code>.</p>
	 *
	 * @param pluralLegalName a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setPluralLegalName(MLText pluralLegalName) {
		this.pluralLegalName = pluralLegalName;
	}

	/**
	 * <p>Getter for the field <code>pluralLegalName</code>.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String getPluralLegalName(Locale locale) {
		String ret = MLTextHelper.getClosestValue(pluralLegalName, locale);

		if ((ret == null) || ret.isEmpty()) {
			return getLegalName(locale);
		}

		return ret;
	}

	/**
	 * <p>isPlural.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPlural() {
		return isPlural && (pluralLegalName != null) && !MLTextHelper.isEmpty(pluralLegalName);
	}

	/**
	 * <p>setPlural.</p>
	 *
	 * @param isPlural a boolean.
	 */
	public void setPlural(boolean isPlural) {
		this.isPlural = isPlural;
	}

	/**
	 * <p>Getter for the field <code>qty</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getQty() {
		return qty;
	}

	public Double getQty(boolean withYield) {
		return withYield ? qtyWithYield : qty;
	}

	/**
	 * <p>Setter for the field <code>qty</code>.</p>
	 *
	 * @param qty a {@link java.lang.Double} object.
	 */
	public void setQty(Double qty) {
		this.qty = qty;
	}

	public Double getQtyWithYield() {
		return qtyWithYield;
	}

	public void setQtyWithYield(Double qtyWithYield) {
		this.qtyWithYield = qtyWithYield;
	}

	/**
	 * <p>Getter for the field <code>volume</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getVolume() {
		return volume;
	}

	public Double getVolume(boolean withYield) {
		return withYield ? qtyWithYield : volume;
	}

	/**
	 * <p>Setter for the field <code>volume</code>.</p>
	 *
	 * @param volume a {@link java.lang.Double} object.
	 */
	public void setVolume(Double volume) {
		this.volume = volume;
	}

	public Double getVolumeWithYield() {
		return volumeWithYield;
	}

	public void setVolumeWithYield(Double volumeWithYield) {
		this.volumeWithYield = volumeWithYield;
	}


	public void setQties(Double value) {
		this.qty = value;
		this.qtyWithYield = value;
		this.volume = value;
		this.volumeWithYield = value;
	}

	
	/**
	 * <p>Getter for the field <code>allergens</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<NodeRef> getAllergens() {
		return allergens;
	}

	/**
	 * <p>Setter for the field <code>allergens</code>.</p>
	 *
	 * @param allergens a {@link java.util.Set} object.
	 */
	public void setAllergens(Set<NodeRef> allergens) {
		this.allergens = allergens;
	}

	public Set<FootNoteRule> getFootNotes() {
		return footNotes;
	}

	public void setFootNotes(Set<FootNoteRule> footNotes) {
		this.footNotes = footNotes;
	}

	/**
	 * <p>Getter for the field <code>bioOrigins</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<NodeRef> getBioOrigins() {
		return bioOrigins;
	}

	/**
	 * <p>Setter for the field <code>geoOrigins</code>.</p>
	 *
	 * @param bioOrigins a {@link java.util.Set} object.
	 */
	public void setBioOrigins(Set<NodeRef> bioOrigins) {
		this.bioOrigins = bioOrigins;
	}

	public Map<PlaceOfActivityTypeCode, Set<NodeRef>> getGeoOriginsByPlaceOfActivity() {
		return geoOriginsByPlaceOfActivity;
	}

	public void setGeoOriginsByPlaceOfActivity(Map<PlaceOfActivityTypeCode, Set<NodeRef>> geoOriginsByPlaceOfActivity) {
		this.geoOriginsByPlaceOfActivity = geoOriginsByPlaceOfActivity;
	}


	public abstract LabelingComponent createCopy();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result)
				+ Objects.hash(footNotes, allergens, bioOrigins, geoOriginsByPlaceOfActivity, isPlural, legalName, pluralLegalName, qty, qtyWithYield, volume, volumeWithYield);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj) || (getClass() != obj.getClass())) {
			return false;
		}
		LabelingComponent other = (LabelingComponent) obj;
		return Objects.equals(allergens, other.allergens) && Objects.equals(bioOrigins, other.bioOrigins) && Objects.equals(footNotes, other.footNotes)
				&& Objects.equals(geoOriginsByPlaceOfActivity, other.geoOriginsByPlaceOfActivity) && (isPlural == other.isPlural) && Objects.equals(legalName, other.legalName)
				&& Objects.equals(pluralLegalName, other.pluralLegalName) && Objects.equals(qty, other.qty)
				&& Objects.equals(qtyWithYield, other.qtyWithYield) && Objects.equals(volume, other.volume)
				&& Objects.equals(volumeWithYield, other.volumeWithYield);
	}

	@Override
	public String toString() {
		return "LabelingComponent [qty=" + qty + ", qtyWithYield=" + qtyWithYield + ", volume=" + volume + ", volumeWithYield=" + volumeWithYield
				+ ", legalName=" + legalName + ", nodeRef=" + nodeRef + ", name=" + name + "]";
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(LabelingComponent lblComponent) {

		if ((lblComponent instanceof CompositeLabeling) && ((CompositeLabeling) lblComponent).isGroup()
				&& !((this instanceof CompositeLabeling) && ((CompositeLabeling) this).isGroup())) {
			return 1;
		}

		if (!((lblComponent instanceof CompositeLabeling) && ((CompositeLabeling) lblComponent).isGroup())
				&& ((this instanceof CompositeLabeling) && ((CompositeLabeling) this).isGroup())) {
			return -1;
		}

		if ((lblComponent.getQty() != null) && (this.getQty() != null)) {
			return Double.compare(lblComponent.getQty(), this.getQty());
		} else if ((this.getQty() == null) && (lblComponent.getQty() != null)) {
			return 1; // after
		} else if ((this.getQty() != null) && (lblComponent.getQty() == null)) {
			return -1; // before
		}
		return 0;// equals
	}


}
