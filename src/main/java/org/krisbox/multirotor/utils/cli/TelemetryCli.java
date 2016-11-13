package org.krisbox.multirotor.utils.cli;

import org.apache.commons.cli.*;
import org.apache.log4j.Logger;
import org.krisbox.multirotor.utils.config.ConfigSettings;

public class TelemetryCli {
    private final Logger   LOGGER;
    private Options        options  = new Options();
    private String[]       args;

    public TelemetryCli(String[] args) {
        LOGGER    = Logger.getLogger(TelemetryCli.class);

        this.args = args;

        options.addOption("h", "help", false, "show help.");
        options.addOption("b", "autowireDatabase", false, "Auto create database");
        options.addOption("t", "autowireTables"  , false, "Auto create tables (if not exist) based on matlab log");
        options.addOption("m", "matlab", true, "Matlab file to parse");
        options.addOption("d", "database", true, "Database name to use");
        options.addOption("c", "connectionURL", true, "Connection URL for database.  Example:  http://localhost:3306/");
        options.addOption("u", "username", true, "Username for database");
        options.addOption("p", "password", true, "Password for database");
        options.addOption("r", "driver", true,   "Database driver to use");
    }



    public ConfigSettings parse() {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd          = null;
        ConfigSettings config   = new ConfigSettings();

        try {
            cmd = parser.parse(options, args);

            if(cmd.hasOption("h")) {
                help();
            }

            if(cmd.hasOption("b")) {
                config.setAutowireDatabase(true);
            }

            if(cmd.hasOption("t")) {
                config.setAutowireTables(true);
            }

            if(cmd.hasOption("d")) {
                config.setDatabase(cmd.getOptionValue("d"));
            }

            if(cmd.hasOption("c")) {
                config.setUrl(cmd.getOptionValue("c"));
            }

            if(cmd.hasOption("u")) {
                config.setUsername(cmd.getOptionValue("u"));
            }

            if(cmd.hasOption("p")) {
                config.setPassword(cmd.getOptionValue("p"));
            }

            if(cmd.hasOption("m")) {
                config.setMatlab(cmd.getOptionValue("m"));
            }

            if(cmd.hasOption("r")) {
                config.setDriver(cmd.getOptionValue("r"));
            }

            if(!cmd.hasOption("b")) {
                config.setAutowireDatabase(false);
            }

            if(!cmd.hasOption("t")) {
                config.setAutowireTables(false);
            }

            if(!cmd.hasOption("d")) {
                config.setDatabase("blackbox");
            }

            if(!cmd.hasOption("c")) {
                config.setUrl("jdbc:mysql://localhost:3306/");
            }

            if(!cmd.hasOption("u")) {
                config.setUsername("root");
            }

            if(!cmd.hasOption("p")) {
                config.setPassword("");
            }

            if(!cmd.hasOption("m")) {
                System.out.println("matlab file parameter is required");
                help();
                System.exit(0);
            }

            if(!cmd.hasOption("r")) {
                config.setDriver("com.mysql.cj.jdbc.Driver");
            }

            if(!cmd.hasOption("f")) {
                config.setFlightnumber("*");
            }

            return config;
        }catch(ParseException ex){
            LOGGER.fatal(ex);
        }
        return config;
    }

    private TelemetryCli help() {
        HelpFormatter formater = new HelpFormatter();

        formater.printHelp("Main", options);
        System.exit(0);
        return this;
    }
}
