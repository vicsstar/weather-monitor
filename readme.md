## This is a Scala SBT console application that monitors for extreme weather conditions based on parameters read from the command line.

The default implementation utilizes the openweathermap.org API for forecasting. Documentation here: https://openweathermap.org/api

### Running the application.

Run the application with this command: `sbt run`
Run tests using the command: `sbt test`

You can also run the application by opening it as an SBT project in IntelliJ IDEA.

### Here are the steps the application takes in achieving weather forecast monitoring:

- Pulls temperature data from the weather API for 5-day forecasts.
- Checks if any of the forecast data has exeeded the limit specified on the command line.
- Saves the data collected to a log file.
- Periodically requests weather forecast data based on the value of the `periodic_check_time` key in `config.properties` resource file.
- Starts a tiny webserver that renders a single page showing the weather monitoring status.
- Extracts data from the log file and renders it to the browser.

Once the app is running, the web server can be accessed at this address: http://127.0.0.1:9000
This shows the weather forecast data and limits in a visual (HTML) interface.

### Configuration

The configuration file resides in the classpath with the name `config.properties`.
It contains the `port` key for specifying the port to which the web server should bind and `api_key` for the weather forecast service.
