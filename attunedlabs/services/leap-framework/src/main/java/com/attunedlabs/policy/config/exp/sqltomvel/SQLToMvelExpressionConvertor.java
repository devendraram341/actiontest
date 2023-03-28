package com.attunedlabs.policy.config.exp.sqltomvel;

import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder.IndividualExpEval;
import com.attunedlabs.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder.IndividualExpHolder;
import com.attunedlabs.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder.IndvidualExpEntry;
import com.attunedlabs.policy.config.exp.sqltomvel.PolicySQLDialectExpressionBuilder.TotalExpressionHolder;

public class SQLToMvelExpressionConvertor {
	final Logger logger = LoggerFactory.getLogger(SQLToMvelExpressionConvertor.class);
	
	public String handleExpConversion(TotalExpressionHolder totalExpHolder){
		String methodName = "handleExpConversion";
		logger.debug("{} entered into the method {}, with totalExpHolder {}", LEAP_LOG_KEY, methodName,totalExpHolder);
		List<IndividualExpHolder> indidualExpList=totalExpHolder.getIndExpHolderList();
		StringBuffer expression=new StringBuffer();
		for(IndividualExpHolder indExpHolder:indidualExpList){
			int type =indExpHolder.getType();
			//Its an Expression
			if(type==1){
				String exp=convertIndividualConditionInExp(indExpHolder);
				
				logger.debug("{} Its an Expression {} ",LEAP_LOG_KEY, exp);

				expression.append(exp);
			}if(type==2){	//Its an Operator
				String operator=convertExpOperatorInExp(indExpHolder);
				
				logger.debug("{} Its an  Operator {} ",LEAP_LOG_KEY, operator);

				expression.append(operator);
			}
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return expression.toString();
	}
	public String convertExpOperatorInExp(IndividualExpHolder indExpHolder){
		//logger.debug("Operator indExpHolder="+indExpHolder.getValue());
		String finalOperator=" "+indExpHolder.getValue()+" ";
		return finalOperator;		
	}
	
	public String convertIndividualConditionInExp(IndividualExpHolder indExpHolder){
		String methodName = "convertIndividualConditionInExp";
		logger.debug("{} entered into the method {}, Expression IndividualExpHolder={} ", LEAP_LOG_KEY, methodName,indExpHolder.getValue());
	
		List<IndividualExpEval> indiConditionList=indExpHolder.getIndExpEvalList();
		logger.trace("{} Expression indExpHolder BEFORE fOR ITEM ARE {}",LEAP_LOG_KEY, indiConditionList.size());
		StringBuffer strbuffer=new StringBuffer();
		for(IndividualExpEval indiCondition:indiConditionList){
			String individualFunction=sqlFunctionMapper(indiCondition);
			strbuffer.append(individualFunction);
			logger.debug("{} Expression indExpHolder sql= {} ",LEAP_LOG_KEY, individualFunction);
		}
		logger.info("{} Expression indExpHolder OUT OF fOR",LEAP_LOG_KEY);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return strbuffer.toString();
	}
	public String sqlFunctionMapper(IndividualExpEval IndCond){
		
		String methodName = "sqlFunctionMapper";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		IndvidualExpEntry operatorEntry=IndCond.getOperator();
		String convertedMvlExp=null;
		String operator=operatorEntry.value;
		logger.debug("{} Operator is={} ",LEAP_LOG_KEY, operator);
		if(operatorEntry.value.equalsIgnoreCase("=")){
			convertedMvlExp=SQLToMvelFunctionHandler.equalsFuncHandler(IndCond);
			
			logger.debug("{} convertedMvlExp value - {}",LEAP_LOG_KEY, convertedMvlExp);

		}else if(operatorEntry.value.equalsIgnoreCase("IN")){
			convertedMvlExp=SQLToMvelFunctionHandler.InFuncHandler(IndCond);
			
			logger.debug("{} convertedMvlExp value - {}",LEAP_LOG_KEY, convertedMvlExp);

		}else if(operatorEntry.value.equalsIgnoreCase("NOT IN")){
			convertedMvlExp=SQLToMvelFunctionHandler.NOTINFuncFuncHandler(IndCond);
			logger.debug("{} convertedMvlExp value - {}",LEAP_LOG_KEY, convertedMvlExp);

		}
		else if(operatorEntry.value.equalsIgnoreCase("< >")){
			convertedMvlExp=SQLToMvelFunctionHandler.NotEqualFuncHandler(IndCond);
			logger.debug("{} convertedMvlExp value - {}",LEAP_LOG_KEY, convertedMvlExp);

		}
		
	
		
		else if(operatorEntry.value.equalsIgnoreCase("IS NOT NULL")){
			convertedMvlExp=SQLToMvelFunctionHandler.ISNOTNULLFuncHandler(IndCond);
			logger.debug("{} convertedMvlExp value  - {}",LEAP_LOG_KEY, convertedMvlExp);

		}
		//Todo 
		else if(operatorEntry.value.equalsIgnoreCase("BETWEEN")){

		} 
		else if(operatorEntry.value.equalsIgnoreCase("IS NULL")){
			convertedMvlExp=SQLToMvelFunctionHandler.ISNULLFuncHandler(IndCond);
			logger.debug("{} convertedMvlExp value - {}",LEAP_LOG_KEY, convertedMvlExp);

		} 
		else if(operatorEntry.value.equalsIgnoreCase("!=")){
			convertedMvlExp=SQLToMvelFunctionHandler.NotEqualFuncHandler(IndCond);
			logger.debug("{} convertedMvlExp value  - {}",LEAP_LOG_KEY, convertedMvlExp);

		} else if(operatorEntry.value.equalsIgnoreCase(">")){
			convertedMvlExp=SQLToMvelFunctionHandler.GRAETERFuncHandler(IndCond);
			logger.debug("{} convertedMvlExp value - {}",LEAP_LOG_KEY, convertedMvlExp);

		}
		else if(operatorEntry.value.equalsIgnoreCase(">=")){
			convertedMvlExp=SQLToMvelFunctionHandler.GREATEREQFuncHandler(IndCond);
			logger.debug("{} convertedMvlExp value  - {}",LEAP_LOG_KEY, convertedMvlExp);

		}else if(operatorEntry.value.equalsIgnoreCase("<")){
			convertedMvlExp=SQLToMvelFunctionHandler.LESSERTHANFuncHandler(IndCond);
			logger.debug("{} convertedMvlExp value  - {}",LEAP_LOG_KEY, convertedMvlExp);

		}else if(operatorEntry.value.equalsIgnoreCase("<=")){
			convertedMvlExp=SQLToMvelFunctionHandler.LESSERTHANEQFuncHandler(IndCond);
			logger.debug("{} convertedMvlExp value - {}",LEAP_LOG_KEY, convertedMvlExp);

		}
		
		else if(operatorEntry.value.equalsIgnoreCase("LIKE")){
			convertedMvlExp=SQLToMvelFunctionHandler.LESSERTHANEQFuncHandler(IndCond);
			logger.debug("{} convertedMvlExp value - {}",LEAP_LOG_KEY, convertedMvlExp);

		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return convertedMvlExp;
	}

}
 