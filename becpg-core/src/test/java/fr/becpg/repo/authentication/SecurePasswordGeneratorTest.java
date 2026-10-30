package fr.becpg.repo.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntPredicate;

import org.jsoup.Jsoup;
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

    private static final int EXPECTED_LENGTH = 15;

    /**
     * Special characters safe to read in a mail and to type back: no {@code &} nor {@code #}, which start HTML
     * character references, no dead key such as {@code ^}, nothing that stops a double-click selection.
     */
    private static final String SPECIAL_CHARS = "@%*-_=+!";

    /** Characters a user easily confuses when typing the password back from a mail. */
    private static final String LOOK_ALIKE_CHARS = "Il1O0";

    private static final int LARGE_SAMPLE_SIZE = 100_000;

    /** Markup the mail templates wrap the password in. */
    private static final String MAIL_PASSWORD_MARKUP = "<b>%s</b>";

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
    public void testAlphabetHoldsNoLookAlikeCharacter() {
        for (char lookAlike : LOOK_ALIKE_CHARS.toCharArray()) {
            assertTrue("Alphabet must not hold the look-alike character '" + lookAlike + "'",
                    SecurePasswordGenerator.ALL.indexOf(lookAlike) < 0);
        }
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
     * The shortest HTML character references a mail client decodes hold three characters ({@code &lt},
     * {@code &#9}), so every triplet of the alphabet reveals a character able to start one.
     */
    @Test
    public void testEveryAlphabetCharacterTripletIsShownUnalteredInHtmlMail() {
        for (char first : SecurePasswordGenerator.ALL.toCharArray()) {
            for (char second : SecurePasswordGenerator.ALL.toCharArray()) {
                for (char third : SecurePasswordGenerator.ALL.toCharArray()) {
                    String triplet = String.valueOf(new char[] { first, second, third });
                    assertEquals("Alphabet character triplet must be shown unaltered once rendered as HTML", triplet,
                            renderAsHtmlMail(triplet));
                }
            }
        }
    }

    @Test
    public void testNoPasswordOfLargeSampleHoldsWhitespaceOrInvisibleCharacter() {
        for (int i = 0; i < LARGE_SAMPLE_SIZE; i++) {
            String password = SecurePasswordGenerator.generatePassword();

            assertTrue("Generated password must not hold a whitespace or invisible character",
                    password.chars().allMatch(SecurePasswordGeneratorTest::isVisible));
        }
    }

    private boolean holdsChar(String password, IntPredicate predicate) {
        return password.chars().anyMatch(predicate);
    }

    private static boolean isSpecial(int character) {
        return SPECIAL_CHARS.indexOf(character) >= 0;
    }

    private static boolean isVisible(int character) {
        return character > ' ' && character < 0x7F;
    }

    /**
     * Renders the password the way a mail client shows it: entities are decoded following the HTML5
     * rules, even without a trailing semicolon, and whitespace is collapsed.
     */
    private static String renderAsHtmlMail(String password) {
        return Jsoup.parse(String.format(MAIL_PASSWORD_MARKUP, password)).body().text();
    }

    private static boolean isAllowed(char character) {
        return Character.isUpperCase(character) || Character.isLowerCase(character) || Character.isDigit(character)
                || isSpecial(character);
    }

}
