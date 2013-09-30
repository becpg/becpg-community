package fr.becpg.repo.product.data.ing;

import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.repo.repository.annotation.AlfMlText;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.model.BeCPGDataObject;

public abstract class AbstractLabelingComponent extends BeCPGDataObject implements LabelingComponent, Comparable<LabelingComponent> {

	protected Double qty = 0d;

	protected MLText legalName;

	@AlfMlText
	@AlfProp
	@AlfQname(qname = "bcpg:legalName")
	public MLText getLegalName() {
		return legalName;
	}

	public void setLegalName(MLText legalName) {
		this.legalName = legalName;
	}

	@Override
	public String getLegalName(Locale locale) {
		if (legalName != null) {
			if (legalName.containsKey(locale)) {
				return legalName.get(locale);
			} else {
				return legalName.getClosestValue(locale);
			}
		}
		return name;
	}

	@Override
	public Double getQty() {
		return qty;
	}

	public void setQty(Double qty) {
		this.qty = qty;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((legalName == null) ? 0 : legalName.hashCode());
		result = prime * result + ((qty == null) ? 0 : qty.hashCode());
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
		AbstractLabelingComponent other = (AbstractLabelingComponent) obj;
		if (legalName == null) {
			if (other.legalName != null)
				return false;
		} else if (!legalName.equals(other.legalName))
			return false;
		if (qty == null) {
			if (other.qty != null)
				return false;
		} else if (!qty.equals(other.qty))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AbstractIng [qty=" + qty + ", legalName=" + legalName + "]";
	}
	



	@Override
	public int compareTo(LabelingComponent lblComponent) {

		if (lblComponent.getQty() != null && this.getQty() != null) {
			return Double.compare(lblComponent.getQty(), this.getQty());
		}
		else if (this.getQty() == null && lblComponent.getQty() != null) {
			return 1; // after
		}
		else if (this.getQty() != null && lblComponent.getQty() == null) {
			return -1; // before
		}
		return 0;// equals

	}

}
