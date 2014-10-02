package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractManualDataItem;

@AlfType
@AlfQname(qname = "mpm:resourceParamList")
public class ResourceParamListItem extends AbstractManualDataItem {

	private String paramType;
	private String paramValue;
	
	private NodeRef step;	
	private NodeRef param;
	private NodeRef resource;
	
	
	@AlfProp
	@AlfQname(qname="mpm:rplParamType")
	public String getParamType() {
		return paramType;
	}
	public void setParamType(String paramType) {
		this.paramType = paramType;
	}
	
	@AlfProp
	@AlfQname(qname="mpm:rplParamValue")
	public String getParamValue() {
		return paramValue;
	}
	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}
	
	@AlfSingleAssoc
	@AlfQname(qname="mpm:rplStepRef")
	public NodeRef getStep() {
		return step;
	}
	public void setStep(NodeRef step) {
		this.step = step;
	}
	
	@AlfSingleAssoc
	@AlfQname(qname="mpm:rplParamRef")
	public NodeRef getParam() {
		return param;
	}
	public void setParam(NodeRef param) {
		this.param = param;
	}
	
	@AlfSingleAssoc
	@AlfQname(qname="mpm:rplResourceRef")
	public NodeRef getResource() {
		return resource;
	}
	public void setResource(NodeRef resource) {
		this.resource = resource;
	}
	
	
	public ResourceParamListItem() {
		super();
	}
	
	
	@Override
	public String toString() {
		return "ResourceParamListItem [paramType=" + paramType + ", paramValue=" + paramValue + ", step=" + step + ", param=" + param + ", resource="
				+ resource + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((param == null) ? 0 : param.hashCode());
		result = prime * result + ((paramType == null) ? 0 : paramType.hashCode());
		result = prime * result + ((paramValue == null) ? 0 : paramValue.hashCode());
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + ((step == null) ? 0 : step.hashCode());
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
		ResourceParamListItem other = (ResourceParamListItem) obj;
		if (param == null) {
			if (other.param != null)
				return false;
		} else if (!param.equals(other.param))
			return false;
		if (paramType == null) {
			if (other.paramType != null)
				return false;
		} else if (!paramType.equals(other.paramType))
			return false;
		if (paramValue == null) {
			if (other.paramValue != null)
				return false;
		} else if (!paramValue.equals(other.paramValue))
			return false;
		if (resource == null) {
			if (other.resource != null)
				return false;
		} else if (!resource.equals(other.resource))
			return false;
		if (step == null) {
			if (other.step != null)
				return false;
		} else if (!step.equals(other.step))
			return false;
		return true;
	}
	
	
	
	
}
