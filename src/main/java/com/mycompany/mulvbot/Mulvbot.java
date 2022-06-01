/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.mycompany.mulvbot;

import static com.mycompany.mulvbot.WFMManager.Masters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

/**
 *
 * @author Samed OZKAN
 */
public class Mulvbot {

        public static void main(String[] args)  {
        try {
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
                        String line = reader.readLine().trim();
                        while (line != null) {
                            // read next line
                            System.out.println(line);
                            try {
                                String[] splited = line.split("\t");
                                Masters.put(splited[0].trim(), splited[1].trim());
                                System.out.println("splitted done");
                            } catch (Exception e) {
                                System.out.println("split error");
                            }
                            line = reader.readLine().trim();
                            
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
                    .addEventListeners(new WFMManager())
                    .setActivity(Activity.playing("Serving Master Mulv"))
                    .build().awaitReady();
        } catch (InterruptedException ex) {
            Logger.getLogger(Mulvbot.class.getName()).log(Level.SEVERE, null, ex);
        } catch (LoginException ex) {
            Logger.getLogger(Mulvbot.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
