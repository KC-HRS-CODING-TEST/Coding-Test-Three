package com.hrs;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import java.nio.file.Files;
import com.google.common.io.Resources;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class Test {
    static Logger log = LogManager.getFormatterLogger();

    public static void main(String[] args) throws InterruptedException {
        String readingsFile = args.length >= 1 ? args[0] : null;
        String rulesFiles = args.length >= 2 ? args[1] : "rules.json";

        try {
            new Test(rulesFiles, readingsFile);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Issue with file, please ensure proper path and formatting and try again");
        }

        Thread.sleep(5);
    }

    public Test(String rulesFile, String readingsFile) throws IOException, NoSuchFieldException, SecurityException {
        List<String> rulesList = mapJSONFile(rulesFile);
        String[] readingArray = mapReadingFile(readingsFile);

        JSONArray returnValues = buildReturnValues(rulesList, readingArray);
        sendResults(returnValues);
    }

    public List<String> mapJSONFile(String filePath) throws IOException {
        URL url = Resources.getResource(filePath);
        String text = Resources.toString(url, StandardCharsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> rulesMap = mapper.readValue(text, Map.class);

        Object[] rulesArray = rulesMap.values().toArray();
        String[] rulesStringArray = new String[rulesArray.length];

        for (int i = 0; i < rulesStringArray.length; i++) {
            rulesStringArray[i] = rulesArray[i].toString();
        }

        List<String> rulesList = new ArrayList<String>();

        for (String s : rulesStringArray) {
            s = s.replace("}", "").replace("{", "");

            if (s.indexOf("],") != -1) {
                String[] temp = s.split("],");

                for (String t : temp) {
                    t = t + "]";
                    rulesList.add(t);
                }

                continue;
            }

            rulesList.add(s);
        }

        return rulesList;
    }

    public String[] mapReadingFile(String readingsFile) throws IOException {
        InputStream readingsStream = Test.class.getResourceAsStream(readingsFile);
        byte[] readingBytes = ByteStreams.toByteArray(readingsStream);
        String readingString = new String(readingBytes);
        return readingString.trim().split("\\W+");
    }

    public JSONArray buildReturnValues(List<String> rulesList, String[] readingArray) {
        int id = 0;
        JSONArray returnValues = new JSONArray();

        for (int i = 0; i < readingArray.length; i++) {
            String s = readingArray[i];

            if (i == 0 || (readingArray[i - 1].matches(".*\\d.*") && readingArray[i].matches(".*\\d.*"))) {
                while (!readingArray[i].matches(".*\\d.*")) {
                    i++;
                }

                id = Integer.parseInt(readingArray[i]);
                continue;
            }

            if (s.matches(".*\\d.*") || s.equals(readingArray[i + 1])) {
                continue;
            }

            for (String rule : rulesList) {
                if (rule.contains(s)) {
                    int ruleValue = Integer.parseInt(rule.replaceAll("\\D+", ""));
                    int readingValue = Integer.parseInt(readingArray[i + 1]);
                    boolean addValue = false;

                    if (rule.contains(">=")) {
                        if (readingValue >= ruleValue) {
                            addValue = true;
                        }
                    } else if (rule.contains("<=")) {
                        if (readingValue <= ruleValue) {
                            addValue = true;
                        }
                    } else if (rule.contains("<")) {
                        if (readingValue < ruleValue) {
                            addValue = true;
                        }
                    } else if (rule.contains(">")) {
                        if (readingValue > ruleValue) {
                            addValue = true;
                        }
                    }

                    if (addValue) {
                        JSONObject tempObject = new JSONObject();
                        tempObject.put("id", id);
                        tempObject.put("measure", s);
                        tempObject.put("value", readingValue);
                        returnValues.put(tempObject);
                    }
                }
            }
        }

        return returnValues;
    }

    public void sendResults(JSONArray returnValues) throws IOException {
        String returnValuesString = returnValues.toString();
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpPost request = new HttpPost("http://www.hrs.com/readings");
            StringEntity valuesParams = new StringEntity(returnValuesString);
            request.addHeader("content-type", "application/x-www-form-urlencoded");
            request.setEntity(valuesParams);
            System.out.println("Data Sync Success");
            // CloseableHttpResponse response = httpClient.execute(request);
        } catch(Exception e) {
            e.printStackTrace();
            System.out.println("Server error, please wait and try again");
        }

        System.out.println(returnValuesString);

        FileWriter fileWriter = new FileWriter("/code/results.json");
        fileWriter.write(returnValuesString);
        fileWriter.close();

        Path path = Paths.get("/code/results.json").toAbsolutePath();
        String pathToFile = path.normalize().toString();
        byte[] encoded = Files.readAllBytes(Paths.get(pathToFile));
        String savedFileText = new String(encoded, StandardCharsets.US_ASCII);
        System.out.println(savedFileText);
    }
}
