package fr.becpg.repo.product.data.productList;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.model.AbstractManualDataItem;
import fr.becpg.repo.variant.model.VariantDataItem;

public abstract class AbstractManualVariantListDataItem extends AbstractManualDataItem implements VariantDataItem {


	private List<NodeRef> variants;

	@AlfProp
	@AlfQname(qname="bcpg:variantIds")
	public List<NodeRef> getVariants() {
		return variants;
	}

	public void setVariants(List<NodeRef> variants) {
		this.variants = variants;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((variants == null) ? 0 : variants.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractManualVariantListDataItem other = (AbstractManualVariantListDataItem) obj;
		if (variants == null) {
			if (other.variants != null)
				return false;
		} else if (!variants.equals(other.variants))
			return false;
		return true;
	}
	

	
}
