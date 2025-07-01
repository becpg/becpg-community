package fr.becpg.repo.formulation;

public class FormulatedEntityHelper {
	
	private FormulatedEntityHelper() {
		
	}

	public static void incrementReformulateCount(FormulatedEntity entity) {
		if (entity.getReformulateCount() == null) {
			entity.setReformulateCount(1);
		} else if (entity.getReformulateCount() < 3) {
			entity.setReformulateCount(entity.getReformulateCount() + 1);
		}
	}
	
	public static boolean isLastFormulation(FormulatedEntity entity) {
		return entity.getReformulateCount() == null || entity.getReformulateCount().equals(entity.getCurrentReformulateCount());
	}
}
