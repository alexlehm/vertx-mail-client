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

import javax.net.ssl.SSLHandshakeException;
import javax.security.cert.CertificateException;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * this tests uses SSL on a local server
 * 
 * @author <a href="http://oss.lehmann.cx/">Alexander Lehmann</a>
 */
@RunWith(VertxUnitRunner.class)
public class MailSslTest extends SMTPTestDummy {

  @Test
  public void mailTestSSLCorrectCert(TestContext testContext) {
    this.testContext = testContext;
    startServer("src/test/resources/certs/server.jks");
    final MailConfig config = new MailConfig("localhost", 1465, StartTLSOptions.DISABLED, LoginOption.DISABLED)
        .setSsl(true).setKeyStore("src/test/resources/certs/client.jks").setKeyStorePassword("password");
    MailClient mailClient = MailClient.createNonShared(vertx, config);
    testSuccess(mailClient);
  }

  @Test
  public void mailTestSSLValidCertWrongHost(TestContext testContext) {
    this.testContext = testContext;
    startServer("src/test/resources/certs/server2.jks");
    final MailConfig config = new MailConfig("127.0.0.1", 1465, StartTLSOptions.DISABLED, LoginOption.DISABLED)
        .setSsl(true).setKeyStore("src/test/resources/certs/client.jks").setKeyStorePassword("password");
    MailClient mailClient = MailClient.createNonShared(vertx, config);
    testException(mailClient);
  }

  @Test
  public void mailTestSSLValidCertIpv6(TestContext testContext) {
    this.testContext = testContext;
    startServer("src/test/resources/certs/server.jks");
    final MailConfig config = new MailConfig("::1", 1465, StartTLSOptions.DISABLED, LoginOption.DISABLED)
        .setSsl(true).setKeyStore("src/test/resources/certs/client.jks").setKeyStorePassword("password");
    MailClient mailClient = MailClient.createNonShared(vertx, config);
    testSuccess(mailClient);
  }

  @Test
  public void mailTestSSLValidCertIpv6_2(TestContext testContext) {
    this.testContext = testContext;
    startServer("src/test/resources/certs/server.jks");
    final MailConfig config = new MailConfig("[::1]", 1465, StartTLSOptions.DISABLED, LoginOption.DISABLED)
        .setSsl(true).setKeyStore("src/test/resources/certs/client.jks").setKeyStorePassword("password");
    MailClient mailClient = MailClient.createNonShared(vertx, config);
    testSuccess(mailClient);
  }

  @Test
  public void mailTestSSLValidCertIpv6_3(TestContext testContext) {
    this.testContext = testContext;
    startServer("src/test/resources/certs/server.jks");
    final MailConfig config = new MailConfig("[0000:0000:0000:0000:0000:0000:0000:0001]", 1465, StartTLSOptions.DISABLED, LoginOption.DISABLED)
        .setSsl(true).setKeyStore("src/test/resources/certs/client.jks").setKeyStorePassword("password");
    MailClient mailClient = MailClient.createNonShared(vertx, config);
    testSuccess(mailClient);
  }

  @Test
  public void mailTestSSLTrustAll(TestContext testContext) {
    this.testContext = testContext;
    startServer("src/test/resources/certs/server.jks");
    final MailConfig config = new MailConfig("localhost", 1465, StartTLSOptions.DISABLED, LoginOption.DISABLED)
        .setSsl(true).setTrustAll(true);
    MailClient mailClient = MailClient.createNonShared(vertx, config);
    testSuccess(mailClient);
  }

  @Test
  public void mailTestSSLNoTrust(TestContext testContext) {
    this.testContext = testContext;
    startServer("src/test/resources/certs/server.jks");
    final MailConfig config = new MailConfig("localhost", 1465, StartTLSOptions.DISABLED, LoginOption.DISABLED)
        .setSsl(true);
    MailClient mailClient = MailClient.createNonShared(vertx, config);
    testException(mailClient, SSLHandshakeException.class);
  }

  @Test
  public void mailTestSSLCertCN(TestContext testContext) {
    this.testContext = testContext;
    startServer("src/test/resources/certs/server3.jks");
    final MailConfig config = new MailConfig("localhost", 1465, StartTLSOptions.DISABLED, LoginOption.DISABLED)
        .setSsl(true).setKeyStore("src/test/resources/certs/client.jks").setKeyStorePassword("password");
    MailClient mailClient = MailClient.createNonShared(vertx, config);
    testSuccess(mailClient);
  }

  @Test
  public void mailTestSSLCertCNWrongHost(TestContext testContext) {
    this.testContext = testContext;
    startServer("src/test/resources/certs/server3.jks");
    final MailConfig config = new MailConfig("127.0.0.1", 1465, StartTLSOptions.DISABLED, LoginOption.DISABLED)
        .setSsl(true).setKeyStore("src/test/resources/certs/client.jks").setKeyStorePassword("password");
    MailClient mailClient = MailClient.createNonShared(vertx, config);
    testException(mailClient, CertificateException.class);
  }

  @Override
  protected void startSMTP() {
    // start server later since the tests use different keystores
  }

  private void startServer(String keystore) {
    smtpServer = new TestSmtpServer(vertx, true, keystore);
  }

}
