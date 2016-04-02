/*
 *  Copyright (c) 2011-2015 The original author or authors
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *       The Eclipse Public License is available at
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *       The Apache License v2.0 is available at
 *       http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.ext.mail;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.junit.Test;

import io.vertx.ext.mail.impl.CertHostnameChecker;

/**
 * test a few certificate checks that are difficult to do when we are connecting to localhost only on the test server
 *
 * @author <a href="http://oss.lehmann.cx/">Alexander Lehmann</a>
 */
public class CertHostnameCheckerTest {

  @Test(expected=SSLPeerUnverifiedException.class)
  public void testCertNullException() throws Exception {
    new CertHostnameChecker(null, "localhost").validateHost();
  }

  @Test
  public void testWildcardMatch() throws Exception {
    X509Certificate[] certs = certList();

    new CertHostnameChecker(certs, "www.example.com").validateHost();
    new CertHostnameChecker(certs, "subdomain.example.com").validateHost();
  }

  @Test(expected=SSLPeerUnverifiedException.class)
  public void testPlainDomain() throws Exception {
    new CertHostnameChecker(certList(), "example.com").validateHost();
  }

  @Test(expected=SSLPeerUnverifiedException.class)
  public void testPlainHostname() throws Exception {
    new CertHostnameChecker(certList(), "hostname").validateHost();
  }

  /**
   * @return
   * @throws CertificateException
   * @throws IOException 
   */
  private X509Certificate[] certList() throws CertificateException, IOException {
    try (InputStream inputStream = new FileInputStream("src/test/resources/certs/server_wildcard.crt")) {
      X509Certificate cert = X509Certificate.getInstance(inputStream);
      X509Certificate[] certs = { cert };
      return certs;
    }
  }

  @Test(expected=SSLPeerUnverifiedException.class)
  public void testDiffDomain() throws Exception {
    new CertHostnameChecker(certList(), "www.example.org").validateHost();
  }

}
