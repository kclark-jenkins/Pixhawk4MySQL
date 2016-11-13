# Pixhawk4MySQL

Command line utility that parses matlab logs that have been converted from TLogs in mission planner and imports them into a MySQL database.

# Notes

This is a personal side project, use with care.  I recomend not using against any MySQL instances that aren't intended solely for this program.
I am not responsible for any negitive results that may be related to this software.

This program *DOES NOT* handle duplicate rows as of right now.  Do not import a matlab log more than once or you're going to have a bad time.

# Useage

```
java -jar Telemetry-all-1.0-SNAPSHOT.jar -b -t -c "jdbc:mysql://localhost:3306/" -d "blackbox" -u "root" -p "myPassword" -m "C:\Users\Kristopher Clark\Documents\Mission Planner\logs\QUADROTOR\matlab logs\2016-11-10 17-34-53.tlog.mat" -r "com.mysql.cj.jdbc.Driver"
```

# Options

```
java -jar Telemetry-all-1.0-SNAPSHOT.jar --help
usage: Main
 -b,--autowireDatabase      Auto create database
 -c,--connectionURL <arg>   Connection URL for database.  Example:
                            http://localhost:3306/
 -d,--database <arg>        Database name to use
 -h,--help                  show help.
 -m,--matlab <arg>          Matlab file to parse
 -p,--password <arg>        Password for database
 -r,--driver <arg>          Database driver to use
 -t,--autowireTables        Auto create tables (if not exist) based on
                            matlab log
 -u,--username <arg>        Username for database
```

* autowireDatabase - This will auto create the database you've specified if it doesn't exist
* connectionURL    - Connection URL to your SQL installation without the database name, make sure to end this with '/' example: jdbc:mysql:localhost:3306/
* database         - Name of database to import into
* matlab           - Your matlab file.  Example: C:/Users/kclark/2016-11-06 12-43-00.tlog.mat *Note: The file must use this same date time format.  It's parsed and converted to EPOCH and used as unique flight numbers
* password         - The password of your database user
* driver           - Driver string to use, the default is com.mysql.cj.jdbc.Driver
* autowireTables   - This will auto create the table and any new columns introduced with new logs.  It's highly recomended this is left on unless you have created the column mappings ahead of time.
* username         - Database user name to use

# License

MIT License
