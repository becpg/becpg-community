package fr.becpg.repo.product.data;

import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;

@AlfType
@AlfQname(qname = "bcpg:logisticUnit")
public class LogisticUnitData extends ProductData {
	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public static LogisticUnitData build() {
		return new LogisticUnitData();
	}

	/**
	 * <p>withName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.product.data.FinishedProductData} object
	 */
	public LogisticUnitData withName(String name) {
		setName(name);
		return this;
	}
}
