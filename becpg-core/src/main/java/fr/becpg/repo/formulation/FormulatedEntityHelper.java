package fr.becpg.repo.formulation;

/**
 * <p>FormulatedEntityHelper class.</p>
 *
 * @author matthieu
 */
public class FormulatedEntityHelper {
	
	private FormulatedEntityHelper() {
		
	}

	/**
	 * <p>incrementReformulateCount.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.formulation.FormulatedEntity} object
	 */
	public static void incrementReformulateCount(FormulatedEntity entity) {
		if (entity.getReformulateCount() == null) {
			entity.setReformulateCount(1);
		} else if (entity.getReformulateCount() < 3) {
			entity.setReformulateCount(entity.getReformulateCount() + 1);
		}
	}
	
	/**
	 * <p>isLastFormulation.</p>
	 *
	 * @param entity a {@link fr.becpg.repo.formulation.FormulatedEntity} object
	 * @return a boolean
	 */
	public static boolean isLastFormulation(FormulatedEntity entity) {
		return entity.getReformulateCount() == null || entity.getReformulateCount().equals(entity.getCurrentReformulateCount());
	}
}
