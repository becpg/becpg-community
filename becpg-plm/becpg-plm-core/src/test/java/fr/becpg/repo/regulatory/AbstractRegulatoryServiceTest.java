package fr.becpg.repo.regulatory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Optional;

import org.junit.Test;

/**
 * Unit tests of the masking of the credentials carried by the errors of the regulatory services.
 */
public class AbstractRegulatoryServiceTest {

	private static final String TOKEN = "fakeTokenForUnitTest0123456789";

	private static final String TRUNCATED_TOKEN = "fakeTokenForUnitTest";

	@Test
	public void configuredTokenIsMasked() {
		String masked = AbstractRegulatoryService.maskSecrets("Call failed with token " + TOKEN, Optional.of(TOKEN));

		assertFalse(masked.contains(TOKEN));
	}

	@Test
	public void truncatedHeaderValueIsMasked() {
		String error = "Unexpected decernis error: Illegal character(s) in message header value: Bearer " + TRUNCATED_TOKEN;

		String masked = AbstractRegulatoryService.maskSecrets(error, Optional.of(TOKEN));

		assertEquals("Unexpected decernis error: Illegal character(s) in message header value: ***", masked);
	}

	@Test
	public void headerValueSpanningSeveralLinesIsMasked() {
		String masked = AbstractRegulatoryService.maskSecrets("header value: Bearer abc\ndef", Optional.empty());

		assertFalse(masked.contains("def"));
	}

	@Test
	public void bearerValueIsMaskedWithoutConfiguredToken() {
		assertEquals("Authorization: Bearer *** refused",
				AbstractRegulatoryService.maskSecrets("Authorization: Bearer " + TRUNCATED_TOKEN + " refused", Optional.empty()));
	}

	@Test
	public void messageWithoutSecretIsUnchanged() {
		assertEquals("Connection refused", AbstractRegulatoryService.maskSecrets("Connection refused", Optional.of(TOKEN)));
	}

	@Test
	public void missingMessageStaysMissing() {
		assertNull(AbstractRegulatoryService.maskSecrets(null, Optional.of(TOKEN)));
	}
}
