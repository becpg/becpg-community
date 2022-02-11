/*
 * 
 */
package fr.becpg.repo.product.data;

import fr.becpg.repo.repository.annotation.AlfCacheable;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.MultiLevelLeaf;

/**
 * <p>RawMaterialData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:rawMaterial")
@MultiLevelLeaf
@AlfCacheable
public class RawMaterialData extends ProductData {

	private static final long serialVersionUID = -2176815295417841030L;



	@Override
	public int hashCode() {
		return super.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}


}
