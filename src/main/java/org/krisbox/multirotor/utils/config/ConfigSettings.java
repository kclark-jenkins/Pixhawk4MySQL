package org.krisbox.multirotor.utils.config;

/**
 * Created by Kristopher Clark on 11/11/2016.
 */
public class ConfigSettings {
    private boolean autowireDatabase;
    private boolean autowireTables;
    private String  matlab;
    private String  database;
    private String  url;
    private String  username;
    private String  password;
    private String  config;
    private String  driver;
    private String  flightnumber;

    public void setAutowireDatabase(boolean autowireDatabase){this.autowireDatabase=autowireDatabase;}
    public void setAutowireTables(boolean autowireTables){this.autowireTables=autowireTables;}
    public void setMatlab(String matlab){this.matlab=matlab;}
    public void setDatabase(String database){this.database=database;}
    public void setUrl(String url){this.url=url;}
    public void setUsername(String username){this.username=username;}
    public void setPassword(String password){this.password=password;}
    public void setDriver(String driver){this.driver=driver;}
    public void setFlightnumber(String flightnumber){this.flightnumber=flightnumber;}

    public boolean getAutowireDatabase(){return autowireDatabase;}
    public boolean getAutowireTables(){return autowireTables;}
    public String getMatlab(){return matlab;}
    public String getDatabase(){return database;}
    public String getUrl(){return url;}
    public String getUsername(){return username;}
    public String getPassword(){return password;}
    public String getDriver(){return driver;}
    public String getFlightnumber(){return flightnumber;}
}
