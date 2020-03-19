package com.xy.cert;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathBuilderException;
import java.security.cert.CertPathBuilderResult;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.LDAPCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.X509CertSelector;
import java.util.Collection;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import com.xiuye.util.log.LogUtil;

public class CertDev2 {

	public static void main(String[] args) throws NoSuchAlgorithmException, KeyStoreException, IOException,
			CertPathBuilderException, InvalidAlgorithmParameterException, CertificateException, CertStoreException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		LogUtil.log(new String(md.digest()));

		// create CertPathBuilder that implements the "PKIX" algorithm
		CertPathBuilder cpb = null;
		cpb = CertPathBuilder.getInstance("PKIX");
		// build certification path using specified parameters ("params")
		KeyStore ks = KeyStore.getInstance("pkcs12");
		ks.load(null, "123456".toCharArray());
		LogUtil.log(ks);

		CollectionCertStoreParameters ccsp = new CollectionCertStoreParameters();
		CertStore cs = CertStore.getInstance("Collection", ccsp);
		X509CertSelector xcs = new X509CertSelector();

		// select only unexpired certificates
		xcs.setCertificateValid(new Date());

		// select only certificates issued to
		// 'CN=alice, O=xyz, C=us'
		xcs.setSubject(new X500Principal("CN=alice, O=xyz, C=us"));

		// select only end-entity certificates
		xcs.setBasicConstraints(-2);

		// select only certificates with a digitalSignature
		// keyUsage bit set (set the first entry in the
		// boolean array to true)
		boolean[] keyUsage = { true };
		xcs.setKeyUsage(keyUsage);

		// select only certificates with a subjectAltName of
		// 'alice@xyz.example.com' (1 is the integer value of
		// an RFC822Name)
		xcs.addSubjectAlternativeName(1, "alice@xyz.example.com");

		LogUtil.log(xcs);

		Collection<? extends Certificate> certs = cs.getCertificates(xcs);
		LogUtil.log(certs);
		ks.setCertificateEntry("基明", certs.iterator().next());
		
		PKIXBuilderParameters params = new PKIXBuilderParameters(ks, xcs);
		CertPathBuilderResult cpbResult = cpb.build(params);
		CertPath cp = cpbResult.getCertPath();
		System.out.println("build passed, path contents: " + cp);
	}

}
