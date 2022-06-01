/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.wfmanager;

import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Samed OZKAN
 */
public class WFManager extends ListenerAdapter {

    static HashMap<String, String> Masters = new HashMap();


    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        MessageChannel  chl = event.getChannel();
        event.getAuthor();
        StringBuilder MastersBuilder = new StringBuilder();

        if (msg.getContentRaw().contains("!masterlist")) {
            if (!Masters.isEmpty()) {
                for (Map.Entry<String, String> entry : Masters.entrySet()) {
                    String key = entry.getKey();
                    String val = entry.getValue();

                    MastersBuilder.append(key.toString());
                    MastersBuilder.append("\n");
                }
                MastersBuilder.append("are my current Masters!");
            } else {
                MastersBuilder.append("I have no Masters at the moment!");
            }
            msg.reply("\"Master,  please wait while i am preparing your results!\"").queue(response /* => Message */ -> {
                response.editMessageFormat(MastersBuilder.toString()).queue();
            });
        } else if (msg.getContentRaw().contains("!newMaster ")) {
            FileWriter file = null;
            try {
                String command = msg.getContentRaw().replace("!newMaster ", "");
                String[] split = command.split(" ");
                Masters.put(split[0].trim(), split[1].trim());
                file = new FileWriter("MastersList.txt");
                // Creates a BufferedWriter
                BufferedWriter output = new BufferedWriter(file);
                // Writes the string to the file
                output.write(split[0].trim()  + "\t" + split[1].trim() + "\n");
                // Closes the writer
                output.close();
                
                msg.reply("\""+ split[0].trim() +" is my new Master from now on !\"").queue();
            } catch (IOException ex) {
                Logger.getLogger(WFManager.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    file.close();
                } catch (IOException ex) {
                    Logger.getLogger(WFManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }else if (msg.getContentRaw().contains("!taskinfo")){
            String command = msg.getContentRaw().replace("!taskinfo ", "");
            
            try {
                JSONObject mastersdata = getMastersTasks(command.trim());
                JSONArray masterstasks = mastersdata.getJSONArray("tasks");
                msg.reply("\"Pease wait while i am preparing your results!\"").queue(response /* => Message */ -> {
                response.editMessageFormat(" Master "+ command.trim() +" has "+ masterstasks.length() +" accessible tasks created in total !").queue();
            });
            } catch (IOException ex) {
                Logger.getLogger(WFManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }else if (msg.getContentRaw().contains("!slaveinfo")){

            String command = msg.getContentRaw().replace("!slaveinfo ", "");
            String[] splitted = command.split(" ");
            String Master = splitted[0].trim();
            String Slave = splitted[1].trim();
            msg.reply("\"Master,  please wait while i am preparing your results!\"").queue(response /* => Message */ -> {
                response.editMessageFormat(getUserInfo(Master,Slave)).queue();
            });
            
        }
    }
    
    private static String getUserInfo(String MasterName , String SlaveName){
         long ttwasted = 0 ;
        try {
            JSONObject masterdata = getMastersTasks(MasterName);
            JSONArray masterstasks = masterdata.getJSONArray("tasks");
            for (int i = 0; i < masterstasks.length(); i++) {
                JSONObject task = (JSONObject) masterstasks.get(i);
                String taskid = (String) task.get("_id");
                JSONArray taskdetails = (JSONArray) (getTaskDetails(taskid).getJSONArray("solutions")) ;
                System.out.println("Task name :" + (String) task.get("name"));
                for (int j = 0; j < taskdetails.length(); j++) {
                    System.out.println((JSONObject)taskdetails.get(j));
                    try {
                        
                 
                    String Realuser_id =  ((JSONObject)((JSONObject)taskdetails.get(j)).get("realUser")).getString("_id");
                    String realuser_name = ((JSONObject)((JSONObject)taskdetails.get(j)).get("realUser")).getString("name");
                    int totaltime = ((JSONObject)taskdetails.get(j)).getInt("totalTime");
                    System.out.println(Realuser_id  + " _ " + totaltime);
                    if (Realuser_id.equals(SlaveName) || realuser_name.equals(SlaveName)){
                        ttwasted += totaltime;
                    }
                       } catch (Exception e) {
                    }
                    
                }
            }
            int seconds = (int) (ttwasted / 1000) % 60 ;
            int minutes = (int) ((ttwasted / (1000*60)) % 60);
            int hours   = (int) ((ttwasted / (1000*60*60)) % 24);
            return "Slave : " + SlaveName + " has wasted a total time of " + hours + " hours "+ minutes+" minutes " +seconds+" seconds for you Master " +MasterName;
        } catch (IOException ex) {
            Logger.getLogger(WFManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
}

    private void getUserInfo(String Master){
        
        try {
            URL url = new URL("https://api.writeforme.org/api/v1/userdetail");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("authority", "api.writeforme.org");
            http.setRequestProperty("accept", "application/json, text/plain, */*");
            http.setRequestProperty("accept-language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7");
            http.setRequestProperty("content-type", "application/json");
            http.setRequestProperty("origin", "https://writeforme.org");
            http.setRequestProperty("referer", "https://writeforme.org/");
            http.setRequestProperty("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Opera\";v=\"87\"");
            http.setRequestProperty("sec-ch-ua-mobile", "?0");
            http.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
            http.setRequestProperty("sec-fetch-dest", "empty");
            http.setRequestProperty("sec-fetch-mode", "cors");
            http.setRequestProperty("sec-fetch-site", "same-site");
            http.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.67 Safari/537.36 OPR/87.0.4390.36");

            String data = "{\"userID\":\"5e95d9dc528f692fa7693899\",\"fullscreenPhoto\":true}";

            byte[] out = data.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);

            System.out.println(http.getResponseCode() + " " + http.getResponseMessage());

            String response = getResponse(http.getInputStream());
            JSONObject obj = new JSONObject(response);
            System.out.println(obj);
        }catch(Exception e){
        }
    }
    
    private static JSONObject getMastersTasks(String MasterName) throws IOException{

        try {
            URL url = new URL("https://api.writeforme.org/api/v1/tasks");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("authority", "api.writeforme.org");
            http.setRequestProperty("accept", "application/json, text/plain, */*");
            http.setRequestProperty("accept-language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7");
            http.setRequestProperty("authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjYyNWI0MzUyNWJmOTJlMDAzNzVjOWY3ZCIsInVzZXJuYW1lIjoiRm9vdFNsYXZlTWVkbyIsImFkbWluIjpmYWxzZSwidmVyc2lvbiI6MSwiYmxvY2tlZFVzZXJzIjpbIjYyODMwNjYyNWJmOTJlMDAzNzZiNTQzOCIsIjYyMWZlODQ0ZWViYzQwNzYxN2Y2Y2M2NSJdLCJpYXQiOjE2NTQwODcxMTAsImV4cCI6MTY1NDE3MzUxMH0.__6b1hCDWLX4lGaRtve8ymIYO7rUKItQ4w6UD-1aZbs");
            http.setRequestProperty("content-type", "application/json");
            http.setRequestProperty("origin", "https://writeforme.org");
            http.setRequestProperty("referer", "https://writeforme.org/");
            http.setRequestProperty("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Opera\";v=\"87\"");
            http.setRequestProperty("sec-ch-ua-mobile", "?0");
            http.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
            http.setRequestProperty("sec-fetch-dest", "empty");
            http.setRequestProperty("sec-fetch-mode", "cors");
            http.setRequestProperty("sec-fetch-site", "same-site");
            http.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.67 Safari/537.36 OPR/87.0.4390.36");
            
            String data = "{\"listType\":\"userTasks\",\"user\":\""+Masters.get(MasterName)+"\",\"query\":\"\",\"limit\":10000,\"skip\":0,\"sortBy\":\"created\",\"sortAsc\":false}";
            byte[] out = data.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);
            
            String response = getResponse(http.getInputStream());
            JSONObject obj = new JSONObject(response);
            
            
            http.disconnect();
            
            return obj;
        } catch (MalformedURLException ex) {
            Logger.getLogger(WFManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(WFManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
}
    
    private String getInformation(String mastername, String slavename){
    return "ok";
    }

    private String getTasksData() {
        try {
            URL url = new URL("https://api.writeforme.org/api/v1/tasks");
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
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

            JSONArray arr = obj.getJSONArray("tasks");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject post_id = arr.getJSONObject(i);
                System.out.println(post_id);
            }
            http.disconnect();
        } catch (MalformedURLException ex) {
            Logger.getLogger(WFManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WFManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "done";
    }

       private static JSONObject getTaskDetails(String taskid){

        try {
            URL url = new URL("https://api.writeforme.org/api/v1/getSolutions");
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("authority", "api.writeforme.org");
            http.setRequestProperty("accept", "application/json, text/plain, */*");
            http.setRequestProperty("accept-language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7");
            http.setRequestProperty("authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjYyNWI0MzUyNWJmOTJlMDAzNzVjOWY3ZCIsInVzZXJuYW1lIjoiRm9vdFNsYXZlTWVkbyIsImFkbWluIjpmYWxzZSwidmVyc2lvbiI6MSwiYmxvY2tlZFVzZXJzIjpbIjYyODMwNjYyNWJmOTJlMDAzNzZiNTQzOCIsIjYyMWZlODQ0ZWViYzQwNzYxN2Y2Y2M2NSJdLCJpYXQiOjE2NTQwODcxMTAsImV4cCI6MTY1NDE3MzUxMH0.__6b1hCDWLX4lGaRtve8ymIYO7rUKItQ4w6UD-1aZbs");
            http.setRequestProperty("content-type", "application/json");
            http.setRequestProperty("origin", "https://writeforme.org");
            http.setRequestProperty("referer", "https://writeforme.org/");
            http.setRequestProperty("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Opera\";v=\"87\"");
            http.setRequestProperty("sec-ch-ua-mobile", "?0");
            http.setRequestProperty("sec-ch-ua-platform", "\"Windows\"");
            http.setRequestProperty("sec-fetch-dest", "empty");
            http.setRequestProperty("sec-fetch-mode", "cors");
            http.setRequestProperty("sec-fetch-site", "same-site");
            http.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.67 Safari/537.36 OPR/87.0.4390.36");
            
            String data = "{\"listType\":\"taskSolutions\",\"task\":\""+taskid+"\",\"query\":\"\",\"limit\":5000,\"skip\":0,\"sortBy\":\"created\",\"sortAsc\":false}";
            
            byte[] out = data.getBytes(StandardCharsets.UTF_8);
            
            OutputStream stream = http.getOutputStream();
            stream.write(out);
            
            String response = getResponse(http.getInputStream());
            JSONObject obj = new JSONObject(response);
            
            http.disconnect();
            
            
            return obj;
        } catch (MalformedURLException ex) {
            Logger.getLogger(WFManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(WFManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
}
