/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.jnp.utils.options;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author gavalian
 */
public class OptionStore {
    
    private final   Map<String,OptionParser> commands     = new HashMap<String,OptionParser>();
    private final   Map<String,String>       commandsDesc = new HashMap<String,String>();
    private String                      activeCommand = "UNKNOWN";
    private String                        programName = "unknown";
    
    public OptionStore(){
        
    }
    public OptionStore(String name){
        this.programName = name;
    }
    /**
     * Adds a OptionParser with given name, and description to the store.
     * @param command the token for command
     * @param description description of the command. will be shown on showUsage()
     */
    public void addCommand(String command, String description){
        commands.put(command, new OptionParser(programName));
        commandsDesc.put(command, description);
    }
    /**
     * Gets the parser with given name
     * @param name option parser name
     * @return 
     */
    public OptionParser getOptionParser(String name){
        return commands.get(name);
    }
    /**
     * parser the input arguments, and sets the active
     * OptionParser if command is recognized.
     * @param args
     * @return 
     */
    public boolean parse(String[] args){
        if(args.length<1){
            printUsage();
            return false;
        }
        
        if(args.length<2){
            String command = args[0];
            if(commands.containsKey(command)==true){
                commands.get(command).printUsage();
            } else {
                printUsage();
                System.out.println("*** ERROR *** : unknown command : " + command);
            }
            return false;
        }
        if(args.length>=2){
            String command = args[0];
            if(commands.containsKey(command)==false){
                printUsage();
                System.out.println("*** ERROR *** : unknown command : " + command);
                return false;
            }
            
            List<String> arguments = new ArrayList<String>();
            for(int i = 1; i < args.length; i++){
                arguments.add(args[i]);
            }
            commands.get(command).parseList(arguments);
            activeCommand = command;
        }     
        return true;
    }
    /**
     * Check if the parsing went well.
     * @return 
     */
    public boolean isValid(){
        return this.commands.containsKey(this.activeCommand);        
    }
    /**
     * returns the name of the parser.
     * @return program name
     */
    public String getName(){ return programName;}
    /**
     * returns the current command
     * @return 
     */
    public String getCommand(){
        return this.activeCommand;
    }
    /**
     * prints usage for the program. all the arguments and
     * descriptions.
     */
    public void printUsage(){
        System.out.println("\n\n");
        System.out.println("    Usage : " + getName() + " [commands] [options]\n");
        System.out.println("\n    commands:");
        for(Map.Entry<String,OptionParser> entry : commands.entrySet()){
            System.out.println(String.format("%18s : %s", entry.getKey(),
                    commandsDesc.get(entry.getKey())));
        }
        System.out.println("\nChoose wisely.....");
    }
    
    public void printUsage(String option){
        if(commands.containsKey(option)==true){
            commands.get(option).printUsage();
        }
    }
    
    public static void main(String args[]){
        
        OptionStore options = new OptionStore("hipoutils");
        options.addCommand("-compress", "compresses given file to compression level provided");
        options.getOptionParser("-compress").addRequired("-i", "input file to compress");
        options.getOptionParser("-compress").addRequired("-o", "output file name");
        options.getOptionParser("-compress").addOption("-type", "1", "compression level (0=uncompresse,1=fast,2=high)");
        
        String[] list = new String[]{"-compress"};
        options.parse(list);
    }
}
