package com.attunedlabs.eventframework.dispatcher.channel;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.config.RequestContext;
import com.attunedlabs.eventframework.dispatchchannel.exception.MessageDispatchingException;
import com.attunedlabs.eventframework.dispatchchannel.exception.NonRetryableMessageDispatchingException;

/**
 * Dispatcher Channel Implementation for the File Store.<br>
 * Dispatches all the msessages to file configured in the EventFramework xml
 * 
 * @author Bizruntime
 *
 */
public class FileStoreDispatchChannel extends AbstractDispatchChannel {
	final static Logger logger = LoggerFactory.getLogger(FileStoreDispatchChannel.class);
	private String filePath;// ="D:/Work/ClientSpace/Getusleap/LogDispatcher";
	private String fileName;// ="LogDispatchChannel.txt";
	private File file;
	private FileWriter fw;
	private BufferedWriter bw;

	public FileStoreDispatchChannel(String channeljsonconfig) throws DispatchChannelInitializationException {
		this.channeljsonconfig = channeljsonconfig;
		initializeFromConfig();
	}

	public FileStoreDispatchChannel() {
	}

	/**
	 * This method is used to dispatch message to file
	 * 
	 * @param msg : Object
	 */
	@Override
	public void dispatchMsg(Serializable msg, RequestContext requestContext, String eventId)
			throws MessageDispatchingException {
		String methodName = "dispatchMsg";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		try {
			file.getParentFile().mkdirs();
			if (!file.exists())
				file.createNewFile();
			this.fw = new FileWriter(file.getAbsoluteFile(), true);
			this.bw = new BufferedWriter(fw);
			this.bw.write(msg.toString() + "\n");
			bw.flush();
			logger.trace("{} dispatchmsg {} written", LEAP_LOG_KEY, msg);
			try {
				closeResources();
			} catch (Throwable e) {
				throw new NonRetryableMessageDispatchingException(
						"Error in closing the file resource :" + filePath + ", with file name as : " + fileName);
			}
		} catch (IOException ioexp) {
			throw new NonRetryableMessageDispatchingException(
					"FileStoreDispatchChannel failed to Dispatch EventMsg to file{" + filePath + "//" + fileName + "}",
					ioexp);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	/**
	 * This method is to initialize file location and file name
	 * 
	 * @param channeljsonconfig
	 */
	// #TODO Write clean and better code for Channel.
	public void initializeFromConfig() throws DispatchChannelInitializationException {
		try {
			String methodName = "initializeFromConfig";
			logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
			parseConfiguration(this.channeljsonconfig);
			if (filePath != null && !filePath.isEmpty() && fileName != null && !fileName.isEmpty()) {
				logger.trace("{} file path in file dispatcher: {}/{}", LEAP_LOG_KEY, filePath, fileName);
				file = new File(filePath + "/" + fileName);
				file.getParentFile().mkdirs();
				if (!file.exists())
					file.createNewFile();
			} else {
				logger.debug("initializeFromConfig filePath null or filename null {}", LEAP_LOG_KEY);
			}
			logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		} catch (ParseException | IOException e) {
			throw new DispatchChannelInitializationException(
					"Failed to Initialize FileStoreDispatchChannel for config=" + channeljsonconfig, e);
		}
	}// end of method

	/**
	 * This method is to parse json configuration
	 * 
	 * @param channeljsonconfig
	 * @throws ParseException
	 */
	private void parseConfiguration(String channeljsonconfig) throws ParseException {
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(channeljsonconfig);
		JSONObject jsonObject = (JSONObject) obj;
		this.filePath = (String) jsonObject.get("filepath");
		this.fileName = (String) jsonObject.get("filename");
	}

	private void closeResources() throws Throwable {
		try {
			if (bw != null)
				bw.close();
			if (fw != null)
				fw.close();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	protected void finalize() throws Throwable {
		closeResources();
	}
}// end of class
