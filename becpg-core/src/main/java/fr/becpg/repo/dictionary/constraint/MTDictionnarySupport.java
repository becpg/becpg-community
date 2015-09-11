package fr.becpg.repo.dictionary.constraint;

public class MTDictionnarySupport {

	public interface Action {

		void run();

	}

	private static final ThreadLocal<Boolean> threadLocalCache = new ThreadLocal<Boolean>() {
		protected Boolean initialValue() {
			return false;
		}
	};

	public static boolean shouldCleanConstraint() {
		return threadLocalCache.get();
	}

	public static void doInResetContext(Action action) {
		try {
			threadLocalCache.set(true);
			action.run();
		} finally {
			threadLocalCache.remove();
		}
	}
}
