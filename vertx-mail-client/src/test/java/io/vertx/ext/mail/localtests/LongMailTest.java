package io.vertx.ext.mail.localtests;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mail.LoginOption;
import io.vertx.ext.mail.MailAttachment;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.SMTPTestWiser;
import io.vertx.ext.mail.StartTLSOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * @author <a href="http://oss.lehmann.cx/">Alexander Lehmann</a>
 *
 */
@RunWith(VertxUnitRunner.class)
public class LongMailTest extends SMTPTestWiser {

  private static final Logger log = LoggerFactory.getLogger(LongMailTest.class);

  @Rule
  public Timeout rule = Timeout.seconds(600);

  //  @Ignore
  @Test
  public void mailTest(TestContext context) throws IOException {
    log.info("starting");

    MailConfig mailConfig = new MailConfig("mail.arcor.de", 587, StartTLSOptions.DISABLED, LoginOption.REQUIRED);
    //    mailConfig.setSsl(false);
    //    mailConfig.setTrustAll(true);
    //    ProxyOptions proxyOptions = new ProxyOptions();
    //    proxyOptions.setType(ProxyType.SOCKS5)
    //    .setHost("localhost").setPort(9150);
    //    mailConfig.setProxy(proxyOptions);
    mailConfig.setOwnHostname("arcor.de");

    Properties account = new Properties();

    InputStream input = null;

    try {
      input = new FileInputStream("account.properties");
      account.load(input);
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    mailConfig.setUsername(account.getProperty("username"));
    mailConfig.setPassword(account.getProperty("password"));

    log.debug("config:"+mailConfig.toJson().toString());

    Vertx vertx = Vertx.vertx();
    Async async = context.async();

    MailClient mailService = MailClient.createShared(vertx, mailConfig);

    MailMessage email = new MailMessage()
        .setFrom("lehmann333@arcor.de")
        .setTo("lehmann333_discard@arcor.de")
        .setSubject("Testmail")
        .setText("Mailtext");

    int size = 5000000;

    Buffer b = Buffer.buffer(size);

    for(int i=0;i<size;i++) {
      b.appendString("*");
    }

    MailAttachment attachment = new MailAttachment();
    attachment.setContentType("application/octet-stream");
    attachment.setName("attachment.file");
    attachment.setData(b);
    email.setAttachment(attachment);

    //    List<String> to = new ArrayList<>();
    //    for (int i=0;i<100;i++) {
    //      to.add("randomuser_"+i+"@lehmann.cx");
    //    }
    //
    //    email.setTo(to);

    sendMail(mailService, email, context, async);
  }

  /**
   * @param mailService
   * @param email
   */
  private void sendMail(MailClient mailService, MailMessage email, TestContext context, Async async) {
    mailService.sendMail(email, result -> {
      log.info("mail finished");
      if (result.succeeded()) {
        log.info(result.result().toString());
//        sendMail(mailService, email);
        async.complete();
      } else {
        log.warn("got exception", result.cause());
        context.fail("got exception "+result.cause());
      }
    });
  }
}
