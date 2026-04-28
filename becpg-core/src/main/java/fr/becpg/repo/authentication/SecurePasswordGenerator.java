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
 * <li>Length: 14 characters.</li>
 * <li>Contains at least one uppercase letter ({@code A-Z}).</li>
 * <li>Contains at least one lowercase letter ({@code a-z}).</li>
 * <li>Contains at least one digit ({@code 0-9}).</li>
 * <li>Contains at least one special character from: {@code !@#$%^&*()-_=+[]{} }.</li>
 * </ul>
 *
 * @author matthieu
 */
public class SecurePasswordGenerator {

	private static final SecureRandom RANDOM = new SecureRandom();
	private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String DIGIT = "0123456789";
	private static final String SPECIAL = "!@#$%^&*()-_=+[]{}";
	private static final String ALL = UPPER + LOWER + DIGIT + SPECIAL;
	
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

	    for (int i = 0; i < 10; i++) {
	        password.append(randomChar(ALL));
	    }

	    return shuffleString(password.toString());
	}

	private static char randomChar(String chars) {
	    return chars.charAt(RANDOM.nextInt(chars.length()));
	}
	
	private static String shuffleString(String input) {
		List<Character> characters = input.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
		Collections.shuffle(characters);
		return characters.stream().map(String::valueOf).collect(Collectors.joining());
	}

}
