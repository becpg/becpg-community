package fr.becpg.repo.security.authentication.openid.oauth;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthRsaSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthSigner;
import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;

/**
 * 
 * @author matthieu
 *
 */
public class OAuthTokenUtils {

	
	private static final  ThreadLocal<GoogleOAuthParameters> token = new ThreadLocal<GoogleOAuthParameters>();
	
	
	private static String  oauthCertFile =null;
	private static boolean is2LeggedOAuth = false;
	
	public static GoogleOAuthParameters getCurrentOAuthToken(){
		return token.get();
	}
	
	public static void setCurrentOAuthToken(GoogleOAuthParameters value){
		token.set(value);
	}

	public static PrivateKey getPrivateKey(String privKeyFileName) throws IOException, NoSuchAlgorithmException, Base64DecoderException, InvalidKeySpecException {
		  File privKeyFile = new File(privKeyFileName);
		  FileInputStream fis = new FileInputStream(privKeyFile);
		  DataInputStream dis  = new DataInputStream(fis);

		  byte[] privKeyBytes = new byte[(int) privKeyFile.length()];
		  dis.read(privKeyBytes);
		  dis.close();
		  fis.close();

		  String BEGIN = "-----BEGIN PRIVATE KEY-----";
		  String END = "-----END PRIVATE KEY-----";
		  String str = new String(privKeyBytes);
		  if (str.contains(BEGIN) && str.contains(END)) {
		    str = str.substring(BEGIN.length(), str.lastIndexOf(END));
		  }

		  KeyFactory fac = KeyFactory.getInstance("RSA");
		  EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(Base64.decode(str));
		  return fac.generatePrivate(privKeySpec);
  }

	public static OAuthSigner getRSASigner() throws NoSuchAlgorithmException, InvalidKeySpecException, OAuthException, IOException, Base64DecoderException {
		return  new OAuthRsaSha1Signer(getPrivateKey(oauthCertFile));
	}

	public static void initPrivateKey(String oauthCertFile) {
		OAuthTokenUtils.oauthCertFile = oauthCertFile;
	}
	
	public static void initIs2LeggedOauth(boolean is2LeggedOAuth) {
		OAuthTokenUtils.is2LeggedOAuth = is2LeggedOAuth;
	}

	public static boolean is2LeggedOAuth() {
		return is2LeggedOAuth;
	}
	
	
}
