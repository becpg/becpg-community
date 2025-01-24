/*
 *
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.CopiableDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;


/**
 * <p>ToxListDataItem class.</p>
 *
 * @author matthieu
 */
@AlfType
@AlfQname(qname = "bcpg:toxList")
public class ToxListDataItem extends AbstractManualDataItem implements SimpleCharactDataItem, AspectAwareDataItem {

	private static final long serialVersionUID = 8297326459126736070L;

	private NodeRef tox;

	private Double value;

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
	}

	/**
	 * <p>Getter for the field <code>tox</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:toxListTox")
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

}
