package org.krisbox.multirotor.utils.mysql;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.krisbox.multirotor.utils.config.ConfigSettings;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.krisbox.multirotor.utils.mysql.DBHelper.CREATION_TYPE.DATABASE;
import static org.krisbox.multirotor.utils.mysql.DBHelper.CREATION_TYPE.TABLE;

@SuppressWarnings("SqlDialectInspection")
public class DBHelper {
    private final Logger LOGGER = Logger.getLogger(DBHelper.class);
    private ConfigSettings cli = new ConfigSettings();

    enum CREATION_TYPE {
        TABLE, DATABASE
    }

    public DBHelper(ConfigSettings cli) {
        this.cli = cli;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }

        LOGGER.info("com.mysql.cj.jdbc.Driver");
        if(cli.getAutowireDatabase()) {
            LOGGER.info("Creating database \"" + cli.getDatabase() + "\"");
            create(DATABASE, "CREATE DATABASE IF NOT EXISTS " + cli.getDatabase());
        }else{
            LOGGER.info("Not creating database, autowire turned off");
        }
    }

    @SuppressWarnings({"JpaQueryApiInspection", "SqlNoDataSourceInspection", "UnusedAssignment"})
    public void startImport(MatFileReader mfr) {
        try {
            Connection conn  = null;

            conn = DriverManager.getConnection(cli.getUrl() + cli.getDatabase() + "?useSSL=true&requireSSL=false",
                    cli.getUsername(),
                    cli.getPassword());

            Map<String, MLArray> t = mfr.getContent();
            Connection finalConn = conn;

            if(cli.getAutowireTables())
                create(TABLE, "CREATE TABLE IF NOT EXISTS FLIGHTLOGS (ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,FLIGHT_NUMBER DOUBLE NOT NULL)");

            long flightnumber;

            if(cli.getFlightnumber().equals("*")) {
                java.nio.file.Path p = java.nio.file.Paths.get(cli.getMatlab());
                String file = p.getFileName().toString();
                file = file.replaceAll(".tlog.mat", "");
                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH-mm-ss");
                DateTime dt = formatter.parseDateTime(file);
                flightnumber = dt.getMillis()/1000;
            }else {
                flightnumber = Long.parseLong(cli.getFlightnumber());
            }

            final int[] counter = {1};
            Connection t1 = DriverManager.getConnection(cli.getUrl() + cli.getDatabase() + "?useSSL=true&requireSSL=false", cli.getUsername(), cli.getPassword());
            Map<String, ArrayList<Double>> table = new HashMap<>();
            ResultSetMetaData              meta  = getColumnResultSet(t1);

            LOGGER.info("Flattening matlab data");
            t.forEach((k, v)-> {
                if(cli.getAutowireTables()) {
                    createColumn(finalConn, meta , k);
                }

                double[][] mlArrayDouble = ((MLDouble) mfr.getMLArray(k)).getArray();
                ArrayList<Double> row = new ArrayList<>();
                for (double[] aMlArrayDouble : mlArrayDouble) {
                    row.add(aMlArrayDouble[1]);
                }
                table.put(k, row);

                counter[0]++;
            });

            final String[]  columns = new String[1];
            final String[]  insertColumns = new String[1];
            final String[]  insertParameters = new String[1];
            final boolean[] firstCol = {true};

            table.forEach((k, v)-> {
                if(firstCol[0]) {
                    columns[0] = k + " DOUBLE";
                    firstCol[0] = false;
                    insertColumns[0] = k + ",";
                    insertParameters[0] = "values (?,";
                }else {
                    columns[0] += "," + k + " DOUBLE";
                    insertColumns[0] += k + ",";
                    insertParameters[0] += "?,";
                }
            });

            insertColumns[0] = insertColumns[0].substring(0,insertColumns[0].length()-1);
            insertParameters[0] = insertParameters[0].substring(0,insertParameters[0].length()-1);
            String insert = "insert into FLIGHTLOGS (FLIGHT_NUMBER," + insertColumns[0] + ")" + insertParameters[0] + ",?)";

            final int[] count = {0};

            table.forEach((k, v)-> {
                if(v.size() > count[0]) {
                    count[0] = v.size();
                }
            });

            LOGGER.info("Writing to table");

            for(int i=0; i<count[0]; i++) {
                if(i%100==0) {
                    LOGGER.info(i+1 + " of " + count[0]);
                }

                PreparedStatement preparedStmt = conn.prepareStatement(insert);

                int rowNum = i;
                final int[] rowCounter = {1};
                table.forEach((k, v)-> {
                    Double currentValue;
                    try {
                        currentValue = v.get(rowNum);
                    }catch(IndexOutOfBoundsException ex){
                        currentValue = 0.0;
                    }

                    try {
                        if(Double.isNaN(currentValue))
                            currentValue = 0.0;

                        preparedStmt.setDouble(rowCounter[0]+1, currentValue);
                        rowCounter[0]++;
                    }catch(SQLException ex){
                        LOGGER.fatal(ex);
                    }
                });
                preparedStmt.setDouble(1, flightnumber);
                //LOGGER.info(preparedStmt);
                preparedStmt.execute();
            }
        }catch(Exception ex){
            LOGGER.fatal(ex);
        }
    }

    @SuppressWarnings({"SqlNoDataSourceInspection", "UnusedAssignment"})
    private ResultSetMetaData getColumnResultSet(Connection conn) {
        ResultSet rs = null;
        ResultSetMetaData metaData = null;
        String sql = "SELECT * FROM flightlogs limit 1";

        try {
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            metaData = rs.getMetaData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return metaData;
    }

    @SuppressWarnings({"SqlNoDataSourceInspection", "UnusedAssignment"})
    private void createColumn(Connection conn, ResultSetMetaData metaData, String column) {
        Statement  stmt  = null;
        try {
            stmt = conn.createStatement();
            int rowCount = metaData.getColumnCount();
            boolean isMyColumnPresent = false;
            for (int i = 1; i <= rowCount; i++) {
                if (column.equals(metaData.getColumnName(i))) {
                    isMyColumnPresent = true;
                }
            }

            if (!isMyColumnPresent) {
                LOGGER.info("Column " + column + " doesn't exist, creating.");
                stmt.executeUpdate("ALTER TABLE flightlogs ADD " + column + " DOUBLE DEFAULT 0.0");
            }
        }catch(SQLException ex){
            LOGGER.fatal(ex);
        }
    }

    @SuppressWarnings("UnusedAssignment")
    private void create(CREATION_TYPE type, String query) {
        Connection conn  = null;
        Statement  stmt  = null;

        try {
            switch(type) {
                case DATABASE:
                    conn = DriverManager.getConnection(cli.getUrl() + "?useSSL=true&requireSSL=false",
                            cli.getUsername(),
                            cli.getPassword());

                    stmt = conn.createStatement();
                    stmt.executeUpdate(query);
                    LOGGER.info("Done creating database");
                    break;
                case TABLE:
                    conn = DriverManager.getConnection(cli.getUrl() + cli.getDatabase() + "?useSSL=true&requireSSL=false",
                            cli.getUsername(),
                            cli.getPassword());

                    stmt = conn.createStatement();
                    stmt.executeUpdate(query);
                    LOGGER.info("Done creating tables");
                    break;
            }
        }catch(SQLException ex){
            ex.printStackTrace();
        }finally{
            try {
                if(stmt != null)
                    stmt.close();
            }catch(SQLException ex){
                ex.printStackTrace();
            }

            try {
                if(stmt != null)
                    stmt.close();
            }catch(SQLException ex){
                ex.printStackTrace();
            }
        }
    }
}
