package fr.becpg.web.authentication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;

/**
 * <p>PortalSessionConfig class.</p>
 *
 * Immutable configuration snapshot for the portal Share-session endpoint
 * ({@link fr.becpg.web.authentication.PortalSessionPost}).
 *
 * The feature is OFF unless <code>becpg.portal.session.enabled</code> is explicitly set
 * to <code>true</code> — either as a JVM system property or as the <code>&lt;enabled&gt;</code>
 * element of the <code>BeCPGPortalSession</code> config block. A deployment that sets
 * neither behaves exactly as it does without this class: the endpoint answers 404 and no
 * decoder, no connector and no session is ever created.
 *
 * Every key can be given twice; the system property always wins, so a secret never has to
 * be shipped inside the war:
 *
 * <pre>
 *   -Dbecpg.portal.session.enabled=true
 *   -Dbecpg.portal.session.sharedSecret=...
 *
 *   &lt;config evaluator="string-compare" condition="BeCPGPortalSession"&gt;
 *      &lt;enabled&gt;true&lt;/enabled&gt;
 *      &lt;issuer&gt;https://auth.example.com/auth/realms/inst1&lt;/issuer&gt;
 *      ...
 *   &lt;/config&gt;
 * </pre>
 *
 * @author matthieu
 */
public class PortalSessionConfig {

	/** Constant <code>CONFIG_CONDITION="BeCPGPortalSession"</code> */
	public static final String CONFIG_CONDITION = "BeCPGPortalSession";

	/** Constant <code>PROPERTY_PREFIX="becpg.portal.session."</code> */
	public static final String PROPERTY_PREFIX = "becpg.portal.session.";

	/** Constant <code>KEY_ENABLED="enabled"</code> — the master switch, false unless explicitly true */
	public static final String KEY_ENABLED = "enabled";

	/** Constant <code>KEY_ISSUER="issuer"</code> — the ONLY trust anchor, never taken from the token */
	public static final String KEY_ISSUER = "issuer";

	/** Constant <code>KEY_AUDIENCES="audiences"</code> — comma separated, empty means reject everything */
	public static final String KEY_AUDIENCES = "audiences";

	/** Constant <code>KEY_AUTHORIZED_PARTIES="authorizedParties"</code> — allowed <code>azp</code> values */
	public static final String KEY_AUTHORIZED_PARTIES = "authorizedParties";

	/** Constant <code>KEY_PRINCIPAL_ATTRIBUTE="principalAttribute"</code> */
	public static final String KEY_PRINCIPAL_ATTRIBUTE = "principalAttribute";

	/**
	 * Constant <code>KEY_TOKEN_TYPE="tokenType"</code> — the expected value of the
	 * <code>typ</code> CLAIM (not the JOSE header, which is always JWT). Keycloak writes
	 * <code>Bearer</code> on access tokens and <code>ID</code> on id tokens, so this is what
	 * stops an id token being traded for a session. There is no way to switch it off.
	 */
	public static final String KEY_TOKEN_TYPE = "tokenType";

	/** Constant <code>KEY_SHARED_SECRET="sharedSecret"</code> — the X-beCPG-Portal-Key value */
	public static final String KEY_SHARED_SECRET = "sharedSecret";

	/** Constant <code>KEY_ALLOWED_ADDRESSES="allowedAddresses"</code> — direct peer allow list, empty denies all */
	public static final String KEY_ALLOWED_ADDRESSES = "allowedAddresses";

	/** Constant <code>KEY_CLOCK_SKEW_SECONDS="clockSkewSeconds"</code> */
	public static final String KEY_CLOCK_SKEW_SECONDS = "clockSkewSeconds";

	/** Constant <code>KEY_MAX_MINTS_PER_MINUTE="maxMintsPerMinute"</code> */
	public static final String KEY_MAX_MINTS_PER_MINUTE = "maxMintsPerMinute";

	/** Constant <code>KEY_MAX_MINTS_PER_MINUTE_PER_USER="maxMintsPerMinutePerUser"</code> */
	public static final String KEY_MAX_MINTS_PER_MINUTE_PER_USER = "maxMintsPerMinutePerUser";

	/** Constant <code>DEFAULT_PRINCIPAL_ATTRIBUTE="preferred_username"</code> */
	public static final String DEFAULT_PRINCIPAL_ATTRIBUTE = "preferred_username";

	/** Constant <code>DEFAULT_TOKEN_TYPE="Bearer"</code> */
	public static final String DEFAULT_TOKEN_TYPE = "Bearer";

	/**
	 * Same margin as {@code BeCPGAIMSFilter}'s token refresh margin: enough to absorb the
	 * clock drift between Share and the identity provider, short enough to be useless to
	 * an attacker replaying an expired token.
	 */
	private static final long DEFAULT_CLOCK_SKEW_SECONDS = 60L;

	private static final int DEFAULT_MAX_MINTS_PER_MINUTE = 60;

	private static final int DEFAULT_MAX_MINTS_PER_MINUTE_PER_USER = 10;

	/** Every key read from the configuration, in the order the operator must supply them. */
	private static final List<String> KEYS = List.of(KEY_ENABLED, KEY_ISSUER, KEY_AUDIENCES, KEY_AUTHORIZED_PARTIES, KEY_PRINCIPAL_ATTRIBUTE,
			KEY_TOKEN_TYPE, KEY_SHARED_SECRET, KEY_ALLOWED_ADDRESSES, KEY_CLOCK_SKEW_SECONDS, KEY_MAX_MINTS_PER_MINUTE,
			KEY_MAX_MINTS_PER_MINUTE_PER_USER);

	private final boolean enabled;

	private final String issuer;

	private final Set<String> audiences;

	private final Set<String> authorizedParties;

	private final String principalAttribute;

	private final String tokenType;

	private final String sharedSecret;

	private final Set<String> allowedAddresses;

	private final long clockSkewSeconds;

	private final int maxMintsPerMinute;

	private final int maxMintsPerMinutePerUser;

	private final List<String> missingKeys;

	/**
	 * <p>Constructor for PortalSessionConfig.</p>
	 *
	 * @param values the raw configuration values, system properties already applied
	 */
	PortalSessionConfig(Map<String, String> values) {
		this.enabled = Boolean.parseBoolean(trimToNull(values.get(KEY_ENABLED)));
		this.issuer = trimToNull(values.get(KEY_ISSUER));
		this.audiences = splitToSet(values.get(KEY_AUDIENCES));
		this.authorizedParties = splitToSet(values.get(KEY_AUTHORIZED_PARTIES));
		String attribute = trimToNull(values.get(KEY_PRINCIPAL_ATTRIBUTE));
		this.principalAttribute = attribute != null ? attribute : DEFAULT_PRINCIPAL_ATTRIBUTE;
		String type = trimToNull(values.get(KEY_TOKEN_TYPE));
		this.tokenType = type != null ? type : DEFAULT_TOKEN_TYPE;
		this.sharedSecret = trimToNull(values.get(KEY_SHARED_SECRET));
		this.allowedAddresses = splitToSet(values.get(KEY_ALLOWED_ADDRESSES));
		this.clockSkewSeconds = parseLong(values.get(KEY_CLOCK_SKEW_SECONDS), DEFAULT_CLOCK_SKEW_SECONDS);
		this.maxMintsPerMinute = (int) parseLong(values.get(KEY_MAX_MINTS_PER_MINUTE), DEFAULT_MAX_MINTS_PER_MINUTE);
		this.maxMintsPerMinutePerUser = (int) parseLong(values.get(KEY_MAX_MINTS_PER_MINUTE_PER_USER), DEFAULT_MAX_MINTS_PER_MINUTE_PER_USER);

		// A half configured feature is a security hole waiting to happen: an empty audience
		// list that "skips" the check, an empty allow list that "allows all". Everything the
		// endpoint needs to refuse properly is therefore mandatory, and a missing value
		// disables the feature outright rather than relaxing a check.
		List<String> missing = new ArrayList<>();
		if (this.enabled) {
			if (this.issuer == null) {
				missing.add(KEY_ISSUER);
			}
			if (this.audiences.isEmpty()) {
				missing.add(KEY_AUDIENCES);
			}
			if (this.authorizedParties.isEmpty()) {
				missing.add(KEY_AUTHORIZED_PARTIES);
			}
			if (this.sharedSecret == null || this.sharedSecret.length() < 32) {
				missing.add(KEY_SHARED_SECRET);
			}
			if (this.allowedAddresses.isEmpty()) {
				missing.add(KEY_ALLOWED_ADDRESSES);
			}
		}
		this.missingKeys = Collections.unmodifiableList(missing);
	}

	/**
	 * Reads the configuration, system properties taking precedence over the
	 * <code>BeCPGPortalSession</code> config block.
	 *
	 * Never throws: a missing config block, a config service that cannot answer, anything
	 * at all, yields a disabled configuration. Nothing about this feature may prevent Share
	 * from serving a request.
	 *
	 * @param configService the Share config service, may be null
	 * @return a {@link fr.becpg.web.authentication.PortalSessionConfig} object
	 */
	public static PortalSessionConfig read(ConfigService configService) {
		Map<String, String> values = new LinkedHashMap<>();

		for (String key : KEYS) {
			values.put(key, readConfigElement(configService, key));
		}
		for (String key : KEYS) {
			String property = trimToNull(System.getProperty(PROPERTY_PREFIX + key));
			if (property != null) {
				values.put(key, property);
			}
		}

		return new PortalSessionConfig(values);
	}

	private static String readConfigElement(ConfigService configService, String key) {
		if (configService == null) {
			return null;
		}
		try {
			Config config = configService.getConfig(CONFIG_CONDITION);
			if (config == null) {
				return null;
			}
			ConfigElement element = config.getConfigElement(key);
			return element != null ? trimToNull(element.getValue()) : null;
		} catch (Exception e) { // NOSONAR - a broken config block must not break Share
			return null;
		}
	}

	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}

	private static Set<String> splitToSet(String value) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			return Collections.emptySet();
		}
		Set<String> result = new LinkedHashSet<>();
		for (String part : Arrays.asList(trimmed.split(","))) {
			String item = trimToNull(part);
			if (item != null) {
				result.add(item);
			}
		}
		return Collections.unmodifiableSet(result);
	}

	private static long parseLong(String value, long defaultValue) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			return defaultValue;
		}
		try {
			long parsed = Long.parseLong(trimmed);
			return parsed > 0 ? parsed : defaultValue;
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * <p>isEnabled.</p>
	 *
	 * @return true when the operator asked for the feature, regardless of whether the rest
	 *         of the configuration is complete
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * <p>isUsable.</p>
	 *
	 * @return true when the feature is enabled AND every mandatory value is present. The
	 *         endpoint serves nothing but a 404 while this is false.
	 */
	public boolean isUsable() {
		return enabled && missingKeys.isEmpty();
	}

	/**
	 * <p>Getter for the field <code>missingKeys</code>.</p>
	 *
	 * @return the mandatory keys the operator has not supplied, never null
	 */
	public List<String> getMissingKeys() {
		return missingKeys;
	}

	/**
	 * <p>Getter for the field <code>issuer</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getIssuer() {
		return issuer;
	}

	/**
	 * <p>Getter for the field <code>audiences</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<String> getAudiences() {
		return audiences;
	}

	/**
	 * <p>Getter for the field <code>authorizedParties</code>.</p>
	 *
	 * @return a {@link java.util.Set} object
	 */
	public Set<String> getAuthorizedParties() {
		return authorizedParties;
	}

	/**
	 * <p>Getter for the field <code>principalAttribute</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getPrincipalAttribute() {
		return principalAttribute;
	}

	/**
	 * <p>Getter for the field <code>tokenType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getTokenType() {
		return tokenType;
	}

	/**
	 * <p>Getter for the field <code>clockSkewSeconds</code>.</p>
	 *
	 * @return a long
	 */
	public long getClockSkewSeconds() {
		return clockSkewSeconds;
	}

	/**
	 * <p>Getter for the field <code>maxMintsPerMinute</code>.</p>
	 *
	 * @return a int
	 */
	public int getMaxMintsPerMinute() {
		return maxMintsPerMinute;
	}

	/**
	 * <p>Getter for the field <code>maxMintsPerMinutePerUser</code>.</p>
	 *
	 * @return a int
	 */
	public int getMaxMintsPerMinutePerUser() {
		return maxMintsPerMinutePerUser;
	}

	/**
	 * Constant time comparison of the presented <code>X-beCPG-Portal-Key</code> against the
	 * configured secret. Never short circuits on length, never logs either value.
	 *
	 * @param presented the header value, may be null
	 * @return true when it matches
	 */
	public boolean matchesSharedSecret(String presented) {
		if (sharedSecret == null || presented == null) {
			return false;
		}
		byte[] expected = sharedSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
		byte[] actual = presented.getBytes(java.nio.charset.StandardCharsets.UTF_8);
		return java.security.MessageDigest.isEqual(expected, actual);
	}

	/**
	 * Tells whether the DIRECT peer is allowed to mint sessions.
	 *
	 * Deliberately ignores <code>X-Forwarded-For</code>: that header is caller controlled and
	 * trusting it would turn the allow list into decoration. A deployment that puts a reverse
	 * proxy in front of Share must list the proxy's address here.
	 *
	 * An entry ending with '.' or ':' is a prefix match, so a whole subnet can be given as
	 * <code>172.20.</code> without dragging in a CIDR parser.
	 *
	 * @param remoteAddress the value of {@code HttpServletRequest.getRemoteAddr()}
	 * @return true when the address is allowed
	 */
	public boolean allowsRemoteAddress(String remoteAddress) {
		if (remoteAddress == null || allowedAddresses.isEmpty()) {
			return false;
		}
		for (String allowed : allowedAddresses) {
			if (allowed.endsWith(".") || allowed.endsWith(":")) {
				if (remoteAddress.startsWith(allowed)) {
					return true;
				}
			} else if (allowed.equals(remoteAddress)) {
				return true;
			}
		}
		return false;
	}

}
