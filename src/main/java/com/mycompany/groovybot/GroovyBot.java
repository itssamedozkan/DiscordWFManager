/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.mycompany.groovybot;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Samed OZKAN
 */
public class GroovyBot {
private class Person {
    public String name;

    public Person(String name) {
        this.name = name;
    }
}
    public static String getResponse(InputStream i) throws IOException {
        String res = "";
        InputStreamReader in = new InputStreamReader(i);
        BufferedReader br = new BufferedReader(in);
        String output;
        while ((output = br.readLine()) != null) {
            res += (output);
        }

        return res;
    }
    public static void main(String[] args) throws Exception {

   URL url = new URL("https://api.writeforme.org/api/v1/tasks");
HttpURLConnection http = (HttpURLConnection)url.openConnection();
http.setRequestMethod("POST");
http.setDoOutput(true);
http.setRequestProperty("accept", "application/json, text/plain, */*");
http.setRequestProperty("accept-language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7");
http.setRequestProperty("content-type", "application/json");
http.setRequestProperty("sec-ch-ua-mobile", "?0");
http.setRequestProperty("sec-fetch-dest", "empty");
http.setRequestProperty("sec-fetch-mode", "cors");
http.setRequestProperty("sec-fetch-site", "same-site");
http.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.67 Safari/537.36 OPR/87.0.4390.36");

String data = "{\"listType\":\"userTasks\",\"user\":\"5e95d9dc528f692fa7693899\",\"query\":\"\",\"limit\":10000,\"skip\":0,\"sortBy\":\"created\",\"sortAsc\":false}";

byte[] out = data.getBytes(StandardCharsets.UTF_8);

OutputStream stream = http.getOutputStream();
stream.write(out);

System.out.println(http.getResponseCode() + " " + http.getResponseMessage());

String response = getResponse(http.getInputStream());
        System.out.println(response.charAt(0));
JSONObject obj = new JSONObject(response);

JSONArray arr  = obj.getJSONArray("tasks");
        for (int i = 0; i < arr.length(); i++)
{
    JSONObject post_id = arr.getJSONObject(i);
    System.out.println(post_id);
}
http.disconnect();

    }
}
