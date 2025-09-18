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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.ManualDataItem;
import fr.becpg.repo.repository.model.Synchronisable;

/**
 * <p>LabelingRuleListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:labelingRuleList")
public class LabelingRuleListDataItem extends BeCPGDataObject implements Synchronisable, ManualDataItem {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8396800984821144870L;

	/** Constant <code>DEFAULT_LABELING_GROUP="default"</code> */
	public static final String DEFAULT_LABELING_GROUP = "default";
	
	private String formula;
	private MLText label;
	private MLText mlTitle;
	private LabelingRuleType labelingRuleType;
	private List<NodeRef> components = new ArrayList<>();
	private List<NodeRef> replacements = new ArrayList<>();
	private Boolean isActive = true;
	private List<String> groups;
	private List<String> locales;
	private SynchronisableState synchronisableState = SynchronisableState.Synchronized;

	
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
	 * <p>Getter for the field <code>groups</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lrGroup")
	public List<String> getGroups() {
		return groups;
	}

	/**
	 * <p>Setter for the field <code>groups</code>.</p>
	 *
	 * @param groups a {@link java.util.List} object.
	 */
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	
	/**
	 * <p>Getter for the field <code>locales</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lrLocales")
	public List<String> getLocales() {
		return locales;
	}

	/**
	 * <p>Setter for the field <code>locales</code>.</p>
	 *
	 * @param locales a {@link java.util.List} object.
	 */
	public void setLocales(List<String> locales) {
		this.locales = locales;
	}

	/**
	 * <p>Getter for the field <code>label</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	@AlfProp
	@AlfMlText
	@AlfQname(qname="bcpg:lrLabel")
	public MLText getLabel() {
		return label;
	}

	/**
	 * <p>Setter for the field <code>label</code>.</p>
	 *
	 * @param label a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public void setLabel(MLText label) {
		this.label = label;
	}
	
	
	/**
	 * <p>Getter for the field <code>isActive</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lrIsActive")
	public Boolean getIsActive() {
		return isActive;
	}

	/**
	 * <p>Setter for the field <code>isActive</code>.</p>
	 *
	 * @param isActive a {@link java.lang.Boolean} object.
	 */
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * <p>Getter for the field <code>formula</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lrFormula")
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
	 * <p>Getter for the field <code>components</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:lrComponents")
	public List<NodeRef> getComponents() {
		return components;
	}

	/**
	 * <p>Setter for the field <code>components</code>.</p>
	 *
	 * @param components a {@link java.util.List} object.
	 */
	public void setComponents(List<NodeRef> components) {
		this.components = components;
	}

	/**
	 * <p>Getter for the field <code>replacements</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname="bcpg:lrReplacements")
	public List<NodeRef> getReplacements() {
		return replacements;
	}

	/**
	 * <p>Setter for the field <code>replacements</code>.</p>
	 *
	 * @param replacements a {@link java.util.List} object.
	 */
	public void setReplacements(List<NodeRef> replacements) {
		this.replacements = replacements;
	}

	/**
	 * <p>Getter for the field <code>labelingRuleType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.constraints.LabelingRuleType} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:lrType")
	public LabelingRuleType getLabelingRuleType() {
		return labelingRuleType;
	}

	/**
	 * <p>Setter for the field <code>labelingRuleType</code>.</p>
	 *
	 * @param labelingRuleType a {@link fr.becpg.repo.product.data.constraints.LabelingRuleType} object.
	 */
	public void setLabelingRuleType(LabelingRuleType labelingRuleType) {
		this.labelingRuleType = labelingRuleType;
	}

	
	/**
	 * <p>Getter for the field <code>synchronisableState</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.SynchronisableState} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname = "bcpg:lrSyncState")
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
	 * <p>Constructor for LabelingRuleListDataItem.</p>
	 */
	public LabelingRuleListDataItem() {
		super();
	}
	
	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelingRuleListDataItem} object
	 */
	public static LabelingRuleListDataItem build() {
		return new LabelingRuleListDataItem();
	}

	
	/**
	 * <p>withName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelingRuleListDataItem} object
	 */
	public LabelingRuleListDataItem withName(String name) {
		this.name = name;
		return this;
	}
	
	/**
	 * <p>withLabel.</p>
	 *
	 * @param label a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelingRuleListDataItem} object
	 * @since 23.4.2.22
	 */
	public LabelingRuleListDataItem withLabel(MLText label) {
		this.label = label;
		return this;
	}
	
	
	/**
	 * <p>withFormula.</p>
	 *
	 * @param formula a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelingRuleListDataItem} object
	 */
	public LabelingRuleListDataItem withFormula(String formula) {
		this.formula = formula;
		return this;
	}
	
	/**
	 * <p>withLabelingRuleType.</p>
	 *
	 * @param labelingRuleType a {@link fr.becpg.repo.product.data.constraints.LabelingRuleType} object
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelingRuleListDataItem} object
	 */
	public LabelingRuleListDataItem withLabelingRuleType(LabelingRuleType labelingRuleType) {
		this.labelingRuleType = labelingRuleType;
		return this;
	}

	/**
	 * <p>withComponents.</p>
	 *
	 * @param components a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelingRuleListDataItem} object
	 */
	public LabelingRuleListDataItem withComponents( List<NodeRef> components) {
		this.components = components;
		return this;
	}
	

	/**
	 * <p>withReplacements.</p>
	 *
	 * @param replacements a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.product.data.productList.LabelingRuleListDataItem} object
	 */
	public LabelingRuleListDataItem withReplacements( List<NodeRef> replacements) {
		this.replacements = replacements;
		return this;
	}


	/**
	 * <p>update.</p>
	 *
	 * @param target a {@link fr.becpg.repo.product.data.productList.LabelingRuleListDataItem} object.
	 */
	public void update(LabelingRuleListDataItem target) {
		this.formula = target.formula;
		this.label = target.label;
		this.labelingRuleType = target.labelingRuleType;
		this.components = target.components;
		this.replacements = target.replacements;
		this.locales = target.locales;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((components == null) ? 0 : components.hashCode());
		result = prime * result + ((formula == null) ? 0 : formula.hashCode());
		result = prime * result + ((groups == null) ? 0 : groups.hashCode());
		result = prime * result + ((isActive == null) ? 0 : isActive.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((labelingRuleType == null) ? 0 : labelingRuleType.hashCode());
		result = prime * result + ((locales == null) ? 0 : locales.hashCode());
		result = prime * result + ((replacements == null) ? 0 : replacements.hashCode());
		result = prime * result + ((synchronisableState == null) ? 0 : synchronisableState.hashCode());
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
		LabelingRuleListDataItem other = (LabelingRuleListDataItem) obj;
		if (components == null) {
			if (other.components != null)
				return false;
		} else if (!components.equals(other.components))
			return false;
		if (formula == null) {
			if (other.formula != null)
				return false;
		} else if (!formula.equals(other.formula))
			return false;
		if (groups == null) {
			if (other.groups != null)
				return false;
		} else if (!groups.equals(other.groups))
			return false;
		if (isActive == null) {
			if (other.isActive != null)
				return false;
		} else if (!isActive.equals(other.isActive))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (labelingRuleType != other.labelingRuleType)
			return false;
		if (locales == null) {
			if (other.locales != null)
				return false;
		} else if (!locales.equals(other.locales))
			return false;
		if (replacements == null) {
			if (other.replacements != null)
				return false;
		} else if (!replacements.equals(other.replacements))
			return false;
		if (synchronisableState != other.synchronisableState)
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "LabelingRuleListDataItem [formula=" + formula + ", label=" + label + ", labelingRuleType=" + labelingRuleType + ", components="
				+ components + ", replacements=" + replacements + ", isActive=" + isActive + ", groups=" + groups + ", locales=" + locales
				+ ", synchronisableState=" + synchronisableState + "]";
	}



	

}
