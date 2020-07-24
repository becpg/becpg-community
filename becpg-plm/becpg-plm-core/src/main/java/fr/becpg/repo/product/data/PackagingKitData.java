/*
 * 
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

/**
 * <p>PackagingKitData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:packagingKit")
public class PackagingKitData extends ProductData  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6154279789978582886L;

	
	Integer palletBoxesPerPallet;

	/**
	 * <p>Getter for the field <code>palletBoxesPerPallet</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pack:palletBoxesPerPallet")
	public Integer getPalletBoxesPerPallet() {
		return palletBoxesPerPallet;
	}


	/**
	 * <p>Setter for the field <code>palletBoxesPerPallet</code>.</p>
	 *
	 * @param palletBoxesPerPallet a {@link java.lang.Integer} object.
	 */
	public void setPalletBoxesPerPallet(Integer palletBoxesPerPallet) {
		this.palletBoxesPerPallet = palletBoxesPerPallet;
	}


	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((palletBoxesPerPallet == null) ? 0 : palletBoxesPerPallet.hashCode());
		return result;
	}


	/** {@inheritDoc} */
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
