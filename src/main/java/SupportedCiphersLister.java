import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SupportedCiphersLister {

    public static void main(String[] args) {

        final Logger LOG = LoggerFactory.getLogger(SupportedCiphersLister.class);

        if (LOG.isTraceEnabled()) {
            LOG.trace("Test: TRACE level message.");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Test: DEBUG level message.");
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("Test: INFO level message.");
        }
        if (LOG.isWarnEnabled()) {
            LOG.warn("Test: WARN level message.");
        }
        if (LOG.isErrorEnabled()) {
            LOG.error("Test: ERROR level message.");
        }

        SSLContext context = null;
        try {
            context = SSLContext.getDefault();
            System.out.println(Cipher.getMaxAllowedKeyLength("AES"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        SSLSocketFactory sf = context.getSocketFactory();
        String[] cipherSuites = sf.getSupportedCipherSuites();
        System.out.println("supported ciphers.....");
        System.out.println(Arrays.toString(cipherSuites));
        System.out.println("default ciphers.....");
        System.out.println(Arrays.toString(sf.getDefaultCipherSuites()));
    }
}

