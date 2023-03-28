package com.attunedlabs.integrationfwk.activities.bean;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.io.File;

import org.apache.camel.Exchange;
import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.integrationfwk.config.jaxb.GroovyScript;
import com.attunedlabs.integrationfwk.config.jaxb.GroovyScriptActivity;
import com.attunedlabs.integrationfwk.config.jaxb.PipeActivity;
import com.attunedlabs.integrationfwk.groovyactivity.config.GroovyScriptClassLoader;
import com.attunedlabs.integrationfwk.groovyactivity.config.GroovyScriptUtil;
import com.attunedlabs.integrationfwk.groovyactivity.config.GroovycScriptClassGenerator;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class GroovyScriptProcessor {
	private Logger logger = LoggerFactory.getLogger(GroovyScriptProcessor.class.getName());
	private static GroovyShell groovyShell = new GroovyShell();;

	/**
	 * @param exchange
	 * @throws GroovyScriptActivityException
	 */
	public void processor(Exchange exchange) throws GroovyScriptActivityException {
		String methodName = "processor";
		logger.debug("{} entered into the method {}.{}()", LEAP_LOG_KEY,getClass().getName(), methodName);
		PipeActivity pipeactivity = (PipeActivity) exchange.getIn().getHeader(ActivityConstant.PIPEACTIVITY_HEADER_KEY);
		GroovyScript groovyScript = getGroovyScript(pipeactivity);
		String scriptingExpression = groovyScript.getScriptingExpression();
		logger.trace("{} scriptingExpression : {}",LEAP_LOG_KEY, scriptingExpression);
		if (scriptingExpression == null)
			throw new GroovyScriptActivityException("ScriptingExpression must be specified");
		Object executeScriptExpression = executeScriptExpression(scriptingExpression, exchange, pipeactivity);
		exchange.getIn().setBody(executeScriptExpression);
		logger.debug("{} exiting from the {}.{}()", LEAP_LOG_KEY,getClass().getName(), methodName);
	}// ..end of the method

	/**
	 * @param scriptingExpression
	 * @param exchange
	 * @param name
	 * @param pipeactivity
	 * @return
	 * @throws GroovyScriptActivityException
	 */
	private Object executeScriptExpression(final String scriptingExpression, Exchange exchange,
			PipeActivity pipeactivity) throws GroovyScriptActivityException {
		try {
			GroovyScriptClassLoader classLoader = GroovyScriptClassLoader.getInstance();
			String staticConfigCompletePath = GroovyScriptUtil.getStaticConfigCompletePath(exchange);
			final String destinationFolder = staticConfigCompletePath + ActivityConstant.BACKWORD_SLASH
					+ ActivityConstant.CLASS_FOLDER;
			final String sourceFolder = staticConfigCompletePath;
			String checkSumValue = GroovyScriptUtil.getCheckSumValue(scriptingExpression);
			File checkSumFile = new File(destinationFolder + ActivityConstant.BACKWORD_SLASH + checkSumValue
					+ ActivityConstant.CLASS_FILE_EXTENTION);
			Script scriptObj = null;
			if (checkSumFile.exists() && !checkSumFile.isDirectory()) {
				logger.trace("{} : {}  exists in the folder...",LEAP_LOG_KEY,checkSumFile);
				scriptObj = classLoader.executeGroovyScript(destinationFolder, checkSumValue, pipeactivity);
			} else {
				logger.trace("{} : {}  not exists in the folder...",LEAP_LOG_KEY,checkSumFile);
				try {
					GroovycScriptClassGenerator classGenerator = GroovycScriptClassGenerator.getInstance();
					classGenerator.generateClassFiles(scriptingExpression, checkSumValue, sourceFolder + "\\groovy",
							destinationFolder);
					scriptObj = classLoader.executeGroovyScript(destinationFolder, checkSumValue, pipeactivity);
				} catch (GroovyScriptActivityException e) {
					e.printStackTrace();
				}
			}
			if (scriptObj == null) {
				logger.trace("{} scriptObj == null",LEAP_LOG_KEY);
				scriptObj = groovyShell.parse(scriptingExpression);
			}
			Binding binding = new Binding(exchange.getIn().getHeaders());
			binding.setVariable(ActivityConstant.EXCHANGE, exchange);
			scriptObj.setBinding(binding);
			Object evaluate = scriptObj.run();
			return evaluate;

		} catch (CompilationFailedException ex) {
			logger.error("{} : error :{}",LEAP_LOG_KEY,ex.getMessage());
			ex.printStackTrace();
			throw new GroovyScriptActivityException(ex.getMessage());
		}
	}// ..end of the method

	/**
	 * @param pipeactivity
	 * @return
	 * @throws GroovyScriptActivityException
	 */
	private GroovyScript getGroovyScript(PipeActivity pipeactivity) throws GroovyScriptActivityException {
		String methodName = "getGroovyScript";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		if (pipeactivity == null)
			throw new GroovyScriptActivityException(ActivityConstant.PIPEACTIVITY_HEADER_KEY + " is empty");
		logger.trace("{} pipelineActivity in GroovyScriptProcessor : {}" ,LEAP_LOG_KEY, pipeactivity.getGroovyScriptActivity());
		GroovyScriptActivity groovyScriptActivity = pipeactivity.getGroovyScriptActivity();
		if (groovyScriptActivity == null)
			throw new GroovyScriptActivityException("GroovyScriptActivity is empty");
		GroovyScript groovyScript = groovyScriptActivity.getGroovyScript();
		if (groovyScript == null)
			throw new GroovyScriptActivityException("GroovyScript is empty");
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return groovyScript;
	}// ..end of the method

}
