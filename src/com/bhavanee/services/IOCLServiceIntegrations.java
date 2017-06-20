package com.bhavanee.services;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.bhavanee.beans.ServiceBeans;
import com.bhavanee.logging.Logging;
import com.google.gson.Gson;

public class IOCLServiceIntegrations
{
	public static Logger logger = Logger.getLogger(IOCLServiceIntegrations.class);
	public ResultSet resultSet=null;
	public int retryCount=0;
	public static Properties adapterProperties=null;
	public static Logging logging=null;
	public static Connection conn=null;

	public IOCLServiceIntegrations(ResultSet resultSet,int retryCount,Properties adapterProperties,Connection conn)
	{
		IOCLServiceIntegrations.conn=conn;
		this.resultSet=resultSet;
		this.retryCount=retryCount;
		IOCLServiceIntegrations.adapterProperties=adapterProperties;
		logging=new Logging(System.getProperty("user.dir") + File.separator +"\\Logs");
	}

	public void createAndSendAPIRequest() throws SQLException
	{
		logger.info("Entered into createAndSendAPIRequest method"+resultSet);
		if(resultSet!=null)
		{
			try 
			{
				while(resultSet.next())
				{
					try
					{
						logger.info(":::::::::::::Entered into Result Set::::::::::");
						int maxRetryCount=retryCount;
						ServiceBeans serviceBean=new ServiceBeans();
						serviceBean.setPlantCode(resultSet.getString("PlantCode"));
						serviceBean.setMaterialNo(resultSet.getString("MaterialNo"));
						serviceBean.setTankNo(resultSet.getString("TankNo"));

						String currDT=new SimpleDateFormat("MM/dd/yyyy").format(new Date());
						logger.info("CurrentDate:::::::::::::::::"+currDT);
						Date currDateInDiff=new SimpleDateFormat("MM/dd/yyyy").parse(currDT);
						logger.info("currDateInDiff:::::::::::::::::"+currDateInDiff);
						String currDate=new SimpleDateFormat("yyyyMMdd").format(currDateInDiff);
						logger.info("CurrentDate:::::::::::::::::"+currDate);
						serviceBean.setDate(currDate);

						//serviceBean.setDate(resultSet.getString("DT"));

						SimpleDateFormat displayFormat = new SimpleDateFormat("HHmmss");
						SimpleDateFormat parseFormat = new SimpleDateFormat("hh:mm:ss a");
						String time=adapterProperties.getProperty("Time");
						Date date = parseFormat.parse(time);
						serviceBean.setTime(displayFormat.format(date));

						//serviceBean.setTime(resultSet.getString("TM"));
						String guageType=resultSet.getString("GaugeType");
						serviceBean.setGaugeType(guageType);
						if(guageType.equalsIgnoreCase("p"))
						{
							serviceBean.setProductDip(resultSet.getString("ProductDip_P"));	
						}
						else
						{
							serviceBean.setProductDip(resultSet.getString("ProductDip_s"));
						}
						serviceBean.setDensity(resultSet.getString("Density"));
						serviceBean.setTemperature(resultSet.getString("Temperature"));
						serviceBean.setTankStatus(resultSet.getString("TankStatus"));
						serviceBean.setPressure(resultSet.getString("Pressure"));
						serviceBean.setDipType(resultSet.getString("DipType"));
						serviceBean.setRemarks(resultSet.getString("Remarks"));
						serviceBean.setPostStatus("Y");

						boolean successFlag=sendingClientRequest(serviceBean);
						logger.info("Success flag for the first attempt:::::::::::"+successFlag);
						if(successFlag==false)
						{
							while(maxRetryCount>0)
							{
								boolean successFlagq=sendingClientRequest(serviceBean);
								logger.info("Success flag after retrying"+"("+maxRetryCount+")"+":::::::::::"+successFlag);
								if(successFlagq==false)
								{
									maxRetryCount--;
									Thread.sleep(Long.parseLong(adapterProperties.getProperty("RetryTimeInterval")));
									continue;
								}
								else
								{
									maxRetryCount=retryCount;
									break;
								}
							}
						}
						maxRetryCount=retryCount;
					}
					catch(Exception ex)
					{
						logger.info("Inside Catch::::::"+ex);
					}
				}
			}
			catch (Exception exception) 
			{
				logger.info("Exception caused in callToRestAPI method::::::::"+exception);
			}
		}
	}

	public static boolean sendingClientRequest(Object serviceReqBean)
	{
		logger.info("Entered into sendingClientRequest method");
		try
		{
			InetSocketAddress proxyInet = new InetSocketAddress(adapterProperties.getProperty("APIServiceServer"),Integer.parseInt(adapterProperties.getProperty("APIServicePort")));
			
			logger.info("proxyInet::::::::"+proxyInet);
			Proxy proxy = new Proxy(Proxy.Type.HTTP, proxyInet);
			logger.info("proxy:::::::::"+proxy);

			String httpsUrL=adapterProperties.getProperty("ServiceURL");

			URL httpsURl = null;

			httpsURl = new URL(httpsUrL);

			HttpURLConnection httpsUrlConnection = (httpsUrL.contains("https")) ? (HttpsURLConnection)httpsURl.openConnection(proxy) : 
				(httpsUrL.contains("http")) ? (HttpURLConnection)httpsURl.openConnection(proxy) : null;

				logger.info("HttpURLConnection object::::::"+httpsUrlConnection);
				if(httpsUrlConnection==null)
				{
					logger.info("Connection could not be established!!!!");
				}
				else 
				{
					if(adapterProperties.getProperty("TokenBasedAuth").equals("false"))
					{
						String userCredentials = adapterProperties.getProperty("APIUserName")+":"+adapterProperties.getProperty("APIPassword");
						String basicAuth = "Basic " + new String(new Base64().encode(userCredentials.getBytes()));
						logger.info("BasicAuth::::::::"+basicAuth);
						httpsUrlConnection.setRequestProperty("authorization", basicAuth);
					}
					else
					{
						String token = adapterProperties.getProperty("AuthenticationKey");
						logger.info("Token::::::::"+token);
						httpsUrlConnection.setRequestProperty("authorization", token);
					}

					httpsUrlConnection.setRequestMethod("POST");
					httpsUrlConnection.setRequestProperty("Content-Type", "application/json");
					httpsUrlConnection.setConnectTimeout(200000);
					httpsUrlConnection.setUseCaches(false);
					httpsUrlConnection.setDoInput(true);
					httpsUrlConnection.setDoOutput(true);

					Gson gson = new Gson();
					String request = gson.toJson(serviceReqBean);
					logger.info("Request Object:::::::::::"+request);

					logging.performRequestLogging(request);

					OutputStream os = httpsUrlConnection.getOutputStream();
					os.write(request.getBytes());
					os.flush();
					logger.info("httpsUrlConnection.getResponseCode():::::::"+httpsUrlConnection.getResponseCode());
					if (httpsUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) 
					{
						logger.info("Respose code not 200");
						throw new RuntimeException("Failed : HTTP error code : "+ httpsUrlConnection.getResponseCode());
					}

					BufferedReader input = new BufferedReader(new InputStreamReader(httpsUrlConnection.getInputStream()));
					StringBuilder sbuilder = new StringBuilder();
					String str = input.readLine();
					logger.info("str:::::"+str);
					try
					{
						while (str != null) 
						{
							logger.info("str::::::"+str);
							sbuilder.append(str);
							str = input.readLine();
							logger.info("str::::::"+str);
							if (str != null) {
								sbuilder.append("\n");
							}
						}
						/*JSONTokener jsonTokener = new JSONTokener(new InputStreamReader(httpsUrlConnection.getInputStream()));
						  JSONObject jsonResponseObj = new JSONObject(jsonTokener);*/
						logging.performResponseLogging(sbuilder.toString());
						logger.info("Response::::::::"+sbuilder.toString());
					}
					catch(Exception e)
					{
						logger.info(e);
					}
					finally
					{
						input.close();
					}
				}
		}
		catch(Exception e)
		{
			logger.info("Inside Send client Exception:::::::::::"+e);
			logging.performResponseLogging(e);
			logger.info("Exception:::::::::::"+e);
			return false;
		}
		return true;
	}
}


