package org.krisbox.multirotor;

import com.jmatio.io.MatFileReader;
import org.apache.log4j.Logger;
import org.krisbox.multirotor.utils.cli.TelemetryCli;
import org.krisbox.multirotor.utils.config.ConfigSettings;
import org.krisbox.multirotor.utils.mysql.DBHelper;

import java.io.*;

public class TelemetryLogs {
    private final Logger   LOGGER;
    private final DBHelper DB_HELPER;
    private ConfigSettings settings;

    public TelemetryLogs(String[] args) throws IOException {
        LOGGER    = Logger.getLogger(TelemetryLogs.class);
        settings  = new TelemetryCli(args).parse();
        DB_HELPER = new DBHelper(settings);
    }

    public TelemetryLogs startImport() {
        try {
            DB_HELPER.startImport(new MatFileReader(settings.getMatlab()));
        }catch(IOException ex){
            LOGGER.fatal(ex);
        }
        return this;
    }

    public static void main(String[] args) throws IOException {
        new TelemetryLogs(args).startImport();
        //new TelemetryLogs().startImport("C:/Users/Kristopher Clark/Documents/Mission Planner/logs/QUADROTOR/1/2016-11-10 11-08-40.tlog.mat");
    }
}