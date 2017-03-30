package com.google.edgemicro.process;

import com.google.edgemicro.EdgemicroBeanPostProcessor;
import com.google.edgemicro.annotations.EdgeMicro;
import com.google.edgemicro.annotations.EdgeMicroPrivateConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by seanwilliams on 3/24/17.
 *
 * Responsible for managing all functions related to the Edge Microgateway.
 * i.e. install microgateway, configure it, start it.
 */
public class EdgeMicroProcess {
    private static Logger logger = Logger.getLogger(EdgeMicroProcess.class.getName());
    private Map<String, Annotation> annotationList;
    private String PATH = "/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin:/usr/local/bin/node:/usr/local/bin/npm:/usr/local/bin/edgemicro";
    private String edgemicroStdOut;
    private String edgemicroErrOut;
    private Map<String, String> edgemicroConfigureResults;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private long edgemicroPID;

    public EdgeMicroProcess(Map<String, Annotation> annotationList){
        this.annotationList = annotationList;
    }

    /**
     * Not currently consumed or tested.
     *
     * Install Edge Microgateway if it is not installed already.
     * @throws IOException
     */
    private void installEM() throws IOException{
        List<String> command = new ArrayList<>();
        command.add("/bin/bash");
        command.add("-c");
        command.add("/usr/local/bin/npm");
        command.add("install");
        command.add("-g edgemicro@3.2.3");
        ProcessBuilder pb = new ProcessBuilder(command);
        Map<String, String> env = pb.environment();
        env.put("PATH", "/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin/node:/usr/local/bin/npm");
        logger.info("process env: " + pb.environment().toString());
        Process myProcess = pb.start();
        printResult(myProcess);
    }


    /**
     * Executes the configure command (private or public) depending on the
     * annotations.
     */
    public void configure(){
        EdgeMicro em = (EdgeMicro) annotationList.get(EdgemicroBeanPostProcessor.EDGEMICRO);
        EdgeMicroPrivateConfig emp = null;
        List<String> command = new ArrayList<>();
        command.add("/bin/bash");
        command.add("-c");
        String configureCommand = "edgemicro ";
        String privateCommand = "";

        if(em.privateConfig()){
            configureCommand += "private configure";
            emp = (EdgeMicroPrivateConfig)annotationList.get(EdgemicroBeanPostProcessor.PRIVATE);
            privateCommand = " -r " + emp.runtimeURL() + " -m " + emp.mgmtURL() + " -v " + emp.virtualHosts();
        } else {
            configureCommand += "configure";
        }

        String configureGeneral = " -o " + em.org() + " -e " + em.env() + " -u " + em.admin() + " -p " + em.password();
        configureCommand += configureGeneral + privateCommand;
        command.add(configureCommand);

        ProcessBuilder pb = new ProcessBuilder(command);
        Map<String, String> env = pb.environment();
        env.put("PATH", PATH);
        logger.info(pb.environment().toString());
        Process myProcess = null;
        try {
            myProcess = pb.start();
        } catch (IOException e){
            logger.severe("Error configuring microgateway.");
            logger.severe(e.getMessage());
        }
        saveResults(myProcess);
        printResult();
        edgemicroConfigureResults = processEdgeMicroConfigureResults(myProcess);

    }

    /**
     * Start the Edge Microgateway in a separate process.
     */
    public void start(){
        EdgeMicro em = (EdgeMicro) annotationList.get(EdgemicroBeanPostProcessor.EDGEMICRO);
        //ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<Long> t = new EdgemicroConfigureTask(em, edgemicroConfigureResults);
        Future<Long> future = executor.submit(t);
        try {
            edgemicroPID = future.get(); //wait for the response
        } catch (InterruptedException e){
            logger.severe(e.getMessage());
        } catch (ExecutionException e) {
            logger.severe(e.getMessage());
        }
    }

    /**
     * Registers a shutdown hook to shutdown the Edge Microgateway instance correctly.
     * This is not a fool-proof plan.  There could be instances where the Microgateway
     * does not shutdown correctly, meaning that the JVM may not call this function in every instance.
     */
    public void registerShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                logger.info("attempt to shutdown executor");
                List<String> command = new ArrayList<>();
                command.add("/bin/bash");
                command.add("-c");
                command.add("edgemicro stop");
                logger.info("Stopping Edgemicro");
                Process p = executeCommand(command);
                executor.shutdown();
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
            catch (InterruptedException e) {
                logger.severe("tasks interrupted");
            } catch(IOException e){
                logger.severe("IO Exception!");
            }
            finally {
                if (!executor.isTerminated()) {
                    logger.info("cancel non-finished tasks");
                }
                executor.shutdownNow();
                logger.info("shutdown finished");
            }
        }));

    }

    /**
     * Read the results of the configure command and store the key, secret and YAML file in a map.
     * @param p Edge Microgateway process
     * @return Map of the key, secret and YAML
     */
    private Map<String, String> processEdgeMicroConfigureResults(Process p){
        Map<String, String> temp = extractKeyAndSecret(edgemicroStdOut);
        temp.put(EdgemicroBeanPostProcessor.EDGEMICRO_CONFIG_FILE, extractEdgemicroConfigFileLocation(edgemicroStdOut));
        return temp;
    }

    /**
     * Extract the key and secret from the results of the configure command.
     * @param results of the edgemicro configure command.
     * @return map of the key and secret.
     */
    private Map<String, String> extractKeyAndSecret(String results){
        Map<String, String> temp = new HashMap<>();
        String keyRegex = "(key:\\s)(.*)";
        String secretRegex = "(secret:\\s)(.*)";
        if(results.contains("key:")) {
            temp.put("key", getValue(keyRegex, results));
        }
        if(results.contains("secret:")) {
            temp.put("secret",getValue(secretRegex, results));
        }
        return temp;
    }

    /**
     * Extract the Edgemicro configure file.
     *
     * @param results of the Edge Microgateway configure process as a String
     * @return YAML file location as a string.
     */
    private String extractEdgemicroConfigFileLocation(String results){
        return getValue("(saving configuration information to:)\\s(.*)", results);
    }

    /**
     * Search for a value with Regex in a String argument.
     *
     * @param regex
     * @param input String to search
     * @return matching pattern as a String
     */
    private String getValue(String regex, String input){
        Pattern p = Pattern.compile(regex);
        //  get a matcher object
        Matcher m = p.matcher(input);
        if(m.find()) {
            return m.group(2);
        }
        return null;
    }

    /**
     * Save the results of a process into a String variable to access it later.
     *
     * @param p Process to stringify
     */
    private void saveResults(Process p){
        edgemicroStdOut = stringifyResults(p.getInputStream());
        edgemicroErrOut = stringifyResults(p.getErrorStream());
    }

    /**
     * Convert the InputStream into a String.
     * @param p Process that has output that needs to be converted to a String
     * @return String output of the process results.
     */
    private String stringifyResults(InputStream p){
        StringBuilder sb = new StringBuilder();
        String s = null;
        try(BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p))
        ) {
            while ((s = stdInput.readLine()) != null) {
                sb.append(s + "\n");
            }
        } catch(IOException e){
            e.printStackTrace();
        } finally {
            return sb.toString();
        }
    }

    /**
     * Print the String output to the logger.
     */
    private void printResult(){
        logger.info("Here is the standard output of the command:\n");
        logger.info(edgemicroStdOut);
        logger.severe("Here is the standard error of the command (if any):\n");
        logger.severe(edgemicroErrOut);
    }

    /**
     * Print the process input stream to the logger.
     * @param p Process which we need to read the results and print.
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

    /**
     * Execute a command with Process builder.
     * //TODO use this in the install and configure commands
     *
     * @param command list of commands to execute.
     * @return Process that this command started
     * @throws IOException
     */
    private Process executeCommand(List<String> command) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(command);
        Map<String, String> env = pb.environment();
        env.put("PATH", PATH);
        logger.info("process env: " + pb.environment().toString());
        Process myProcess = pb.start();
        return myProcess;
    }

    /**
     * Return the configure results property.
     * //TODO not sure if this is needed now that its encapsulated.
     *
     * @param key String key
     * @return value as a String
     */
    public String getProperty(String key){
        return edgemicroConfigureResults.get(key);
    }
}
