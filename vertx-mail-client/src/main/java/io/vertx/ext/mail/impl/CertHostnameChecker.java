/**
 * 
 */
package io.vertx.ext.mail.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateFactory;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * @author <a href="http://oss.lehmann.cx/">Alexander Lehmann</a>
 *
 */
public class CertHostnameChecker {

  /**
   * 
   */
  private static final Pattern IPV4 = Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");
  private static final Pattern IPV6 = Pattern.compile("^\\[?([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}\\]?$");
  private static final Logger log = LoggerFactory.getLogger(CertHostnameChecker.class);

  private final X509Certificate[] certs;
  private final String hostname;

  public CertHostnameChecker(X509Certificate[] certs, String hostname) {
    this.certs = certs;
    this.hostname = hostname;
  }

  /**
   * @throws SSLPeerUnverifiedException 
   * @throws UnsupportedEncodingException 
   * @throws CertificateException 
   * @throws java.security.cert.CertificateException 
   * @throws UnknownHostException 
   * 
   */
  public void validateHost() throws SSLPeerUnverifiedException, UnsupportedEncodingException,
  CertificateException, java.security.cert.CertificateException, UnknownHostException {
    log.debug("checking certificate for "+hostname);
    if (certs == null) {
      throw new SSLPeerUnverifiedException("no certificate chain available");
    } else {
      boolean matched = false;
      X509Certificate cert = certs[0];
      byte encCert[] = cert.getEncoded();
      java.security.cert.X509Certificate cert2 = instantiate(encCert);
      Collection<List<?>> sa = cert2.getSubjectAlternativeNames();
      boolean hostnameIsIp = isIpAddress(hostname);
      if(sa != null) {
        for (List<?> s : sa) {
          int type = (Integer) s.get(0);
          if(type == 2) {
            String name = (String) s.get(1);
            if(hostnameIsIp) {
              if(isIpAddress(name)) {
                matched = matchIp(hostname, name);
              }
            } else {
              matched = matchHostname(hostname, name);
            }
            if(matched) {
              log.debug(hostname + " matches "+name);
              break;
            }
          }
          else if(type == 7) {
            String ip = (String) s.get(1);
            matched = matchIp(hostname, ip);
            if(matched) {
              log.debug(hostname + " matches "+ip);
              break;
            }
          }
        }
      } else {
        // check CN only if no altsubject entries are present
        String subject = cert.getSubjectDN().getName();
        int index = subject.indexOf("CN=");
        if(index>=0) {
          String cn =subject.substring(index+3);
          int index2 = cn.indexOf(',');
          if(index2 >=0) {
            cn = cn.substring(0,index2);
          }
          if(hostnameIsIp) {
            matched = matchIp(hostname, cn);
          } else {
            if(!isIpAddress(cn)) {
              matched = matchHostname(hostname, cn);
            }
          }
        }
        if(!matched) {
          throw new SSLPeerUnverifiedException("hostname doesn't match");
        }
      }
    }
  }

  /**
   * @param hostname2
   * @return
   */
  private boolean isIpAddress(String hostname) {
    return IPV4.matcher(hostname).matches() ||
            IPV6.matcher(hostname).matches();
  }

  /**
   * @param ip
   * @return
   * @throws UnknownHostException
   */
  private boolean matchIp(String hostname, String ip) throws UnknownHostException {
    InetAddress ipAddr = InetAddress.getByName(ip);
    InetAddress ipAddr2 = InetAddress.getByName(hostname);
    log.debug("compare("+hostname+","+ip+")");
    log.debug("compare("+ipAddr.getHostAddress()+","+ipAddr2.getHostAddress()+")");
    return ipAddr.equals(ipAddr2);
  }

  /**
   * @param hostname
   * @param name
   * @return
   */
  private boolean matchHostname(String hostname, String name) {
    log.debug("compare("+hostname+","+name+")");
    if(name.startsWith("*.")) {
      if(hostname.contains(".")) {
        return hostname.substring(hostname.indexOf('.')+1).equals(name.substring(2));
      } else {
        return false;
      }
    } else {
      return hostname.equals(name);
    }
  }

  private java.security.cert.X509Certificate instantiate(byte encCert[]) throws java.security.cert.CertificateException {
    InputStream inStream = null;
    inStream = new ByteArrayInputStream(encCert);
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    java.security.cert.X509Certificate cert = (java.security.cert.X509Certificate)cf.generateCertificate(inStream);
    return cert;
  }

}
