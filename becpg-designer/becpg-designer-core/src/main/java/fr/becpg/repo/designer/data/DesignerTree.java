/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
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
package fr.becpg.repo.designer.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * Tree model representing the xml tree.
 */
public class DesignerTree {
	
	private String type;
	private String formId;
	private String formKind;
	private String nodeRef;
	private String name;
	private String title;
	private String description;
	private String subType;
	private Boolean hasError = false; 
	private String formType;
	
	
	public DesignerTree(String nodeRef) {
		super();
		this.nodeRef = nodeRef;
	}


	private List<DesignerTree> childrens = new ArrayList<>();

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the nodeRef
	 */
	public String getNodeRef() {
		return nodeRef;
	}

	/**
	 * @param nodeRef the nodeRef to set
	 */
	public void setNodeRef(String nodeRef) {
		this.nodeRef = nodeRef;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the childrens
	 */
	public List<DesignerTree> getChildrens() {
		return childrens;
	}

	
	
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param childrens the childrens to set
	 */
	public void setChildrens(List<DesignerTree> childrens) {
		this.childrens = childrens;
	}

	
	
	
	public String getFormKind() {
		return formKind;
	}

	public void setFormKind(String formKind) {
		this.formKind = formKind;
	}

	/**
	 * @return the isDraggable
	 */
	public Boolean getIsDraggable() {
		if("m2:property".equals(type) 
				||  "m2:propertyOverride".equals(type) 
				||  "dsg:formField".equals(type)
				||  "m2:type".equals(type)
				||  "m2:aspect".equals(type)
				||  "m2:association".equals(type)
				||  "m2:childAssociation".equals(type)){
			return true;
		}
		return false;
	}

	public List<String> getAccepts() {
		List<String> ret = new ArrayList<>();
		if ("m2:type".equals(type) || "m2:aspect".equals(type) || "m2:properties".equals(type)
				|| "m2:propertyOverrides".equals(type) ) {
			ret.add("property");
		}
		
		if ("m2:type".equals(type) || "m2:aspect".equals(type) || "m2:associations".equals(type) ) {
			ret.add("association");
		}
		
		if ("m2:type".equals(type) || "m2:aspect".equals(type)){
			ret.add("aspect");
		}
		
		
		if ("dsg:form".equals(type) || "dsg:formSet".equals(type) ||  "dsg:sets".equals(type)) {
			ret.add("set");
		}

		if ("dsg:config".equals(type) || "dsg:configElements".equals(type) ) {	
			ret.add("type");
		}
	
		if ("dsg:form".equals(type) || "dsg:formSet".equals(type) || "dsg:fields".equals(type)) {
			ret.add("association");
			ret.add("property");
			ret.add("field");
			ret.add("aspect");
		}
		
		if ("dsg:formField".equals(type)) {
			ret.add("control");
		}

		return ret;

	}
	
	/**
	 * @return the formId
	 */
	public String getFormId() {
		return formId;
	}

	/**
	 * @param formId the formId to set
	 */
	public void setFormId(String formId) {
		this.formId = formId;
	}

	/**
	 * @return the hasError
	 */
	public Boolean getHasError() {
		return hasError;
	}

	/**
	 * @param hasError the hasError to set
	 */
	public void setHasError(Boolean hasError) {
		this.hasError = hasError;
	}

	/**
	 * @return the subType
	 */
	public String getSubType() {
		return subType;
	}

	/**
	 * @param subType the subType to set
	 */
	public void setSubType(String subType) {
		this.subType = subType;
	}
	

	public String getFormType() {
		return formType;
	}

	public void setFormType(String formType) {
		this.formType = formType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((childrens == null) ? 0 : childrens.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((formId == null) ? 0 : formId.hashCode());
		result = prime * result + ((formKind == null) ? 0 : formKind.hashCode());
		result = prime * result + ((formType == null) ? 0 : formType.hashCode());
		result = prime * result + ((hasError == null) ? 0 : hasError.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((subType == null) ? 0 : subType.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DesignerTree other = (DesignerTree) obj;
		if (childrens == null) {
			if (other.childrens != null)
				return false;
		} else if (!childrens.equals(other.childrens))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (formId == null) {
			if (other.formId != null)
				return false;
		} else if (!formId.equals(other.formId))
			return false;
		if (formKind == null) {
			if (other.formKind != null)
				return false;
		} else if (!formKind.equals(other.formKind))
			return false;
		if (formType == null) {
			if (other.formType != null)
				return false;
		} else if (!formType.equals(other.formType))
			return false;
		if (hasError == null) {
			if (other.hasError != null)
				return false;
		} else if (!hasError.equals(other.hasError))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (subType == null) {
			if (other.subType != null)
				return false;
		} else if (!subType.equals(other.subType))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DesignerTree [type=" + type + ", formId=" + formId + ", formKind=" + formKind + ", nodeRef=" + nodeRef + ", name=" + name + ", title=" + title + ", description="
				+ description + ", subType=" + subType + ", hasError=" + hasError + ", formType=" + formType + ", childrens=" + childrens + "]";
	}

	
	

}
