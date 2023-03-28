package com.leap.authentication.usermgmntservice;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPublicKey;

public class ValidateRSASignature {

	public static void main(String[] args) throws Exception {
		RSAPublicKey publicKey = null;
		InputStream file = ClassLoader.getSystemResourceAsStream("wso2carbon.jks");
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		keystore.load(file, "wso2carbon".toCharArray());

		String alias = "wso2carbon";

		// Get certificate of public key
		Certificate cert = keystore.getCertificate(alias);

		// Get public key
		publicKey = (RSAPublicKey) cert.getPublicKey();
		System.out.println("Format Key :: " + publicKey.getFormat());
		System.out.println("Algoritham Key :: " + publicKey.getAlgorithm());
		System.out.println("public key value :: " + publicKey.toString());

		// // Enter JWT String here
		// String signedJWTAsString =
		// "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhbGljZSIsImlzcyI6Imh0d";
		//
		// SignedJWT signedJWT = SignedJWT.parse(signedJWTAsString);
		//
		// JWSVerifier verifier = new RSASSAVerifier(publicKey);
		//
		// if (signedJWT.verify(verifier)) {
		// System.out.println("Signature is Valid");
		// } else {
		// System.out.println("Signature is NOT Valid");
		// }
	}

}
