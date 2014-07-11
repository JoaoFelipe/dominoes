/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import domain.Configuration;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Daniel
 */
public class ConfigurationFile {

    private String path = "conf.txt";

    /**
     * This Functions is used to load the basic configuration of system
     *
     * @throws IOException
     * @throws Exception
     */
    public void loadConfigurationFile() throws IOException, Exception {

        File file = new File(path);

        if (!file.exists()) {
            file.createNewFile();
            writeBasicConfiguration(file);
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int amount = 1;
            String line = br.readLine();
            String separator = ":";
            String nameVariable = null;
            String valueVariable = null;
            while (amount <= Configuration.amont && line != null) {
                if (line.split(separator).length == 2) {
                    nameVariable = line.split(separator)[0].trim().toLowerCase();
                    valueVariable = line.split(separator)[1].trim().toLowerCase();
                    if (nameVariable.equals("fullscreen")
                            && Boolean.parseBoolean(valueVariable)) {
                        Configuration.fullscreen = Boolean.parseBoolean(valueVariable);
                    } else if (nameVariable.equals("autosave")
                            && Boolean.parseBoolean(valueVariable)) {
                        Configuration.autoSave = Boolean.parseBoolean(valueVariable);
                    } else if (nameVariable.equals("visibilityhistoric")
                            && Boolean.parseBoolean(valueVariable)) {
                        Configuration.visibilityHistoric = Boolean.parseBoolean(valueVariable);
                    } else if (nameVariable.equals("resizable")
                            && Boolean.parseBoolean(valueVariable)) {
                        Configuration.resizable = Boolean.parseBoolean(valueVariable);
                    } else if (nameVariable.equals("width")
                            && isDouble(valueVariable)) {
                        Configuration.width = Double.parseDouble(valueVariable);
                    } else if (nameVariable.equals("height")
                            && isDouble(valueVariable)) {
                        Configuration.height = Double.parseDouble(valueVariable);
                    } else if (nameVariable.equals("listwidth")
                            && isDouble(valueVariable)) {
                        Configuration.listWidth = Double.parseDouble(valueVariable);
                    } else if (nameVariable.equals("accessmode")) {
                        Configuration.accessMode = valueVariable;
                    }
                }
                line = br.readLine();
                amount++;
            }

        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    private void writeBasicConfiguration(File file) throws IOException, Exception {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("fullscreen:\t\t" + Configuration.fullscreen + "\n");
            bw.write("autosave:\t\t\t" + Configuration.autoSave + "\n");
            bw.write("visibilityhistoric:\t" + Configuration.visibilityHistoric + "\n");
            bw.write("resizable:\t\t\t" + Configuration.resizable + "\n");
            bw.write("width:\t\t\t" + Configuration.width + "\n");
            bw.write("height:\t\t\t" + Configuration.height + "\n");
            bw.write("listwidth:\t\t\t" + Configuration.listWidth + "\n");
            bw.write("accessmode:\t\t" + Configuration.accessMode + "\n");
        } catch (IOException ex) {
            throw new IOException(ex.getMessage());
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    private boolean isDouble(String valueVariable) {
        boolean result = true;
        try{
            Double.parseDouble(valueVariable);
        }catch(NumberFormatException ex){
            result = false;
        }
        return result;
    }
}
