package fr.becpg.repo.product.data.document;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.model.DocumentEffectivityType;
import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * DocumentTypeItem class.
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:documentType")
@AlfCacheable(isCharact = true)
public class DocumentTypeItem extends BeCPGDataObject {

	private static final long serialVersionUID = 4814381673959719201L;

	private String charactName;
	private Boolean isMandatory;
	private DocumentEffectivityType effectivityType;
	private Integer autoExpirationDelay;
	private String formula;
	private String nameFormat;
	private String destPath;

	private List<NodeRef> linkedCharactRefs;
	private List<String>  linkedTypes;
	private List<NodeRef> linkedHierarchy;
	private List<NodeRef> subsidiaryRefs;
	private List<NodeRef> plants;



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
	
	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:docTypeLinkedCharacts")
	public List<NodeRef> getLinkedCharactRefs() {
		return linkedCharactRefs;
	}

	public void setLinkedCharactRefs(List<NodeRef> linkedCharactRefs) {
		this.linkedCharactRefs = linkedCharactRefs;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:docTypeLinkedTypes")
	public List<String> getLinkedTypes() {
		return linkedTypes;
	}

	public void setLinkedTypes(List<String> linkedTypes) {
		this.linkedTypes = linkedTypes;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:docTypeLinkedHierarchy")
	public List<NodeRef> getLinkedHierarchy() {
		return linkedHierarchy;
	}

	public void setLinkedHierarchy(List<NodeRef> linkedHierarchy) {
		this.linkedHierarchy = linkedHierarchy;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:docTypeDestPath")
	public String getDestPath() {
		return destPath;
	}

	public void setDestPath(String destPath) {
		this.destPath = destPath;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:subsidiaryRefs")
	public List<NodeRef> getSubsidiaryRefs() {
		return subsidiaryRefs;
	}

	public void setSubsidiaryRefs(List<NodeRef> subsidiaryRefs) {
		this.subsidiaryRefs = subsidiaryRefs;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "bcpg:plants")
	public List<NodeRef> getPlants() {
		return plants;
	}

	public void setPlants(List<NodeRef> plants) {
		this.plants = plants;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:docTypeIsMandatory")
	public Boolean getIsMandatory() {
		return isMandatory;
	}

	public void setIsMandatory(Boolean isMandatory) {
		this.isMandatory = isMandatory;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:docTypeEffectivityType")
	public DocumentEffectivityType getEffectivityType() {
		return effectivityType;
	}

	public void setEffectivityType(DocumentEffectivityType effectivityType) {
		this.effectivityType = effectivityType;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:docTypeAutoExpirationDelay")
	public Integer getAutoExpirationDelay() {
		return autoExpirationDelay;
	}

	public void setAutoExpirationDelay(Integer autoExpirationDelay) {
		this.autoExpirationDelay = autoExpirationDelay;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:docTypeFormula")
	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:docTypeNameFormat")
	public String getNameFormat() {
		return nameFormat;
	}

	public void setNameFormat(String nameFormat) {
		this.nameFormat = nameFormat;
	}

    /**
     * Default constructor.
     */
    public DocumentTypeItem() {
        super();
    }

    /**
     * Creates a new builder instance for DocumentTypeItem.
     *
     * @return a new DocumentTypeItem instance for method chaining
     */
    public static DocumentTypeItem builder() {
        return new DocumentTypeItem();
    }

    /**
     * Sets the character name and returns the instance for method chaining.
     *
     * @param charactName the character name to set
     * @return this instance for method chaining
     */
    public DocumentTypeItem withCharactName(String charactName) {
        this.charactName = charactName;
        return this;
    }

    /**
     * Sets the mandatory flag and returns the instance for method chaining.
     *
     * @param isMandatory true if the document type is mandatory
     * @return this instance for method chaining
     */
    public DocumentTypeItem withIsMandatory(Boolean isMandatory) {
        this.isMandatory = isMandatory;
        return this;
    }

    /**
     * Sets the effectivity type and returns the instance for method chaining.
     *
     * @param effectivityType the effectivity type to set
     * @return this instance for method chaining
     */
    public DocumentTypeItem withEffectivityType(DocumentEffectivityType effectivityType) {
        this.effectivityType = effectivityType;
        return this;
    }

    /**
     * Sets the auto expiration delay and returns the instance for method chaining.
     *
     * @param autoExpirationDelay the auto expiration delay in days
     * @return this instance for method chaining
     */
    public DocumentTypeItem withAutoExpirationDelay(Integer autoExpirationDelay) {
        this.autoExpirationDelay = autoExpirationDelay;
        return this;
    }

    /**
     * Sets the formula and returns the instance for method chaining.
     *
     * @param formula the formula to set
     * @return this instance for method chaining
     */
    public DocumentTypeItem withFormula(String formula) {
        this.formula = formula;
        return this;
    }

    /**
     * Sets the name format and returns the instance for method chaining.
     *
     * @param nameFormat the name format to set
     * @return this instance for method chaining
     */
    public DocumentTypeItem withNameFormat(String nameFormat) {
        this.nameFormat = nameFormat;
        return this;
    }
    
    /**
     * Sets the linked characteristic references and returns the instance for method chaining.
     *
     * @param linkedCharactRefs the list of linked characteristic references
     * @return this instance for method chaining
     */
    public DocumentTypeItem withLinkedCharactRefs(List<NodeRef> linkedCharactRefs) {
        this.linkedCharactRefs = linkedCharactRefs;
        return this;
    }

    /**
     * Sets the linked types and returns the instance for method chaining.
     *
     * @param linkedTypes the list of linked types
     * @return this instance for method chaining
     */
    public DocumentTypeItem withLinkedTypes(List<String> linkedTypes) {
        this.linkedTypes = linkedTypes;
        return this;
    }

    /**
     * Sets the linked hierarchy and returns the instance for method chaining.
     *
     * @param linkedHierarchy the list of linked hierarchy nodes
     * @return this instance for method chaining
     */
    public DocumentTypeItem withLinkedHierarchy(List<NodeRef> linkedHierarchy) {
        this.linkedHierarchy = linkedHierarchy;
        return this;
    }

    /**
     * Sets the destination path and returns the instance for method chaining.
     *
     * @param destPath the destination path to set
     * @return this instance for method chaining
     */
    public DocumentTypeItem withDestPath(String destPath) {
        this.destPath = destPath;
        return this;
    }

    /**
     * Sets the subsidiary references and returns the instance for method chaining.
     *
     * @param subsidiaryRefs the list of subsidiary references
     * @return this instance for method chaining
     */
    public DocumentTypeItem withSubsidiaryRefs(List<NodeRef> subsidiaryRefs) {
        this.subsidiaryRefs = subsidiaryRefs;
        return this;
    }

    /**
     * Sets the plant references and returns the instance for method chaining.
     *
     * @param plants the list of plant references
     * @return this instance for method chaining
     */
    public DocumentTypeItem withPlants(List<NodeRef> plants) {
        this.plants = plants;
        return this;
    }

    /**
     * Checks if this document type is synchronized with any external systems.
     * A document type is considered synchronized if it has any linked hierarchy,
     * linked types, linked characteristic references, subsidiary references, or plants.
     *
     * @return true if the document type is synchronized, false otherwise
     */
    public boolean isSynchronisedDocumentType() {
        return this.linkedHierarchy != null && !this.linkedHierarchy.isEmpty()  
            || this.linkedTypes != null && !this.linkedTypes.isEmpty()
            || this.linkedCharactRefs != null && !this.linkedCharactRefs.isEmpty()
            || this.subsidiaryRefs != null && !this.subsidiaryRefs.isEmpty()
            || this.plants != null && !this.plants.isEmpty();
    }


	/**
	 * Returns a hash code value for this document type item.
	 *
	 * @return a hash code value for this object
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(autoExpirationDelay, charactName, destPath, effectivityType, formula, isMandatory, linkedCharactRefs,
				linkedHierarchy, linkedTypes, nameFormat, plants, subsidiaryRefs);
		return result;
	}

	/**
	 * Indicates whether some other object is "equal to" this one.
     * Two DocumentTypeItem objects are considered equal if they have the same values
     * for all their properties.
     *
     * @param obj the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentTypeItem other = (DocumentTypeItem) obj;
		return Objects.equals(autoExpirationDelay, other.autoExpirationDelay) && Objects.equals(charactName, other.charactName)
				&& Objects.equals(destPath, other.destPath) && effectivityType == other.effectivityType && Objects.equals(formula, other.formula)
				&& Objects.equals(isMandatory, other.isMandatory) && Objects.equals(linkedCharactRefs, other.linkedCharactRefs)
				&& Objects.equals(linkedHierarchy, other.linkedHierarchy) && Objects.equals(linkedTypes, other.linkedTypes)
				&& Objects.equals(nameFormat, other.nameFormat) && Objects.equals(plants, other.plants)
				&& Objects.equals(subsidiaryRefs, other.subsidiaryRefs);
	}

	/**
     * Returns a string representation of this document type item.
     * The string representation includes all the properties of the document type.
     *
     * @return a string representation of this document type item
     */
	@Override
	public String toString() {
		return "DocumentTypeItem [isMandatory=" + isMandatory + ", effectivityType=" + effectivityType + ", autoExpirationDelay="
				+ autoExpirationDelay + ", formula=" + formula + ", nameFormat=" + nameFormat + ", linkedCharactRefs=" + linkedCharactRefs
				+ ", linkedTypes=" + linkedTypes + ", nodeRef=" + nodeRef + ", linkedHierarchy=" + linkedHierarchy + ", parentNodeRef="
				+ parentNodeRef + ", destPath=" + destPath + ", name=" + name + ", aspects=" + aspects + ", subsidiaryRefs=" + subsidiaryRefs
				+ ", plants=" + plants + ", aspectsToRemove=" + aspectsToRemove + ", extraProperties=" + extraProperties + ", isTransient="
				+ isTransient + ", getLinkedCharactRefs()=" + getLinkedCharactRefs() + ", getLinkedTypes()=" + getLinkedTypes() + ", getDbHashCode()="
				+ getDbHashCode() + ", getLinkedHierarchy()=" + getLinkedHierarchy() + ", getDestPath()=" + getDestPath() + ", getSubsidiaryRefs()="
				+ getSubsidiaryRefs() + ", getPlants()=" + getPlants() + ", isMandatory()=" + getIsMandatory() + ", getEffectivityType()="
				+ getEffectivityType() + ", getAutoExpirationDelay()=" + getAutoExpirationDelay() + ", getNodeRef()=" + getNodeRef()
				+ ", getFormula()=" + getFormula() + ", getNameFormat()=" + getNameFormat() + ", getName()=" + getName() + ", hashCode()="
				+ hashCode() + ", getParentNodeRef()=" + getParentNodeRef() + ", getAspects()=" + getAspects() + ", getAspectsToRemove()="
				+ getAspectsToRemove() + ", getExtraProperties()=" + getExtraProperties() + ", isTransient()=" + isTransient() + ", toString()="
				+ super.toString() + ", getClass()=" + getClass() + "]";
	}

}
