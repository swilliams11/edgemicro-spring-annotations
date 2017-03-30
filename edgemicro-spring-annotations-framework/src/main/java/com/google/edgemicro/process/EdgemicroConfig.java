package com.google.edgemicro.process;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by seanwilliams on 3/23/17.
 *
 * Responsible for reading the Edge Microgateway config file, updating it
 * and writing changes to disk.
 */
public class EdgemicroConfig {
    private static Logger logger = Logger.getLogger(EdgemicroConfig.class.getName());
    private Path pathToConfig;
    private String configContent;

    public EdgemicroConfig (String pathToConfig){
        this.pathToConfig = Paths.get(pathToConfig);
        readFile();
    }

    /**
     * Read the config file and convert to string and store it in instance variable.
     */
    private void readFile(){
        logger.info("Reading edgemicro config file.");
        Charset charset = StandardCharsets.UTF_8;
        String file = null;
        try {
             configContent = new String(Files.readAllBytes(pathToConfig), charset);
        } catch(IOException e){
            logger.severe("Error reading edgemicro config file.");
            logger.severe(e.getMessage());
        }
    }

    /**
     * Update the config file.
     *
     * @param target value to replace
     * @param replacement replacement value
     */
    public void update(String target, String replacement) {
        configContent = configContent.replaceAll(target, replacement);
    }

    /**
     * Append content to the end of the configuration file.
     *
     * @param text to append to the end of the file as a String.
     */
    public void append(String text){
        configContent = configContent +"\n" + text;
    }

    /**
     * Write the in-memory String content to disk.
     *
     */
    public void save() {
        Charset charset = StandardCharsets.UTF_8;
        try {
            //logger.info("config file is writeable? " + Files.isWritable(edgemicroConfigPath));
            Files.write(pathToConfig, configContent.getBytes(charset));
            logger.info("Saved Edge Microgateway Config file.");
        } catch (IOException e) {
            logger.severe("Unable to save Edge Microgateway config file.");
            logger.severe(e.getMessage());
        }
    }
}
