package fr.becpg.repo.product.data.document;

import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:documentType")
@AlfCacheable(isCharact = true)
public class DocumentTypeItem extends BeCPGDataObject {

	private static final long serialVersionUID = 4814381673959719201L;

	public enum DocumentEffectivityType {
		NONE, AUTO
	}


	private String charactName;

	private Boolean isMandatory;
	private DocumentEffectivityType effectivityType;
	private Integer autoExpirationDelay;
	private String formula;
	private String nameFormat;

	private List<NodeRef> linkedCharactRefs;
	private List<String> linkedTypes;
	private List<NodeRef> linkedHierarchy;
	private String destPath;
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
	public Boolean isMandatory() {
		return isMandatory;
	}

	public void setMandatory(Boolean isMandatory) {
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

    public DocumentTypeItem(){
        super();
    }

    public static DocumentTypeItem builder(){
        return new DocumentTypeItem();
    }

    public DocumentTypeItem withCharactName(String charactName){
        this.charactName = charactName;
        return this;
    }

    public DocumentTypeItem withIsMandatory(Boolean isMandatory){
        this.isMandatory = isMandatory;
        return this;
    }

    public DocumentTypeItem withEffectivityType(DocumentEffectivityType effectivityType){
        this.effectivityType = effectivityType;
        return this;
    }

    public DocumentTypeItem withAutoExpirationDelay(Integer autoExpirationDelay){
        this.autoExpirationDelay = autoExpirationDelay;
        return this;
    }

    public DocumentTypeItem withFormula(String formula){
        this.formula = formula;
        return this;
    }

    public DocumentTypeItem withNameFormat(String nameFormat){
        this.nameFormat = nameFormat;
        return this;
    }
    
    public DocumentTypeItem withLinkedCharactRefs(List<NodeRef> linkedCharactRefs){
        this.linkedCharactRefs = linkedCharactRefs;
        return this;
    }

    public DocumentTypeItem withLinkedTypes(List<String> linkedTypes){
        this.linkedTypes = linkedTypes;
        return this;
    }

    public DocumentTypeItem withLinkedHierarchy(List<NodeRef> linkedHierarchy){
        this.linkedHierarchy = linkedHierarchy;
        return this;
    }

    public DocumentTypeItem withDestPath(String destPath){
        this.destPath = destPath;
        return this;
    }

    public DocumentTypeItem withSubsidiaryRefs(List<NodeRef> subsidiaryRefs){
        this.subsidiaryRefs = subsidiaryRefs;
        return this;
    }

    public DocumentTypeItem withPlants(List<NodeRef> plants){
        this.plants = plants;
        return this;
    }   

    public boolean isSynchronisedDocumentType(){
        return this.linkedHierarchy != null && !this.linkedHierarchy.isEmpty()  
        || this.linkedTypes != null && !this.linkedTypes.isEmpty()
        || this.linkedCharactRefs != null && !this.linkedCharactRefs.isEmpty()
        || this.subsidiaryRefs != null && !this.subsidiaryRefs.isEmpty()
        || this.plants != null && !this.plants.isEmpty();
    }


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + (isMandatory ? 1231 : 1237);
		result = (prime * result) + ((effectivityType == null) ? 0 : effectivityType.hashCode());
		result = (prime * result) + ((autoExpirationDelay == null) ? 0 : autoExpirationDelay.hashCode());
		result = (prime * result) + ((formula == null) ? 0 : formula.hashCode());
		result = (prime * result) + ((nameFormat == null) ? 0 : nameFormat.hashCode());
		result = (prime * result) + ((linkedCharactRefs == null) ? 0 : linkedCharactRefs.hashCode());
		result = (prime * result) + ((linkedTypes == null) ? 0 : linkedTypes.hashCode());
		result = (prime * result) + ((linkedHierarchy == null) ? 0 : linkedHierarchy.hashCode());
		result = (prime * result) + ((destPath == null) ? 0 : destPath.hashCode());
		result = (prime * result) + ((subsidiaryRefs == null) ? 0 : subsidiaryRefs.hashCode());
		return (prime * result) + ((plants == null) ? 0 : plants.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj) || (getClass() != obj.getClass())) {
			return false;
		}
		DocumentTypeItem other = (DocumentTypeItem) obj;
		if (isMandatory != other.isMandatory) {
			return false;
		}
		if (!Objects.equals(effectivityType, other.effectivityType)) {
			return false;
		}
		if (!Objects.equals(autoExpirationDelay, other.autoExpirationDelay)) {
			return false;
		}
		if (!Objects.equals(formula, other.formula)) {
			return false;
		}
		if (!Objects.equals(nameFormat, other.nameFormat)) {
			return false;
		}
		if (!Objects.equals(linkedCharactRefs, other.linkedCharactRefs)) {
			return false;
		}
		if (!Objects.equals(linkedTypes, other.linkedTypes)) {
			return false;
		}
		if (!Objects.equals(linkedHierarchy, other.linkedHierarchy)) {
			return false;
		}
		if (!Objects.equals(destPath, other.destPath)) {
			return false;
		}
		if (!Objects.equals(subsidiaryRefs, other.subsidiaryRefs)) {
			return false;
		}
		if (!Objects.equals(plants, other.plants)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "DocumentTypeItem [isMandatory=" + isMandatory + ", effectivityType=" + effectivityType + ", autoExpirationDelay="
				+ autoExpirationDelay + ", formula=" + formula + ", nameFormat=" + nameFormat + ", linkedCharactRefs=" + linkedCharactRefs
				+ ", linkedTypes=" + linkedTypes + ", nodeRef=" + nodeRef + ", linkedHierarchy=" + linkedHierarchy + ", parentNodeRef="
				+ parentNodeRef + ", destPath=" + destPath + ", name=" + name + ", aspects=" + aspects + ", subsidiaryRefs=" + subsidiaryRefs
				+ ", plants=" + plants + ", aspectsToRemove=" + aspectsToRemove + ", extraProperties=" + extraProperties + ", isTransient="
				+ isTransient + ", getLinkedCharactRefs()=" + getLinkedCharactRefs() + ", getLinkedTypes()=" + getLinkedTypes() + ", getDbHashCode()="
				+ getDbHashCode() + ", getLinkedHierarchy()=" + getLinkedHierarchy() + ", getDestPath()=" + getDestPath() + ", getSubsidiaryRefs()="
				+ getSubsidiaryRefs() + ", getPlants()=" + getPlants() + ", isMandatory()=" + isMandatory() + ", getEffectivityType()="
				+ getEffectivityType() + ", getAutoExpirationDelay()=" + getAutoExpirationDelay() + ", getNodeRef()=" + getNodeRef()
				+ ", getFormula()=" + getFormula() + ", getNameFormat()=" + getNameFormat() + ", getName()=" + getName() + ", hashCode()="
				+ hashCode() + ", getParentNodeRef()=" + getParentNodeRef() + ", getAspects()=" + getAspects() + ", getAspectsToRemove()="
				+ getAspectsToRemove() + ", getExtraProperties()=" + getExtraProperties() + ", isTransient()=" + isTransient() + ", toString()="
				+ super.toString() + ", getClass()=" + getClass() + "]";
	}

}
