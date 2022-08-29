package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;

/**
 * <p>ResourceParamListItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "mpm:resourceParamList")
public class ResourceParamListItem  extends AbstractManualVariantListDataItem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -617071999761977342L;
	private String paramType;
	private String paramValue;
	
	private NodeRef step;	
	private NodeRef param;
	private NodeRef resource;
	private NodeRef product;
	
	
	/**
	 * <p>Getter for the field <code>paramType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="mpm:rplParamType")
	public String getParamType() {
		return paramType;
	}
	/**
	 * <p>Setter for the field <code>paramType</code>.</p>
	 *
	 * @param paramType a {@link java.lang.String} object.
	 */
	public void setParamType(String paramType) {
		this.paramType = paramType;
	}
	
	/**
	 * <p>Getter for the field <code>paramValue</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="mpm:rplParamValue")
	public String getParamValue() {
		return paramValue;
	}
	/**
	 * <p>Setter for the field <code>paramValue</code>.</p>
	 *
	 * @param paramValue a {@link java.lang.String} object.
	 */
	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}
	
	/**
	 * <p>Getter for the field <code>step</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="mpm:rplStepRef")
	public NodeRef getStep() {
		return step;
	}
	/**
	 * <p>Setter for the field <code>step</code>.</p>
	 *
	 * @param step a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setStep(NodeRef step) {
		this.step = step;
	}
	
	/**
	 * <p>Getter for the field <code>param</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="mpm:rplParamRef")
	@DataListIdentifierAttr
	public NodeRef getParam() {
		return param;
	}
	/**
	 * <p>Setter for the field <code>param</code>.</p>
	 *
	 * @param param a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setParam(NodeRef param) {
		this.param = param;
	}
	
	/**
	 * <p>Getter for the field <code>resource</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname="mpm:rplResourceRef")
	public NodeRef getResource() {
		return resource;
	}
	/**
	 * <p>Setter for the field <code>resource</code>.</p>
	 *
	 * @param resource a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setResource(NodeRef resource) {
		this.resource = resource;
	}
	
	
	/**
	 * <p>Getter for the field <code>product</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "mpm:rplProductRef")
	public NodeRef getProduct() {
		return product;
	}

	/**
	 * <p>Setter for the field <code>product</code>.</p>
	 *
	 * @param product a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setProduct(NodeRef product) {
		this.product = product;
	}

	
	/**
	 * <p>Constructor for ResourceParamListItem.</p>
	 */
	public ResourceParamListItem() {
		super();
	}
	
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ResourceParamListItem [paramType=" + paramType + ", paramValue=" + paramValue + ", step=" + step + ", param=" + param + ", resource="
				+ resource + "]";
	}
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((param == null) ? 0 : param.hashCode());
		result = prime * result + ((paramType == null) ? 0 : paramType.hashCode());
		result = prime * result + ((paramValue == null) ? 0 : paramValue.hashCode());
		result = prime * result + ((resource == null) ? 0 : resource.hashCode());
		result = prime * result + ((product == null) ? 0 : product.hashCode());
		result = prime * result + ((step == null) ? 0 : step.hashCode());
		return result;
	}
	/** {@inheritDoc} */
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
		if (product == null) {
			if (other.product != null)
				return false;
		} else if (!product.equals(other.product))
			return false;
		if (step == null) {
			if (other.step != null)
				return false;
		} else if (!step.equals(other.step))
			return false;
		return true;
	}
	
	
	
	
}
