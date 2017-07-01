import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

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
			BigDecimal bd=new BigDecimal(.6);
			System.out.println("BD::::::"+bd);
			/*Decimal bd1=new Decimal(0.6);
			System.out.println("BD::::::"+bd1);*/
			Float f=new Float(.6);
			System.out.println("f::::"+f.toString());
			logger.info("main");
			initialiseLogger();
			initialiseRequiredProperties();
			
			Connection connection=null;
			try
			{
				logger.info("Inside conn method");
				Class.forName("com.mysql.jdbc.Driver");
				String dbURL = adapterProperties.getProperty("URL");
				String dbUserName = adapterProperties.getProperty("DBUser");
				String dbPassword = adapterProperties.getProperty("DBPassword");
				logger.info("ConnectionURL"+dbURL);
				connection = DriverManager.getConnection(dbURL,dbUserName,dbPassword); 
				logger.info("Connection::::::::::"+connection);
			} 
			catch (Exception exception) 
			{
				logger.info("Exception caused in the method MYSQLConnectionObject:::::"+exception);
			}
			
			logger.info("Connection object:::::::::::::"+connection);
			
			Statement stmt=connection.createStatement();
			ResultSet rs=stmt.executeQuery("Select pointval from test");
			while(rs.next())
			{
				System.out.println("SSSSSS:::"+rs.getString("pointVal"));
				System.out.println("SSSSSS:::"+rs.getFloat("pointVal"));
				System.out.println("SSSSSS:::"+rs.getBigDecimal("pointVal"));
				System.out.println("SSSSSS:::"+rs.getDouble("pointVal"));
			}
		}
		catch(Exception exp)
		{
			logger.info(exp);
		}
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

