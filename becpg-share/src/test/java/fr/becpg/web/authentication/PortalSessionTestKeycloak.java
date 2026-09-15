package fr.becpg.web.authentication;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * <p>PortalSessionTestKeycloak class.</p>
 *
 * A throwaway identity provider for the tests: it serves a discovery document and a JWKS on
 * localhost, and mints tokens shaped exactly like the ones Keycloak issues for this realm —
 * two audiences, the client id in <code>azp</code> and NOT in <code>aud</code>, a
 * <code>typ</code> claim of <code>Bearer</code>, and no <code>nbf</code> at all.
 *
 * Reproducing that shape matters: a verifier written against a guessed token shape passes its
 * own tests and rejects every real token.
 *
 * Uses only the JDK's HTTP server and the nimbus-jose-jwt already in the Share war, so it adds
 * no dependency.
 *
 * @author matthieu
 */
class PortalSessionTestKeycloak implements AutoCloseable {

	static final String CLIENT_ID = "becpg-portal";

	static final String AUDIENCE = "inst1-openid";

	static final String USERNAME = "supplier.local@becpg.fr";

	private final HttpServer server;

	private final RSAKey signingKey;

	/** A second, valid-looking key the issuer never publishes: used to forge a signature. */
	private final RSAKey foreignKey;

	private final String issuer;

	private final AtomicInteger jwksHits = new AtomicInteger();

	private volatile boolean available = true;

	PortalSessionTestKeycloak() throws Exception {
		this.signingKey = new RSAKeyGenerator(2048).keyID("test-key-1").generate();
		this.foreignKey = new RSAKeyGenerator(2048).keyID("test-key-1").generate();

		this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		this.issuer = "http://127.0.0.1:" + server.getAddress().getPort() + "/auth/realms/inst1";

		server.createContext("/auth/realms/inst1/.well-known/openid-configuration", exchange -> respond(exchange, "{\"issuer\":\"" + issuer
				+ "\",\"jwks_uri\":\"" + issuer + "/protocol/openid-connect/certs\"}"));
		server.createContext("/auth/realms/inst1/protocol/openid-connect/certs", exchange -> {
			jwksHits.incrementAndGet();
			respond(exchange, new JWKSet(signingKey.toPublicJWK()).toString());
		});
		server.start();
	}

	/** Simulates an identity provider that is up but broken, without moving the port. */
	void setAvailable(boolean available) {
		this.available = available;
	}

	private void respond(HttpExchange exchange, String body) throws IOException {
		if (!available) {
			exchange.sendResponseHeaders(500, -1);
			exchange.close();
			return;
		}
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, bytes.length);
		try (OutputStream out = exchange.getResponseBody()) {
			out.write(bytes);
		}
	}

	String issuer() {
		return issuer;
	}

	int jwksHits() {
		return jwksHits.get();
	}

	/** A token exactly like the real one measured on the local realm. */
	JWTClaimsSet.Builder validClaims() {
		Instant now = Instant.now();
		return new JWTClaimsSet.Builder().issuer(issuer).audience(List.of(AUDIENCE, "account")).claim("azp", CLIENT_ID).claim("typ", "Bearer")
				.claim("preferred_username", USERNAME).issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(300)));
		// Deliberately no notBeforeTime(): Keycloak emits none, and a verifier that requires
		// one rejects every real token.
	}

	String sign(JWTClaimsSet claims) throws Exception {
		return sign(claims, signingKey, JWSAlgorithm.RS256);
	}

	/** Signs with a key the issuer never published: a well formed, correctly signed forgery. */
	String signWithForeignKey(JWTClaimsSet claims) throws Exception {
		return sign(claims, foreignKey, JWSAlgorithm.RS256);
	}

	private String sign(JWTClaimsSet claims, RSAKey key, JWSAlgorithm algorithm) throws Exception {
		SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(algorithm).keyID(key.getKeyID()).type(JOSEObjectType.JWT).build(), claims);
		jwt.sign(new RSASSASigner(key));
		return jwt.serialize();
	}

	/** Flips one character of the signature, leaving the header and payload intact. */
	static String tamper(String token) {
		int lastDot = token.lastIndexOf('.');
		String signature = token.substring(lastDot + 1);
		char first = signature.charAt(0);
		char replacement = first == 'A' ? 'B' : 'A';
		return token.substring(0, lastDot + 1) + replacement + signature.substring(1);
	}

	@Override
	public void close() {
		server.stop(0);
	}

}
