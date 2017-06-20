import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class SampleConn 
{
	private static Logger logger = Logger.getLogger(SampleConn.class);
	private static Properties adapterProperties;
	public static void main(String[] args) 
	{
		try
		{
			logger.info("main");
			initialiseLogger();
			initialiseRequiredProperties();
			
			
			String userCredentials = adapterProperties.getProperty("APIUserName")+":"+adapterProperties.getProperty("APIPassword");
			String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
			logger.info("BasicAuth::::::::"+basicAuth);
			
			String currDT=new SimpleDateFormat("MM/dd/yyyy").format(new Date());
			logger.info("CurrentDate:::::::::::::::::"+currDT);
			Date currDateInDiff=new SimpleDateFormat("MM/dd/yyyy").parse(currDT);
			logger.info("currDateInDiff:::::::::::::::::"+currDateInDiff);
			String currDate=new SimpleDateFormat("yyyyMMdd").format(currDateInDiff);
			logger.info("CurrentDate:::::::::::::::::"+currDate);
			
			
			SimpleDateFormat displayFormat = new SimpleDateFormat("HHmmss");
			SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm:ss a");
			String time="3:24:19 PM";
			Date date = parseFormat.parse(time);
			
			

	
		       System.out.println(parseFormat.format(date) + " = " + displayFormat.format(date));
			
			Connection connection=MYSQLConnectionObject();
			logger.info("Connection object:::::::::::::"+connection);
		}
		catch(Exception exp)
		{
			logger.info(exp);
		}
	}

	public static Connection MYSQLConnectionObject()
	{
		Connection con=null;
		try
		{
			System.out.println("Inside conn method");
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			String dbURL = adapterProperties.getProperty("sampleprop");
			logger.info(dbURL);
			con = DriverManager.getConnection(dbURL); 
			logger.info("Connection Object::::::::::"+con);
		} 
		catch (Exception exception) 
		{
			logger.info("Exception caused in the method MYSQLConnectionObject:::::"+exception);
		}
		return con;
	}

	public static void initialiseLogger()
	{
		try
		{
			String log4jConfigFile = System.getProperty("user.dir") + File.separator + "log4j.properties";
			PropertyConfigurator.configure(log4jConfigFile);
		}
		catch(Exception exception)
		{
			logger.info("Exception caused in the method InitialiseLogger:::::"+exception);
		}
	}

	public static void initialiseRequiredProperties()
	{
		String adapterConfigFile = System.getProperty("user.dir") + File.separator + "adapter.properties";
		adapterProperties=new Properties();
		InputStream inputStream = null;
		try 
		{
			inputStream = new FileInputStream(adapterConfigFile);
			adapterProperties.load(inputStream);
		}
		catch(Exception exception)
		{
			logger.info("Exception caused in the method InitialiseRequiredProperties:::::"+exception);
		}
	}

}

