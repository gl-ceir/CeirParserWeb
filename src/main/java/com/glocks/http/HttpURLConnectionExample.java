package com.glocks.http;

import com.glocks.parser.ErrorFileGenrator;
import static com.glocks.parser.MainController.ip;
import static com.glocks.parser.MainController.serverName;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpURLConnectionExample {

    static Logger logger = LogManager.getLogger(HttpURLConnectionExample.class);
    private static final String POST_PARAMS = "";

    public static String sendGET(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");

        int responseCode = con.getResponseCode();
        StringBuffer response = new StringBuffer();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            logger.debug(response.toString());
        } else {
            logger.warn("GET request not working Getting "+ responseCode);
        }
        return response.toString();
    }

    public static String sendPOST(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(POST_PARAMS.getBytes());
        os.flush();
        os.close();
        // For POST only - END

        StringBuffer response = new StringBuffer();
        int responseCode = con.getResponseCode();
        logger.debug("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            logger.debug(response.toString());

        } else {
            logger.warn("POST request not worked");
        }

        return response.toString();
    }

    public static String sendPOST(String url, String body) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");

        con.setRequestMethod("POST");

        // For POST only - START
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        byte[] input = body.getBytes("utf-8");
        os.write(input, 0, input.length);
        os.flush();
        os.close();
        // For POST only - END

        StringBuffer response = new StringBuffer();
        int responseCode = con.getResponseCode();
        logger.debug("POST Response Code :: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) { //success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            logger.debug(response.toString());

        } else {
            logger.debug("POST request not worked");
        }

        return response.toString();
    }

    public void redudencyApiConnect(String fileName, String txn_id, String fileNameInput) {

        String tag = "http://$LOCAL_IP:9502/CEIR/uploadedFile/save";
        logger.info("  uploadedFile tag  " + tag);

        tag = tag.replace("$LOCAL_IP", ip);
        String serverId = serverName.contains("1") ? "1" : "2";

        String responseBody = "{\n"
                + "\"fileName\": \"" + fileName + "\",\n"
                + "\"txnId\": \"" + txn_id + "\",\n"
                + "\"filePath\": \"" + fileNameInput + "\",\n"
                + "\"serverId\": " + serverId + " \n"
                + "}";
        logger.info("  after Replace  " + tag);
        logger.info("responseBody after Replace  " + responseBody);

        HttpApiConnecter(tag, responseBody);

    }

    public void HttpApiConnecter(String tag, String responseBody) {
        try {
            URL url = new URL(tag);
            HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
            hurl.setRequestMethod("POST");
            hurl.setDoOutput(true);
            hurl.setRequestProperty("Content-Type", "application/json");
            hurl.setRequestProperty("Accept", "application/json");
            OutputStreamWriter osw = new OutputStreamWriter(hurl.getOutputStream());
            osw.write(responseBody);
            osw.flush();
            osw.close();
            logger.info("Request Send");
            hurl.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(hurl.getInputStream()));
            String temp = null;
            StringBuilder sb = new StringBuilder();
            while ((temp = in.readLine()) != null) {
                sb.append(temp).append(" ");
            }
            String result = sb.toString();
            in.close();
            logger.info("OUTPUT result is .." + result);
        } catch (Exception e) {
            logger.error(responseBody + "  " + e);
            new ErrorFileGenrator().apiConnectionErrorFileWriter(tag, responseBody);
            logger.error(e.getLocalizedMessage() + "" + e);
        }
    }

//    public HttpURLConnection getHttpConnection(String url, String type) {
//        URL uri = null;
//        HttpURLConnection con = null;
//        try {
//            uri = new URL(url);
//            con = (HttpURLConnection) uri.openConnection();
//            con.setRequestMethod(type); // type: POST, PUT, DELETE, GET
//            con.setDoOutput(true);
//            con.setDoInput(true);
//            con.setConnectTimeout(60000); // 60 secs...
//            con.setReadTimeout(60000); // 60 secs
//            con.setRequestProperty("Accept-Encoding", "application/json");
//            con.setRequestProperty("Content-Type", "application/json");
//        } catch (Exception e) {
//            logger.error(e.getLocalizedMessage() + "" + e);
//        }
//        return con;
//    }
    
}
