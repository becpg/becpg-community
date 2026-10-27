package fr.becpg.repo.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * <p>PasswordStrengthRuleTest class.</p>
 *
 * Covers the security policy every password must match: length, required character classes and
 * what counts as a special character.
 *
 * @author valentin
 */
public class PasswordStrengthRuleTest {

    /** 13 characters holding an uppercase letter, lowercase letters and digits, but no special character. */
    private static final String BASE_WITHOUT_SPECIAL = "Abcdefghij123";

    private static final String VALID_PASSWORD = BASE_WITHOUT_SPECIAL + "!";

    private static final String SPECIAL_CHARS_OUTSIDE_FORMER_ALPHABET = "?~,;:'/\\|<>\"`";

    @Test
    public void testAcceptsSpecialCharacterOutsideFormerAlphabet() {
        for (char special : SPECIAL_CHARS_OUTSIDE_FORMER_ALPHABET.toCharArray()) {
            assertTrue("'" + special + "' must count as a special character",
                    PasswordStrengthRule.isSatisfiedBy(BASE_WITHOUT_SPECIAL + special));
        }
    }

    @Test
    public void testAcceptsEveryGeneratorSpecialCharacter() {
        for (char special : SecurePasswordGenerator.SPECIAL.toCharArray()) {
            assertTrue("Generator special character '" + special + "' must be accepted by the policy",
                    PasswordStrengthRule.isSatisfiedBy(BASE_WITHOUT_SPECIAL + special));
        }
    }

    @Test
    public void testRejectsWhitespaceAsSpecialCharacter() {
        assertTrue("A visible special character must be accepted", PasswordStrengthRule.isSatisfiedBy(VALID_PASSWORD));
        assertFalse("A space must not count as a special character", PasswordStrengthRule.isSatisfiedBy(BASE_WITHOUT_SPECIAL + " "));
        assertFalse("A tab must not count as a special character", PasswordStrengthRule.isSatisfiedBy(BASE_WITHOUT_SPECIAL + "\t"));
    }

    @Test
    public void testRejectsNonAsciiAsSpecialCharacter() {
        assertTrue("An ASCII special character must be accepted", PasswordStrengthRule.isSatisfiedBy(VALID_PASSWORD));
        assertFalse("A non ASCII symbol must not count as a special character",
                PasswordStrengthRule.isSatisfiedBy(BASE_WITHOUT_SPECIAL + "\u20AC"));
    }

    @Test
    public void testRejectsPasswordShorterThanPolicy() {
        assertTrue("A 14 character password must be accepted", PasswordStrengthRule.isSatisfiedBy(VALID_PASSWORD));
        assertFalse("A 13 character password must be rejected", PasswordStrengthRule.isSatisfiedBy(VALID_PASSWORD.substring(1)));
    }

    @Test
    public void testRejectsPasswordMissingACharacterClass() {
        assertTrue("A password holding every class must be accepted", PasswordStrengthRule.isSatisfiedBy(VALID_PASSWORD));
        assertFalse("A password without uppercase letter must be rejected", PasswordStrengthRule.isSatisfiedBy("abcdefghij123!"));
        assertFalse("A password without lowercase letter must be rejected", PasswordStrengthRule.isSatisfiedBy("ABCDEFGHIJ123!"));
        assertFalse("A password without digit must be rejected", PasswordStrengthRule.isSatisfiedBy("Abcdefghijklm!"));
        assertFalse("A password without special character must be rejected", PasswordStrengthRule.isSatisfiedBy("Abcdefghij1234"));
    }

    @Test
    public void testRejectsNullPassword() {
        assertTrue("A valid password must be accepted", PasswordStrengthRule.isSatisfiedBy(VALID_PASSWORD));
        assertFalse("A null password must be rejected", PasswordStrengthRule.isSatisfiedBy(null));
    }

}
