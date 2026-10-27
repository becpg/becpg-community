package fr.becpg.repo.authentication;

/**
 * Security policy every password must match, whether generated or typed by the user: at least
 * 14 characters, one uppercase letter, one lowercase letter, one digit and one special character.
 * <p>
 * A special character is any visible ASCII character that is neither a letter nor a digit, so the
 * policy does not depend on the alphabet {@link SecurePasswordGenerator} draws from.
 * </p>
 *
 * @author valentin
 */
final class PasswordStrengthRule {

	private static final int MIN_LENGTH = 14;

	private static final char FIRST_VISIBLE_ASCII = '!';

	private static final char LAST_VISIBLE_ASCII = '~';

	private PasswordStrengthRule() {

	}

	/**
	 * Tells whether the password matches the security policy.
	 *
	 * @param password the password to check, may be null
	 * @return true if the password matches the policy
	 */
	static boolean isSatisfiedBy(String password) {
		return password != null && password.length() >= MIN_LENGTH
				&& password.chars().anyMatch(Character::isUpperCase)
				&& password.chars().anyMatch(Character::isLowerCase)
				&& password.chars().anyMatch(Character::isDigit)
				&& password.chars().anyMatch(PasswordStrengthRule::isSpecial);
	}

	private static boolean isSpecial(int character) {
		return character >= FIRST_VISIBLE_ASCII && character <= LAST_VISIBLE_ASCII && !Character.isLetterOrDigit(character);
	}

}
