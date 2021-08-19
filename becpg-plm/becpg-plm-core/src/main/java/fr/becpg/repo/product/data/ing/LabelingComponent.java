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
package fr.becpg.repo.product.data.ing;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.helper.MLTextHelper;
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
public abstract class LabelingComponent extends BeCPGDataObject implements RepositoryEntity, Comparable<LabelingComponent>, Cloneable {

	private static final long serialVersionUID = 270866664168102414L;

	protected Double qty = 0d;

	protected Double volume = 0d;

	protected MLText legalName;

	private boolean isPlural = false;

	protected MLText pluralLegalName;

	private Set<NodeRef> allergens = new HashSet<>();

	private Set<NodeRef> geoOrigins = new HashSet<>();

	/**
	 * <p>Constructor for LabelingComponent.</p>
	 */
	public LabelingComponent() {
		super();
	}

	/**
	 * <p>Constructor for LabelingComponent.</p>
	 *
	 * @param abstractLabelingComponent a {@link fr.becpg.repo.product.data.ing.LabelingComponent} object.
	 */
	public LabelingComponent(LabelingComponent abstractLabelingComponent) {
		super(abstractLabelingComponent);
		this.pluralLegalName = abstractLabelingComponent.pluralLegalName;
		this.qty = abstractLabelingComponent.qty;
		this.volume = abstractLabelingComponent.volume;
		this.legalName = abstractLabelingComponent.legalName;
		this.isPlural = abstractLabelingComponent.isPlural;
		this.allergens = new HashSet<>(abstractLabelingComponent.allergens);
		this.geoOrigins = new HashSet<>(abstractLabelingComponent.geoOrigins);
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

	/**
	 * <p>Setter for the field <code>qty</code>.</p>
	 *
	 * @param qty a {@link java.lang.Double} object.
	 */
	public void setQty(Double qty) {
		this.qty = qty;
	}

	/**
	 * <p>Getter for the field <code>volume</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getVolume() {
		return volume;
	}

	/**
	 * <p>Setter for the field <code>volume</code>.</p>
	 *
	 * @param volume a {@link java.lang.Double} object.
	 */
	public void setVolume(Double volume) {
		this.volume = volume;
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

	/**
	 * <p>Getter for the field <code>geoOrigins</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<NodeRef> getGeoOrigins() {
		return geoOrigins;
	}

	/**
	 * <p>Setter for the field <code>geoOrigins</code>.</p>
	 *
	 * @param geoOrigins a {@link java.util.Set} object.
	 */
	public void setGeoOrigins(Set<NodeRef> geoOrigins) {
		this.geoOrigins = geoOrigins;
	}

	/** {@inheritDoc} */
	@Override
	public abstract LabelingComponent clone();

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((allergens == null) ? 0 : allergens.hashCode());
		result = (prime * result) + ((geoOrigins == null) ? 0 : geoOrigins.hashCode());
		result = (prime * result) + (isPlural ? 1231 : 1237);
		result = (prime * result) + ((legalName == null) ? 0 : legalName.hashCode());
		result = (prime * result) + ((pluralLegalName == null) ? 0 : pluralLegalName.hashCode());
		result = (prime * result) + ((qty == null) ? 0 : qty.hashCode());
		result = (prime * result) + ((volume == null) ? 0 : volume.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		LabelingComponent other = (LabelingComponent) obj;
		if (allergens == null) {
			if (other.allergens != null) {
				return false;
			}
		} else if (!allergens.equals(other.allergens)) {
			return false;
		}
		if (geoOrigins == null) {
			if (other.geoOrigins != null) {
				return false;
			}
		} else if (!geoOrigins.equals(other.geoOrigins)) {
			return false;
		}
		if (isPlural != other.isPlural) {
			return false;
		}
		if (legalName == null) {
			if (other.legalName != null) {
				return false;
			}
		} else if (!legalName.equals(other.legalName)) {
			return false;
		}
		if (pluralLegalName == null) {
			if (other.pluralLegalName != null) {
				return false;
			}
		} else if (!pluralLegalName.equals(other.pluralLegalName)) {
			return false;
		}
		if (qty == null) {
			if (other.qty != null) {
				return false;
			}
		} else if (!qty.equals(other.qty)) {
			return false;
		}
		if (volume == null) {
			if (other.volume != null) {
				return false;
			}
		} else if (!volume.equals(other.volume)) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "LabelingComponent [qty=" + qty + ", volume=" + volume + ", legalName=" + legalName + ", nodeRef=" + nodeRef + ", name=" + name
				+ "]";
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
