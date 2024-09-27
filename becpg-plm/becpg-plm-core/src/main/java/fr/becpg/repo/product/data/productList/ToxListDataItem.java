/*
 *
 */
package fr.becpg.repo.product.data.productList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.BeCPGDataObject;
import fr.becpg.repo.repository.model.CopiableDataItem;
import fr.becpg.repo.repository.model.SimpleCharactDataItem;


@AlfType
@AlfQname(qname = "bcpg:toxList")
public class ToxListDataItem extends BeCPGDataObject implements SimpleCharactDataItem, AspectAwareDataItem {

	private static final long serialVersionUID = 8297326459126736070L;

	private NodeRef tox;

	private Double value;

	public ToxListDataItem() {
	}
	
	public ToxListDataItem(ToxListDataItem other) {
		this.tox = other.tox;
		this.value = other.value;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:toxListTox")
	public NodeRef getTox() {
		return tox;
	}

	public void setTox(NodeRef tox) {
		this.tox = tox;
	}

	@AlfProp
	@AlfQname(qname = "bcpg:toxListValue")
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	@Override
	public CopiableDataItem copy() {
		ToxListDataItem ret = new ToxListDataItem(this);
		ret.setName(null);
		ret.setNodeRef(null);
		ret.setParentNodeRef(null);
		return ret;
	}

	@Override
	public void setCharactNodeRef(NodeRef tox) {
		setTox(tox);
	}

	@Override
	public NodeRef getCharactNodeRef() {
		return getTox();
	}

}
