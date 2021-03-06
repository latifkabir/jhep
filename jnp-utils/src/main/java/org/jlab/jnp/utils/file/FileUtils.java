/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.jnp.utils.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gavalian
 */
public class FileUtils {
    
    public static int  DEBUG_MODE = 0;    
    
    /**
     * returns list of file in given directory. By default it skips files
     * that start with ".", and backup files that end with "~".
     * @param directory
     * @return list of files in the directory
     */
    public static List<String>  getFileListInDir(String directory){       
       if(FileUtils.DEBUG_MODE>0) System.out.println(">>> scanning directory : " + directory);
        List<String> fileList = new ArrayList<String>();
        File[] files = new File(directory).listFiles();
        if(files==null){
            if(FileUtils.DEBUG_MODE>0) System.out.println(">>> scanning directory : directory does not exist");
            return fileList;
        }
        //System.out.println("FILE LIST LENGTH = " + files.length);
        for (File file : files) {
            if (file.isFile()) {
                if(file.getName().startsWith(".")==true||
                        file.getName().endsWith("~")){
                    if(FileUtils.DEBUG_MODE>0) System.out.println("[FileUtils] ----> skipping file : " + file.getName());
                } else {
                    fileList.add(file.getAbsolutePath());
                }
            }
        }
        return fileList;
    }
    /**
     * returns list of files in directory that have provided extension "ext".
     * it skips files that start with "." or end with "~".
     * @param directory name of the directory
     * @param ext extension of the files
     * @return list of files in directory with given extension.
     */
    public static List<String>  getFileListInDir(String directory, String ext){
        List<String> files = FileUtils.getFileListInDir(directory);
        List<String> accepted = new ArrayList<String>();
        for(String file : files){
            if(file.endsWith(ext)==true) accepted.add(file);
        }
        return accepted;
    }
    /**
     * returns file list in directory given by relative path to environmental variable
     * "env", extension filter is applied.
     * @param env environmental variable
     * @param directory directory relative to environmental variable
     * @param ext extension to search for
     * @return  list of files that match.
     */
    public static List<String>  getFileListInDir(String env, String directory, String ext){
        String envDirectory = FileUtils.getEnvironmentPath(env, directory);
        if(envDirectory==null){
            return new ArrayList<String>();
        }
        return FileUtils.getFileListInDir(envDirectory, ext);
    }
    /**
     * returns full path for directory given relative to environmental variable.
     * if the environmental variable is not defined NULL will be returned.
     * @param env environmental variable
     * @param dir relative directory path
     * @return full path to directory
     */
    public static String getEnvironmentPath(String env, String dir){
        String envDir = System.getenv(env);
        String proDir = System.getProperty(env);
        if(proDir!=null){
            StringBuilder str = new StringBuilder();
            str.append(proDir);
            if(proDir.endsWith("/")==false) str.append("/");
            str.append(dir);
            String fullPath = str.toString();
            File dirFile = new File(fullPath);
            if(dirFile.exists()==false){
                System.out.println("[FileUtils] ---> directory does not exist : " + fullPath);
                return null;
            }
            return str.toString();
        }
        
        if(envDir!=null){
            StringBuilder str = new StringBuilder();
            str.append(envDir);
            if(envDir.endsWith("/")==false) str.append("/");
            str.append(dir);
            String fullPath = str.toString();
            File dirFile = new File(fullPath);
            if(dirFile.exists()==false){
                System.out.println("[FileUtils] ---> directory does not exist : " + fullPath);
                return null;
            }
            return str.toString();
        }
        System.out.println("[FileUtils] ----> error : Environment variable " + env + " is not set");
        return null;
    }
    /**
     * Reads the content of the file into a List of strings
     * @param filename file name to read.
     * @return List of strings in the file
     */
    public static List<String>  readFile(String filename){
        List<String> items = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            while(line!=null){
                items.add(line);
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
        try {
            File file = new File(filename);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                //System.out.println(scanner.next());
                String line = scanner.next();
                items.add(line);
                System.out.println();
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        return items;
    }
    /**
     * Read entire file and return only strings that start with given expression.
     * @param filename filename to read.
     * @param starts_with expression that will be considered
     * @return List of strings 
     */
    public static List<String>  readFile(String filename, String starts_with, boolean replace){
        
        List<String> items = new ArrayList<String>();
         try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line = br.readLine();
            while(line!=null){
                if(line.startsWith(starts_with)==true){
                    if(replace==false){
                        items.add(line);
                    } else {
                        items.add(line.replace(starts_with, ""));
                    }
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
        try {
            File file = new File(filename);
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNext()) {
                    //System.out.println(scanner.next());
                    String nextLine = scanner.next();
                    if(nextLine.startsWith(starts_with)==true){
                        if(replace==false) {
                            items.add(nextLine);
                        } else {
                            items.add(nextLine.replace(starts_with, ""));
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
        return items;
    }
    /**
     * reads lines from the file into one string
     * @param filename
     * @return String representation of the file
     */
    public static String readFileAsString(String filename){
        List<String> list = FileUtils.readFile(filename);
        StringBuilder str = new StringBuilder();
        for(String item : list){
            str.append(item).append("\n");
        }
        return str.toString();
    }
    /**
     * reads file as a string only reading lines that start with "starts_with",
     * and returns as a single string. if replace==true, the "starts_with" sequence will 
     * be removed.
     * @param filename
     * @param starts_with
     * @param replace
     * @return 
     */
    public static String  readFileAsString(String filename, String starts_with, boolean replace){
        List<String> list = FileUtils.readFile(filename,starts_with,replace);
        StringBuilder str = new StringBuilder();
        for(String item : list){
            str.append(item).append("\n");
        }
        return str.toString();
    }
}
