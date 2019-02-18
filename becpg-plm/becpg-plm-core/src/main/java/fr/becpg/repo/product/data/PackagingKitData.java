/*
 * 
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

@AlfType
@AlfQname(qname = "bcpg:packagingKit")
public class PackagingKitData extends ProductData  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6154279789978582886L;

	
	Integer palletBoxesPerPallet;

	@AlfProp
	@AlfQname(qname = "pack:palletBoxesPerPallet")
	public Integer getPalletBoxesPerPallet() {
		return palletBoxesPerPallet;
	}


	public void setPalletBoxesPerPallet(Integer palletBoxesPerPallet) {
		this.palletBoxesPerPallet = palletBoxesPerPallet;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((palletBoxesPerPallet == null) ? 0 : palletBoxesPerPallet.hashCode());
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
		PackagingKitData other = (PackagingKitData) obj;
		if (palletBoxesPerPallet == null) {
			if (other.palletBoxesPerPallet != null)
				return false;
		} else if (!palletBoxesPerPallet.equals(other.palletBoxesPerPallet))
			return false;
		return true;
	}
	 
	 


}
