package fr.becpg.web.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Test;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;

/**
 * <p>PortalSessionConfigTest class.</p>
 *
 * The whole point of this class is the OFF state: an unconfigured deployment must produce a
 * configuration that cannot be used, and every way of half-configuring the feature must also
 * produce one that cannot be used. "Enabled but incomplete" must never degrade into "enabled
 * with a check switched off".
 *
 * @author matthieu
 */
public class PortalSessionConfigTest {

	private static final String SECRET = "0123456789012345678901234567890123456789";

	private final List<String> setProperties = new java.util.ArrayList<>();

	@After
	public void clearProperties() {
		setProperties.forEach(System::clearProperty);
		setProperties.clear();
	}

	private void property(String key, String value) {
		String name = PortalSessionConfig.PROPERTY_PREFIX + key;
		System.setProperty(name, value);
		setProperties.add(name);
	}

	private Map<String, String> complete() {
		Map<String, String> values = new HashMap<>();
		values.put(PortalSessionConfig.KEY_ENABLED, "true");
		values.put(PortalSessionConfig.KEY_ISSUER, "https://auth.example.com/auth/realms/inst1");
		values.put(PortalSessionConfig.KEY_AUDIENCES, "inst1-openid");
		values.put(PortalSessionConfig.KEY_AUTHORIZED_PARTIES, "becpg-portal");
		values.put(PortalSessionConfig.KEY_SHARED_SECRET, SECRET);
		values.put(PortalSessionConfig.KEY_ALLOWED_ADDRESSES, "10.0.0.7");
		return values;
	}

	/** No configuration at all, which is what every existing install has. */
	@Test
	public void isOffWhenNothingIsConfigured() {
		PortalSessionConfig config = new PortalSessionConfig(new HashMap<>());

		assertFalse(config.isEnabled());
		assertFalse(config.isUsable());
	}

	@Test
	public void isOffWhenTheConfigServiceKnowsNothingAboutIt() {
		ConfigService configService = mock(ConfigService.class);
		when(configService.getConfig(anyString())).thenReturn(null);

		assertFalse(PortalSessionConfig.read(configService).isUsable());
	}

	@Test
	public void isOffWhenThereIsNoConfigServiceAtAll() {
		assertFalse(PortalSessionConfig.read(null).isUsable());
	}

	/** A broken config block must disable the feature, never take Share down with it. */
	@Test
	public void isOffWhenTheConfigServiceThrows() {
		ConfigService configService = mock(ConfigService.class);
		when(configService.getConfig(anyString())).thenThrow(new IllegalStateException("boom"));

		assertFalse(PortalSessionConfig.read(configService).isUsable());
	}

	@Test
	public void isOffWhenEnabledIsAnythingButTrue() {
		for (String value : List.of("false", "", "1", "yes", "TRUE-ish")) {
			Map<String, String> values = complete();
			values.put(PortalSessionConfig.KEY_ENABLED, value);

			assertFalse("'" + value + "' must not switch the feature on", new PortalSessionConfig(values).isUsable());
		}
	}

	@Test
	public void isUsableOnlyWhenEverythingMandatoryIsThere() {
		PortalSessionConfig config = new PortalSessionConfig(complete());

		assertTrue(config.isUsable());
		assertTrue(config.getMissingKeys().isEmpty());
	}

	/**
	 * Each mandatory value on its own. An absent audience list must not mean "accept any
	 * audience", an absent address list must not mean "accept any peer".
	 */
	@Test
	public void staysOffWhenAnyMandatoryValueIsMissing() {
		for (String key : List.of(PortalSessionConfig.KEY_ISSUER, PortalSessionConfig.KEY_AUDIENCES, PortalSessionConfig.KEY_AUTHORIZED_PARTIES,
				PortalSessionConfig.KEY_SHARED_SECRET, PortalSessionConfig.KEY_ALLOWED_ADDRESSES)) {
			Map<String, String> values = complete();
			values.remove(key);
			PortalSessionConfig config = new PortalSessionConfig(values);

			assertTrue("still enabled without " + key, config.isEnabled());
			assertFalse("must not be usable without " + key, config.isUsable());
			assertTrue("must name " + key + " as missing", config.getMissingKeys().contains(key));
		}
	}

	/** A short secret is guessable, so it counts as no secret at all. */
	@Test
	public void staysOffWhenTheSharedSecretIsTooShort() {
		Map<String, String> values = complete();
		values.put(PortalSessionConfig.KEY_SHARED_SECRET, "portal");

		PortalSessionConfig config = new PortalSessionConfig(values);

		assertFalse(config.isUsable());
		assertTrue(config.getMissingKeys().contains(PortalSessionConfig.KEY_SHARED_SECRET));
	}

	@Test
	public void systemPropertiesWinOverTheConfigBlock() {
		ConfigService configService = configServiceReturning(Map.of(PortalSessionConfig.KEY_ENABLED, "false", PortalSessionConfig.KEY_ISSUER,
				"https://from-the-war.example.com", PortalSessionConfig.KEY_AUDIENCES, "inst1-openid",
				PortalSessionConfig.KEY_AUTHORIZED_PARTIES, "becpg-portal", PortalSessionConfig.KEY_ALLOWED_ADDRESSES, "10.0.0.7"));

		property(PortalSessionConfig.KEY_ENABLED, "true");
		property(PortalSessionConfig.KEY_ISSUER, "https://from-the-jvm.example.com");
		property(PortalSessionConfig.KEY_SHARED_SECRET, SECRET);

		PortalSessionConfig config = PortalSessionConfig.read(configService);

		assertTrue(config.isUsable());
		assertEquals("https://from-the-jvm.example.com", config.getIssuer());
	}

	@Test
	public void readsTheConfigBlockWhenNoSystemPropertyIsSet() {
		ConfigService configService = configServiceReturning(Map.of(PortalSessionConfig.KEY_ENABLED, "true", PortalSessionConfig.KEY_ISSUER,
				"https://auth.example.com/auth/realms/inst1", PortalSessionConfig.KEY_AUDIENCES, "inst1-openid, account",
				PortalSessionConfig.KEY_AUTHORIZED_PARTIES, "becpg-portal", PortalSessionConfig.KEY_SHARED_SECRET, SECRET,
				PortalSessionConfig.KEY_ALLOWED_ADDRESSES, "10.0.0.7"));

		PortalSessionConfig config = PortalSessionConfig.read(configService);

		assertTrue(config.isUsable());
		assertEquals(java.util.Set.of("inst1-openid", "account"), config.getAudiences());
		assertEquals(PortalSessionConfig.DEFAULT_PRINCIPAL_ATTRIBUTE, config.getPrincipalAttribute());
		assertEquals(PortalSessionConfig.DEFAULT_TOKEN_TYPE, config.getTokenType());
	}

	@Test
	public void matchesTheSharedSecretExactly() {
		PortalSessionConfig config = new PortalSessionConfig(complete());

		assertTrue(config.matchesSharedSecret(SECRET));
		assertFalse(config.matchesSharedSecret(SECRET + "x"));
		assertFalse(config.matchesSharedSecret(SECRET.substring(0, SECRET.length() - 1)));
		assertFalse(config.matchesSharedSecret(""));
		assertFalse(config.matchesSharedSecret(null));
	}

	/** With no secret configured, nothing must match — least of all a null header. */
	@Test
	public void matchesNothingWhenNoSecretIsConfigured() {
		PortalSessionConfig config = new PortalSessionConfig(new HashMap<>());

		assertFalse(config.matchesSharedSecret(null));
		assertFalse(config.matchesSharedSecret(""));
		assertFalse(config.matchesSharedSecret("anything"));
	}

	@Test
	public void allowsOnlyTheListedAddresses() {
		Map<String, String> values = complete();
		values.put(PortalSessionConfig.KEY_ALLOWED_ADDRESSES, "10.0.0.7, 172.20., ::1");
		PortalSessionConfig config = new PortalSessionConfig(values);

		assertTrue(config.allowsRemoteAddress("10.0.0.7"));
		assertTrue("a trailing dot is a prefix", config.allowsRemoteAddress("172.20.0.4"));
		assertTrue(config.allowsRemoteAddress("::1"));
		assertFalse(config.allowsRemoteAddress("10.0.0.70"));
		assertFalse(config.allowsRemoteAddress("172.21.0.4"));
		assertFalse(config.allowsRemoteAddress("127.0.0.1"));
		assertFalse(config.allowsRemoteAddress(null));
	}

	@Test
	public void allowsNoAddressWhenTheListIsEmpty() {
		Map<String, String> values = complete();
		values.remove(PortalSessionConfig.KEY_ALLOWED_ADDRESSES);
		PortalSessionConfig config = new PortalSessionConfig(values);

		assertFalse(config.allowsRemoteAddress("10.0.0.7"));
		assertFalse(config.allowsRemoteAddress("127.0.0.1"));
	}

	@Test
	public void fallsBackToSaneDefaultsForTheOptionalValues() {
		PortalSessionConfig config = new PortalSessionConfig(complete());

		assertEquals(60L, config.getClockSkewSeconds());
		assertEquals(60, config.getMaxMintsPerMinute());
		assertEquals(10, config.getMaxMintsPerMinutePerUser());
	}

	@Test
	public void ignoresNonsensicalNumbers() {
		Map<String, String> values = complete();
		values.put(PortalSessionConfig.KEY_CLOCK_SKEW_SECONDS, "not-a-number");
		values.put(PortalSessionConfig.KEY_MAX_MINTS_PER_MINUTE, "-1");
		values.put(PortalSessionConfig.KEY_MAX_MINTS_PER_MINUTE_PER_USER, "0");

		PortalSessionConfig config = new PortalSessionConfig(values);

		assertEquals(60L, config.getClockSkewSeconds());
		assertEquals(60, config.getMaxMintsPerMinute());
		assertEquals(10, config.getMaxMintsPerMinutePerUser());
	}

	private ConfigService configServiceReturning(Map<String, String> elements) {
		Config config = mock(Config.class);
		for (Map.Entry<String, String> entry : elements.entrySet()) {
			ConfigElement element = mock(ConfigElement.class);
			when(element.getValue()).thenReturn(entry.getValue());
			when(config.getConfigElement(entry.getKey())).thenReturn(element);
		}
		ConfigService configService = mock(ConfigService.class);
		when(configService.getConfig(PortalSessionConfig.CONFIG_CONDITION)).thenReturn(config);
		return configService;
	}

}
