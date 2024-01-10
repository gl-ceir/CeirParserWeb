/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.glocks.parser;

import com.glocks.dao.SysConfigurationDao;
import com.glocks.http.HttpURLConnectionExample;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author user
 */
public class ErrorFileGenrator {


    static Logger logger = LogManager.getLogger(ErrorFileGenrator.class);

    public void gotoErrorFile(Connection conn, String txn_id, String errorString) {
        try {
            logger.info("Error File init");
//            Statement stmt = conn.createStatement();
//            String qury = "select value from  sys_param  where tag  = 'system_error_filepath' ";
//            ResultSet resultmsdn = null;
//            resultmsdn = stmt.executeQuery(qury);
//            String errorPath = null;
//            try {
//                while (resultmsdn.next()) {
//                    errorPath = resultmsdn.getString(1);
//                }
//            } catch (Exception e) {
//                logger.error("Error...errorPath." + e);
//            }
//            logger.debug("fileString is " + errorString);
            String errorPath = new SysConfigurationDao().getTagValue(conn, "system_error_filepath");
            try {

                File file = new File(errorPath + txn_id);
                file.mkdir();
                String fileNameInput = errorPath + txn_id + "/" + txn_id + "_error.csv";       //
                String fileName = txn_id + "_error.csv";
                String filePath = errorPath + txn_id;
                logger.info("fileNameInput...." + fileNameInput);
                File fout = new File(fileNameInput);
                FileOutputStream fos = new FileOutputStream(fout, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                bw.write(errorString);
                bw.newLine();
                bw.close();

                new HttpURLConnectionExample().redudencyApiConnect(fileName, txn_id, errorPath + txn_id + "/");
            } catch (Exception e) {
                logger.error(e + "in ["+ Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) +"]");
            }

        } catch (Exception e) {
            logger.error(e + "in ["+ Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) +"]");
        }
    }

    public void gotoErrorFilewithList(String errorPath, String txn_id, ArrayList<String> fileLines) {
        try {
            File file = new File(errorPath + txn_id);
            file.mkdir();
            logger.info(" mkdir done ");
            String fileNameInput = errorPath + txn_id + "/" + txn_id + "_error.csv";
            logger.info(" fileNameInput Erorr file name  " + fileNameInput);
            File fout = new File(fileNameInput);
            FileOutputStream fos = new FileOutputStream(fout, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            for (String val : fileLines) {
                bw.write(val);
                bw.newLine();
            }
            bw.close();
            new HttpURLConnectionExample().redudencyApiConnect(txn_id + "_error.csv", txn_id, errorPath + txn_id + "/");
        } catch (Exception e) {
            logger.error(e + "in ["+ Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) +"]");
        }
    }

    public void writeErrorMessageInFile(String errorPath, String txn_id, String errorString) {
        try {
            File file = new File(errorPath + txn_id);
            file.mkdir();
            String fileNameInput = errorPath + txn_id + "/" + txn_id + "_error.csv";       //
            logger.info("fileNameInput...." + fileNameInput);
            logger.info("errorString...." + errorString);
            File fout = new File(fileNameInput);
            FileOutputStream fos = new FileOutputStream(fout, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(errorString);
            bw.newLine();
            bw.close();
            new HttpURLConnectionExample().redudencyApiConnect(txn_id + "_error.csv", txn_id, errorPath + txn_id + "/");
        } catch (Exception e) {
            logger.error(e + "in ["+ Arrays.stream(e.getStackTrace()).filter(ste -> ste.getClassName().equals(this.getClass().getName())).collect(Collectors.toList()).get(0) +"]");
        }

    }

    public void apiConnectionErrorFileReader() {
        try {
            ArrayList<String> coll = new ArrayList<String>();
            String currentDirectory = System.getProperty("user.dir");
            String fileName = currentDirectory + "/conf/apiConnectionTag.txt";
            File file = null;
            String line = null;
            String data[] = null;
            BufferedReader br = null;
            FileReader fr = null;
            file = new File(fileName);
            String tag = null;
            String responseBody = null;
            logger.info("fileNameInput @ apiConnectionErrorFileReader...." + fileName);
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            int a = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() > 0) {
                    if (a == 0) {
                        data = line.split("@", 2);
                        tag = data[0];
                        responseBody = data[1];
                    } else {
                        coll.add(line);
                    }
                    a++;
                }
            }
            br.close();
            fr.close();
            logger.info("--- > " + tag + " :: " + responseBody);
            FileWriter writer = new FileWriter(fileName);
            for (String lineS : coll) {
                writer.write(lineS);
                writer.write("\n");
            }
            writer.close();
            if (tag != null || tag.equals("")) {
                new HttpURLConnectionExample().HttpApiConnecter(tag, responseBody);
            }
        } catch (Exception e) {
            logger.warn("No File Found ");
//               logger.error(" " + e);
        }

    }

    public void apiConnectionErrorFileWriter(String tag, String responseBody) {
        try {
            String currentDirectory = System.getProperty("user.dir");
            String fileNameInput = currentDirectory + "/conf/apiConnectionTag.txt";
            logger.info("FilePath[" + fileNameInput + "]Tag ["+tag +"] ResponseBody ["  + responseBody+"]");
            File fout = new File(fileNameInput);
            FileOutputStream fos = new FileOutputStream(fout, true);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(tag + "@" + responseBody);
            bw.newLine();
            bw.close();
        } catch (Exception x) {
            logger.error(" :: " + x);

        }

    }

}
