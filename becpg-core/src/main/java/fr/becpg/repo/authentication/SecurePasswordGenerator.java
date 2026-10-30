package fr.becpg.repo.authentication;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Utility class that generates a random password using a cryptographically strong
 * random source ({@link java.security.SecureRandom}).
 * <p>
 * The generated password has the following characteristics:
 * </p>
 * <ul>
 * <li>Length: 15 characters.</li>
 * <li>Contains at least one uppercase letter ({@code A-Z} but {@code I O}).</li>
 * <li>Contains at least one lowercase letter ({@code a-z} but {@code l}).</li>
 * <li>Contains at least one digit ({@code 2-9}).</li>
 * <li>Contains at least one special character from: {@code @%*-_=+! }.</li>
 * </ul>
 * <p>
 * The password is sent by mail and typed back by the user, so the alphabet excludes characters that
 * alter or blur it: {@code &} and {@code #} start HTML character references a mail client decodes,
 * {@code I l 1 O 0} look alike, {@code ^} is a dead key on AZERTY keyboards and punctuation such as
 * {@code . ( ) [ ]} stops a double-click selection.
 * </p>
 *
 * @author matthieu
 */
public class SecurePasswordGenerator {

	/** Constant <code>RANDOM</code> */
	private static final SecureRandom RANDOM = new SecureRandom();
	/** Constant <code>UPPER="ABCDEFGHJKLMNPQRSTUVWXYZ"</code> */
	private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
	/** Constant <code>LOWER="abcdefghijkmnopqrstuvwxyz"</code> */
	private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
	/** Constant <code>DIGIT="23456789"</code> */
	private static final String DIGIT = "23456789";
	/** Constant <code>SPECIAL="@%*-_=+!"</code> */
	static final String SPECIAL = "@%*-_=+!";
	/** Constant <code>ALL="UPPER + LOWER + DIGIT + SPECIAL"</code> */
	static final String ALL = UPPER + LOWER + DIGIT + SPECIAL;
	/**
	 * Number of characters drawn from {@link #ALL} once each character class holds one: 15 characters in all,
	 * one more than the policy minimum, to make up for the smaller alphabet (about 90 bits of entropy).
	 */
	private static final int RANDOM_CHAR_COUNT = 11;

	/**
	 * <p>Constructor for SecurePasswordGenerator.</p>
	 */
	private SecurePasswordGenerator() {
		
	}

	/**
	 * <p>generatePassword.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public static String generatePassword() {
	    StringBuilder password = new StringBuilder();

	    password.append(randomChar(UPPER));
	    password.append(randomChar(LOWER));
	    password.append(randomChar(DIGIT));
	    password.append(randomChar(SPECIAL));

	    for (int i = 0; i < RANDOM_CHAR_COUNT; i++) {
	        password.append(randomChar(ALL));
	    }

	    return shuffleString(password.toString());
	}

	/**
	 * <p>randomChar.</p>
	 *
	 * @param chars a {@link java.lang.String} object
	 * @return a char
	 */
	private static char randomChar(String chars) {
	    return chars.charAt(RANDOM.nextInt(chars.length()));
	}
	
	/**
	 * <p>shuffleString.</p>
	 *
	 * @param input a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	private static String shuffleString(String input) {
		List<Character> characters = input.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
		Collections.shuffle(characters, RANDOM);
		return characters.stream().map(String::valueOf).collect(Collectors.joining());
	}

}
