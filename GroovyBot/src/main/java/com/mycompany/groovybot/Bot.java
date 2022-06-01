/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.mycompany.groovybot;

import static com.mycompany.groovybot.GroovyBot.getResponse;
import java.util.Collections;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Samed OZKAN
 */
public class Bot extends ListenerAdapter {

    static HashMap<String, String> Masters = new HashMap();

    public static void main(String[] args) throws LoginException {
        // create a file object for the current location
        File file = new File("MastersList.txt");

        try {

            // trying to create a file based on the object
            boolean value = file.createNewFile();
            if (value) {
                System.out.println("The new file is created.");
            } else {
                System.out.println("The file already exists. Filling Masters");

                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader("MastersList.txt"));
                    String line = reader.readLine().stripTrailing();
                    while (line != null) {
                        // read next line
                        System.out.println(line);
                        try {
                            String[] splited = line.split("\t");
                             Masters.put(splited[0].strip(), splited[1].strip());
                            System.out.println("splitted done");
                        } catch (Exception e) {
                            System.out.println("split error");
                        }
                        line = reader.readLine().stripTrailing();

                    }
                    reader.close();
                } catch (IOException e) {
                    e.getStackTrace();
                }

            }

        } catch (Exception e) {
            e.getStackTrace();
        }

        // args[0] should be the token
        // We only need 2 intents in this bot. We only respond to messages in guilds and private channels.
        // All other events will be disabled.
        JDABuilder.createLight("OTgxMjYzNzc2NTU0ODExNDAy.Gq9DBo.H42YC8rRw_DlmumDfdk5tqVUWfmpPfQIz3wVOM", GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES)
                .addEventListeners(new Bot())
                .setActivity(Activity.playing("Type !ping"))
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
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
                Masters.put(split[0].strip(), split[1].strip());
                file = new FileWriter("MastersList.txt");
                // Creates a BufferedWriter
                BufferedWriter output = new BufferedWriter(file);
                // Writes the string to the file
                output.write(split[0].strip()  + "\t" + split[1].strip() + "\n");
                // Closes the writer
                output.close();
                
                msg.reply("\""+ split[0].strip() +" is my new Master from now on !\"").queue();
            } catch (IOException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    file.close();
                } catch (IOException ex) {
                    Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }else if (msg.getContentRaw().contains("!info")){
            String command = msg.getContentRaw().replace("!info ", "");

            getUserInfo(command.strip());
        } else if (msg.getContentRaw().contains("!ping")) {
            msg.reply("\"Master,  please wait while i am preparing your results!\"").queue(response /* => Message */ -> {
                response.editMessageFormat("Thank you for waiting").queue();
            });

        }
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
            System.out.println(response.charAt(0));
            JSONObject obj = new JSONObject(response);
            System.out.println(obj);
        }catch(Exception e){
        }
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
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "done";
    }

}
