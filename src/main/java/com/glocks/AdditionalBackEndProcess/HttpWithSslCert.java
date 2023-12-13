/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.glocks.AdditionalBackEndProcess;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class HttpWithSslCert {

    String urls = "https://ticket.goldilocks-tech.com/httpUtility/download";

    SSLContextBuilder builder = new SSLContextBuilder();

    static public String certificateString
            = "-----BEGIN CERTIFICATE-----\n"
            + "MIIGQTCCBSmgAwIBAgIHBcg1dAivUzANBgkqhkiG9w0BAQsFADCBjDELMAkGA1UE"
            + "BhMCSUwxFjAUBgNVBAoTDVN0YXJ0Q29tIEx0ZC4xKzApBgNVBAsTIlNlY3VyZSBE"
            + "5126sfeEJMRV4Fl2E5W1gDHoOd6V==\n"
            + "-----END CERTIFICATE-----";

    public static void main(String[] args) {
        try {
            new HttpWithSslCert().getRestTemplate();
        } catch (Exception e) {
            System.out.println("Error " + e);

        }

    }

    public RestTemplate getRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (x509Certificates, s) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        System.out.println("httpClient::: " + httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        System.out.println("restTemplate:: " + restTemplate);
        ResponseEntity<String> response = restTemplate.getForEntity(urls, String.class);

        System.out.println("response::: " + response);
        return restTemplate;
    }

    public static void main2342(String[] args) {
        try {

            ByteArrayInputStream derInputStream = new ByteArrayInputStream(HttpWithSslCert.certificateString.getBytes());
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(derInputStream);
            String alias = "alias";//cert.getSubjectX500Principal().getName();

            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null);
            trustStore.setCertificateEntry(alias, cert);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(trustStore, null);
            KeyManager[] keyManagers = kmf.getKeyManagers();

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(trustStore);
            TrustManager[] trustManagers = tmf.getTrustManagers();

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
            URL url = new URL("https://ticket.goldilocks-tech.com/httpUtility/download");
            var conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sslContext.getSocketFactory());

            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            System.out.println("RESPONSE ::::::::" + conn);
            OutputStream out = conn.getOutputStream();

            System.out.println(out);

        } catch (Exception e) {
            System.out.println("Error " + e);

        }
    }

    public static void main123(String[] args) {

        try {
            URL url = new URL("https://ticket.goldilocks-tech.com/httpUtility/download");
            invalidateSslSessions(url.getHost());
            HttpURLConnection http = null;
            if (url.getProtocol().toLowerCase().equals("https")) {
                // trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                http = https;
            } else {
                http = (HttpURLConnection) url.openConnection();
            }
            http.setRequestMethod("GET");
            http.setDoOutput(true);
            http.setDoInput(true);

            System.out.println("RESPONSE ::::::::" + http);
            OutputStream out = http.getOutputStream();

            System.out.println(out);

//            var bw = new BufferedWriter(new OutputStreamWriter(out));
//            int responseCode = http.getResponseCode();
//            StringBuffer response = new StringBuffer();
//            if (responseCode == HttpURLConnection.HTTP_OK) { // success
//                BufferedReader in = new BufferedReader(new InputStreamReader(
//                        http.getInputStream()));
//                String inputLine;
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//                System.out.println(response.toString());
//            } else {
//                System.out.println("GET request not working Getting " + responseCode);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void invalidateSslSessions(String forHost) {
        try {
            SSLSessionContext sc = SSLContext.getDefault().getClientSessionContext();
            while (sc.getIds().hasMoreElements()) {
                byte[] sessionId = sc.getIds().nextElement();
                SSLSession session = sc.getSession(sessionId);
                if (session.getPeerHost().equals(forHost)) {
                    session.invalidate();
                }
            }
        } catch (Exception e) {
            System.out.println("Could not validate SSL session for " + forHost);
            e.printStackTrace();
        }
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static void trustAllHosts() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509ExtendedTrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] xcs, String string, SSLEngine socket) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] xcs, String string, SSLEngine socket) throws CertificateException {

                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] xcs, String string, Socket socket) throws CertificateException {
                    }

                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            System.out.println("Error occurred" + e);
        }
    }

    public void sslFunction(String[] args) {
        try {
            builder.loadTrustMaterial(null, new TrustStrategy() {
                public boolean isTrusted(final X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            });
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());

            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        } catch (Exception e) {
        }
    }

//    private static HostnameVerifier getHostnameVerifier() {
//        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
//            @Override
//            public boolean verify(String hostname, SSLSession session) {
//                HostnameVerifier hv   = HttpsURLConnection.getDefaultHostnameVerifier();
//                return hv.verify("com.example.com", session);
//            }
//        };
//        return hostnameVerifier;
//    }
}
