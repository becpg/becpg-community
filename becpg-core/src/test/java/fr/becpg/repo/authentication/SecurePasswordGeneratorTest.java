package fr.becpg.repo.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntPredicate;

import org.junit.Test;

/**
 * <p>SecurePasswordGeneratorTest class.</p>
 *
 * Covers the security policy the generated passwords must match: length, required character
 * classes and the special character alphabet.
 *
 * @author valentin
 */
public class SecurePasswordGeneratorTest {

    private static final int SAMPLE_SIZE = 500;

    private static final int EXPECTED_LENGTH = 14;

    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+[]{}.";

    @Test
    public void testGeneratedPasswordMatchesPolicyLength() {
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            assertEquals("Generated password must hold " + EXPECTED_LENGTH + " characters", EXPECTED_LENGTH,
                    SecurePasswordGenerator.generatePassword().length());
        }
    }

    @Test
    public void testGeneratedPasswordHoldsEveryRequiredCharacterClass() {
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            String password = SecurePasswordGenerator.generatePassword();

            assertTrue("Generated password must hold an uppercase letter", holdsChar(password, Character::isUpperCase));
            assertTrue("Generated password must hold a lowercase letter", holdsChar(password, Character::isLowerCase));
            assertTrue("Generated password must hold a digit", holdsChar(password, Character::isDigit));
            assertTrue("Generated password must hold a special character", holdsChar(password, SecurePasswordGeneratorTest::isSpecial));
        }
    }

    @Test
    public void testGeneratedPasswordHoldsOnlyAllowedCharacters() {
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            String password = SecurePasswordGenerator.generatePassword();

            for (int position = 0; position < password.length(); position++) {
                assertTrue("Generated password must not hold a character outside the policy alphabet",
                        isAllowed(password.charAt(position)));
            }
        }
    }

    @Test
    public void testDotBelongsToTheSpecialCharacterAlphabet() {
        assertTrue("The dot must be drawn as a special character", isDrawnOverSamples('.'));
    }

    @Test
    public void testGeneratedPasswordsAreNeverRepeated() {
        Set<String> passwords = new HashSet<>();

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            passwords.add(SecurePasswordGenerator.generatePassword());
        }

        assertEquals("Generated passwords must all differ", SAMPLE_SIZE, passwords.size());
    }

    /**
     * The generator draws at random, so a single password proves nothing about the alphabet: a
     * character present in the alphabet is drawn over the sample with a probability of 1 - 1e-39.
     */
    private boolean isDrawnOverSamples(char expected) {
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            if (SecurePasswordGenerator.generatePassword().indexOf(expected) >= 0) {
                return true;
            }
        }

        return false;
    }

    private boolean holdsChar(String password, IntPredicate predicate) {
        return password.chars().anyMatch(predicate);
    }

    private static boolean isSpecial(int character) {
        return SPECIAL_CHARS.indexOf(character) >= 0;
    }

    private static boolean isAllowed(char character) {
        return Character.isUpperCase(character) || Character.isLowerCase(character) || Character.isDigit(character)
                || isSpecial(character);
    }

}
