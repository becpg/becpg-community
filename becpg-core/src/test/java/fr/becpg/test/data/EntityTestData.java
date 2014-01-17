package fr.becpg.test.data;

import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "bcpg:entityV2")
public class EntityTestData extends BeCPGDataObject{

	@Override
	public String toString() {
		return "EntityTestData [name=" + name + "]";
	}

}
