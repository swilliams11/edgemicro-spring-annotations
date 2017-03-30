package com.google.edgemicro.process;

import com.google.edgemicro.annotations.EdgeMicro;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Created by seanwilliams on 3/22/17.
 *
 * Responsible for starting Edge Microgateway as a separate process and returning the process ID.
 */
public class EdgemicroConfigureTask implements Callable<Long> {
    private EdgeMicro em;
    private Map<String, String> configureResults;
    private int DEFAULT_PORT = 8000;
    private static Logger logger = Logger.getLogger(EdgemicroConfigureTask.class.getName());
    //TODO externalize this path and make it generic. Needs to be able to run in Cloud Foundry or other *nix systems.
    private String PATH = "/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/usr/local/bin/node:/usr/local/bin/npm:/usr/local/bin/edgemicro";

    public EdgemicroConfigureTask(EdgeMicro em, Map<String, String> results){
        this.em = em;
        configureResults = results;
    }

    /**
     * Executes the edgemicro start command with the key and secret from configure command.
     *
     * @return process ID //TODO no need to return this now.
     * @throws Exception
     */
    @Override
    public Long call() throws Exception {
        long pid = 0;
        try {
            List<String> command = new ArrayList<>();
            command.add("/bin/bash");
            command.add("-c");
            String edgeMicroStartCommand = "edgemicro start " +
                    " -o " + em.org() +
                    " -e " + em.env() +
                    " -k " + configureResults.get("key") +
                    " -s " + configureResults.get("secret");

            if (em.port() != DEFAULT_PORT) {
                edgeMicroStartCommand += " --port " + em.port();
            }
            logger.info(edgeMicroStartCommand);
            command.add(edgeMicroStartCommand);
            ProcessBuilder pb = new ProcessBuilder(command);
            Map<String, String> env = pb.environment();
            env.put("PATH", PATH);
            logger.info(pb.environment().toString());
            Process myProcess = pb.start();
            pid = getPidOfProcess(myProcess);
            logger.info("edgemicro process id is: " + pid);
            //printResult(myProcess);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            return pid;
        }
    }

    /**
     * Obtain the process ID for Edgemicro.
     *
     * @param p Process from which the process ID is obtained.
     * @return process ID as a long
     */
    private long getPidOfProcess(Process p) {
        long pid = -1;

        try {
            if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getLong(p);
                f.setAccessible(false);
            }
        } catch (Exception e) {
            pid = -1;
        }
        return pid;
    }

    /**
     * Prints the results of the process for troubleshooting.
     * @param p Process to print the results.
     */
    private void printResult(Process p) {
        String s = null;
        try(BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()))
        ){
            // read the output from the command
            logger.info("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                logger.info(s);
            }

            // read any errors from the attempted command
            logger.info("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                logger.info(s);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
