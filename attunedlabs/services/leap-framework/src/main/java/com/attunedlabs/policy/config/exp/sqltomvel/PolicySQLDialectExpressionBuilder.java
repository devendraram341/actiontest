package com.attunedlabs.policy.config.exp.sqltomvel;
import static com.attunedlabs.leap.logging.LeapLoggingConstants.LEAP_LOG_KEY;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.attunedlabs.policy.config.PolicyEvaluationConfigurationUnit;
import com.attunedlabs.policy.jaxb.Eval;
import com.attunedlabs.policy.jaxb.Evaluation;
import com.attunedlabs.policy.jaxb.Expression;

public class PolicySQLDialectExpressionBuilder {
	final Logger logger = LoggerFactory.getLogger(PolicySQLDialectExpressionBuilder.class);
	private static final String[] searchList = { " or ", " OR ", " and ", " AND " };
	private static final String[] replacementList = { " || ", " || ", " && ", " && " };
	public static final String[] supportedSQLFunction = { "NOT IN", " IN ", " = ", " != ", "< >", "IS NOT NULL", "IS NULL", " < ", " > ",
			" <= ", " >= " };

	// Todo Need write Mvel Expression For " LIKE "," NOT LIKE "," BETWEEN "

	public PolicyEvaluationConfigurationUnit buildEvaluation(Evaluation eval) {
		String methodName = "buildEvaluation";
		logger.debug("{} entered into the method {}, Dialect={}, Salience={} ", LEAP_LOG_KEY, methodName,eval.getEvalDialect().value(),eval.getSalience());
		PolicyEvaluationConfigurationUnit policyEvalUnit = new PolicyEvaluationConfigurationUnit();
		policyEvalUnit.setDialect(eval.getEvalDialect().value());
		policyEvalUnit.setSalience(eval.getSalience());

		Map<String, String> expMap = new LinkedHashMap();
		List<Expression> expressionList = eval.getExpression();
		// Convert all expression from SQL dialect to MVEL
		for (Expression exp : expressionList) {
			String expName = exp.getName().trim();
			PolicyCompiledExpression polExp = buildMvelExpression(exp);
			String mvelexpValue = polExp.getExpression();
			expMap.put(expName, mvelexpValue);
			policyEvalUnit.addPSVar(polExp.getPsVarList());
			policyEvalUnit.addReqVar(polExp.getReqVarList());
		}
		// Handle evaluate Expresiion
		String evalExpression = eval.getEvaluateExp();
		String mvelExp = evaluateExpression(evalExpression, expMap);

		logger.info("{} finsl mvleExp value {} " ,LEAP_LOG_KEY, mvelExp);
		policyEvalUnit.setExpression(mvelExp);
		
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return policyEvalUnit;
	}

	private String evaluateExpression(String evalExpression, Map<String, String> expMap) {
		Set<String> keySet = expMap.keySet();
		String methodName = "evaluateExpression";
		logger.debug("{} entered into the method {}, evalExpression={}, expMap={} ", LEAP_LOG_KEY, methodName,evalExpression,expMap);

		evalExpression = StringUtils.replaceEachRepeatedly(evalExpression, searchList, replacementList);

		logger.debug("{} after replaceing  AND  OR oprator expression is {}" ,LEAP_LOG_KEY, evalExpression);
		for (String expName : keySet) {
			String mvelExp = expMap.get(expName);
			logger.debug("{} mvelExp  value before evaluting Expression {} " ,LEAP_LOG_KEY, mvelExp);
			logger.trace("{} expName  value before evaluting Expression {} " ,LEAP_LOG_KEY, expName);

			evalExpression = StringUtils.replace(evalExpression, expName, "( " + mvelExp + " )");

			logger.debug("{} after evalatingExpression the expValue is {} ",LEAP_LOG_KEY, evalExpression);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return evalExpression;
	}

	
	private PolicyCompiledExpression buildMvelExpression(Expression exp) {
		String methodName = "buildMvelExpression";
		logger.debug("{} entered into the method {}, with ExpValue", LEAP_LOG_KEY, methodName,exp.getExpValue());
		String expValue = exp.getExpValue();
		String expName = exp.getName().trim();
		PolicyCompiledExpression polExp = new PolicyCompiledExpression();
		polExp.setExpName(expName);

		findRequestVariable(expValue, polExp);
		findPermaStoreVariable(expValue, polExp);
		expValue = replacePSVariable(expValue, polExp);
		String mvelexpValue = handleSQLToMvelConvertion(expValue, polExp);
		logger.trace("{} buildesMvelExpression Value {}" ,LEAP_LOG_KEY, mvelexpValue);
		polExp.setExpression(mvelexpValue);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return polExp;
	}

	private void findRequestVariable(String expValue, PolicyCompiledExpression polExp) {
		String methodName = "findRequestVariable";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);
		String regExPatternStr = "\\$\\w+";
		Pattern r = Pattern.compile(regExPatternStr);
		Matcher m = r.matcher(expValue);
		while (m.find()) {
			String varName = m.group(0);
			varName = varName.substring(1, varName.length());
			polExp.addReqVar(varName);
			logger.trace("Request variable " + varName);
		}
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
	}

	private String findPermaStoreVariable(String expValue, PolicyCompiledExpression polExp) {

		String methodName = "findPermaStoreVariable";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		String regExPatternStr = "::PS\\(([^)]+)\\)";
		Pattern r = Pattern.compile(regExPatternStr);
		Matcher m = r.matcher(expValue);
		while (m.find()) {
			polExp.addPSVar(m.group(1));

			logger.trace("{} PermaStoreVariable :{}  expressionValue :{} " ,LEAP_LOG_KEY, m.group(1) ,expValue);

		}
		logger.debug(" {} expressionValue  = {}" ,LEAP_LOG_KEY, expValue);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

		return expValue;
	}

	private String replacePSVariable(String expValue, PolicyCompiledExpression polExp) {

		String methodName = "replacePSVariable";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		List<String> psVarList = polExp.getPsVarList();
		if (psVarList == null)
			return expValue;
		for (String psVarName : psVarList) {
			expValue = StringUtils.replace(expValue, "::PS(" + psVarName + ")", "$" + psVarName);
		}
		logger.debug("{} replacePSVariable---Mvel Expression is NOW---> {} " ,LEAP_LOG_KEY, expValue);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return expValue;

	}

	private String handleSQLToMvelConvertion(String expValue, PolicyCompiledExpression polExp) {

		String methodName = "handleSQLToMvelConvertion";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		expValue = StringUtils.replaceEachRepeatedly(expValue, searchList, replacementList);

		logger.debug("{} expValue after replacing with java oprator  :{}" ,LEAP_LOG_KEY, expValue);
		expValue = expValue.replace("'", "\"");

		logger.debug("{} expValue after replacing with ' and \' {}  " ,LEAP_LOG_KEY, expValue);

		TotalExpressionHolder totalExpHolder = getIndividualExp(expValue, polExp);
		SQLToMvelExpressionConvertor mvelConvertor = new SQLToMvelExpressionConvertor();
		String finalMvelExp = mvelConvertor.handleExpConversion(totalExpHolder);

		logger.info("{} finalMvelExp values :{} " ,LEAP_LOG_KEY, finalMvelExp);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);

		return finalMvelExp;

	}

	private TotalExpressionHolder getIndividualExp(String expValue, PolicyCompiledExpression polExp) {
		TotalExpressionHolder toexpHolder = new TotalExpressionHolder();

		String methodName = "getIndividualExp";
		logger.debug("{} entered into the method {}", LEAP_LOG_KEY, methodName);

		// System.out.println("--TOBegin--"+expValue);
		// Regex Pattern that breaks the large Exp into smaller condition based on
		// && and || operator
		String regExPatternStr = "\\s&&\\s|\\s\\|\\|\\s";
		Pattern r = Pattern.compile(regExPatternStr);
		Matcher m = r.matcher(expValue);
		int lastindex = 0;
		while (m.find()) {
			String operator = m.group(0);
			int startIndex = m.start();
			int endIndex = m.end();
			String expSplitValue = expValue.substring(lastindex, startIndex);
			lastindex = endIndex;
			IndividualExpHolder operatorHolder = new IndividualExpHolder(operator, 2);
			IndividualExpHolder valueHolder = new IndividualExpHolder(expSplitValue, 1);
			toexpHolder.add(valueHolder);
			toexpHolder.add(operatorHolder);
			logger.trace("{} Expression are {}-{} string is -> {}" ,LEAP_LOG_KEY, startIndex, endIndex , expSplitValue);
		}
		// lastExp is left
		String expSplitValue = expValue.substring(lastindex, expValue.length());
		logger.trace("{}  lastExp is {} " ,LEAP_LOG_KEY, expSplitValue);
		IndividualExpHolder valueHolder = new IndividualExpHolder(expSplitValue, 1);
		toexpHolder.add(valueHolder);
		logger.trace("{} Expression are {}-{} string is -> {}" ,LEAP_LOG_KEY, lastindex ,expValue.length(), expSplitValue);
		logger.debug("{} exiting from the {}", LEAP_LOG_KEY, methodName);
		return toexpHolder;
	}

	public class TotalExpressionHolder {
		List<IndividualExpHolder> indExpHolderList = new ArrayList();

		public void add(IndividualExpHolder hoder) {
			indExpHolderList.add(hoder);
		}

		public List<IndividualExpHolder> getIndExpHolderList() {
			return indExpHolderList;
		}

		public void setIndExpHolderList(List<IndividualExpHolder> indExpHolderList) {
			this.indExpHolderList = indExpHolderList;
		}

		public String toString() {
			return "TotalExpressionHolder [IndExpHolderList=" + indExpHolderList + "]";
		}
	}

	// Will reprent $dstare in (::PS(GetStagingAreas)
	public class IndividualExpHolder {
		public String value;
		public int type; // 1-Expession 2-Operator
		List<IndividualExpEval> indExpEvalList = new ArrayList();

		public IndividualExpHolder(String value, int type) {
			this.value = value.trim();
			this.type = type;
			if (type == 2) {
				this.value = value.toUpperCase().trim();
			} else if (type == 1) {
				parseIndividualExp();
			}
		}

		public String getValue() {
			return value;
		}

		public int getType() {
			return type;
		}

		public List<IndividualExpEval> getIndExpEvalList() {
			return indExpEvalList;
		}

		// Todo Need change logic if supportedSQLFunction contian both exmple NOT
		// IN, IN or LIKE , NOT LIKE
		private void parseIndividualExp() {
			int lengthOfSupFunc = supportedSQLFunction.length;

			for (int i = 0; i < lengthOfSupFunc; i++) {
				logger.trace("{} Searching for Function= {}, value is :{}" ,LEAP_LOG_KEY, supportedSQLFunction[i] , value);

				String functionStr = supportedSQLFunction[i];

				if (value.contains(functionStr) || value.toLowerCase().contains(functionStr.toLowerCase())) {

					int startOfFunctionIndex = value.indexOf(functionStr);
					if (startOfFunctionIndex == -1) {
						startOfFunctionIndex = value.toLowerCase().indexOf(functionStr.toLowerCase());
					}
					int endOfFunctionIndex = startOfFunctionIndex + functionStr.length();
					String operator = functionStr;
					IndvidualExpEntry operatorEntry = new IndvidualExpEntry(operator, 1, 0);

					String leftValue = value.substring(0, startOfFunctionIndex);
					IndvidualExpEntry leftEntry = new IndvidualExpEntry(leftValue, getVarOrLitral(leftValue), 1);
					String rightValue = value.substring(endOfFunctionIndex, value.length());
					IndvidualExpEntry rightEntry = new IndvidualExpEntry(rightValue, getVarOrLitral(rightValue), 2);

					IndividualExpEval expVal = new IndividualExpEval(value);
					expVal.addExpEntry(leftEntry);
					expVal.addExpEntry(operatorEntry);
					expVal.addExpEntry(rightEntry);
					indExpEvalList.add(expVal);
					if (functionStr.equalsIgnoreCase("NOT IN")) {
						break;
					} else if (functionStr.equalsIgnoreCase("< >")) {
						break;
					}

				} else {
					logger.trace("Function Not Supported Function is=" + functionStr);
				}
			}
		}

		int getVarOrLitral(String value) {
			if (value.startsWith("$")) {
				return 3;
			} else if (value.startsWith("\"")) {
				return 2;
			}
			// error situation -1 error code
			return -1;
		}

		@Override
		public String toString() {
			return "IndividualExpHolder [value=" + value + ", type=" + type + ", indExpEvalList=" + indExpEvalList + "]";
		}

	}

	// $dstare in (::PS(GetStagingAreas)) with each as indiviualEntry as
	// combination on var,operator,literal
	public class IndividualExpEval {
		List<IndvidualExpEntry> indExpEntryList;
		String actualExpression;

		public IndividualExpEval(String exp) {
			this.actualExpression = actualExpression;
			this.indExpEntryList = new ArrayList();
			// buildIndividualEntry();
		}

		public void addExpEntry(IndvidualExpEntry indExpEntry) {
			if (indExpEntryList == null)
				indExpEntryList = new ArrayList();
			indExpEntryList.add(indExpEntry);
		}

		public IndvidualExpEntry getOperator() {
			for (IndvidualExpEntry expEntry : indExpEntryList) {
				if (expEntry.type == 1)
					return expEntry;
			}
			return null;
		}

		public IndvidualExpEntry getLeftEntry() {
			for (IndvidualExpEntry expEntry : indExpEntryList) {
				if (expEntry.type != 1 && expEntry.sideOfOperator == 1)
					return expEntry;
			}
			return null;
		}

		public IndvidualExpEntry getRightEntry() {
			for (IndvidualExpEntry expEntry : indExpEntryList) {
				if (expEntry.type != 1 && expEntry.sideOfOperator == 2)
					return expEntry;
			}
			return null;
		}

		@Override
		public String toString() {
			return "IndividualExpEval [indExpEntryList=" + indExpEntryList + ", actualExpression=" + actualExpression + "]";
		}

	}

	// $dstare variable or || operator or "Wood" literal
	public class IndvidualExpEntry {
		String value;
		int type;// 1=operator,2-literal,3=variable
		int sideOfOperator; // 1=left,2=right

		public IndvidualExpEntry(String value, int type, int sideOfOperator) {
			super();
			this.value = value.trim();
			this.type = type;
			this.sideOfOperator = sideOfOperator;
		}

		@Override
		public String toString() {
			return "IndvidualExpEntry [value=" + value + ", type=" + type + ", sideOfOperator=" + sideOfOperator + "]";
		}

	}

	public static void main(String[] args) {
		 String regex="$templateName = 'tempate1' OR $templateName = 'tempate2' OR $templateName = 'tempate3'";

		 Evaluation eval=new Evaluation(); eval.setSalience(1);
		 eval.setEvalDialect(Eval.valueOf("SQL")); Expression expression=new
		  Expression(); expression.setName("validStageArea");
		  expression.setExpValue(regex);
		  Expression expression1=new Expression();
		  expression1.setName("validPlatformArea");
		  expression1.setExpValue("$platform IS NULL");
		  
		  eval.getExpression().add(expression); //
		  //eval.getExpression().add(expression1);
		  
		  eval.setEvaluateExp(" validStageArea ");
		  
		  PolicyEvaluationConfigurationUnit policyEvaluationConfigurationUnit=
		  new PolicySQLDialectExpressionBuilder().buildEvaluation(eval);
		  System.out.println("PolicyEvaluation Configaration "+
		  policyEvaluationConfigurationUnit);
		  
		  Map<String, Object> map=new HashMap<String, Object>();
		  map.put("$templateName", "tempate1");
		
		  System.out.println("is True "+MVEL.eval((String)policyEvaluationConfigurationUnit.getExpression(), map));

		// to match word
		// String regex="(regexval)";

		// to not match words
		// to check empty =
		// String regex="^\\s*$";

		// to check range of values
		//String regex = "([1-2][0-9])+";

		// to check charcter matching
		

	}

	private static void checkDuplicateSql() {

	}
}
