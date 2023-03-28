package com.leap.authorization.functional.testing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class OrderTestRunner extends BlockJUnit4ClassRunner {

	public OrderTestRunner(Class<?> clazz) throws InitializationError {
		super(clazz);
	}

	@Override
	protected List<FrameworkMethod> computeTestMethods() {
		List<FrameworkMethod> toSort = super.computeTestMethods();
		if (toSort.isEmpty())
			return toSort;

		final Map<Integer, FrameworkMethod> testMethods = new TreeMap<>();

		Class<?> clazz = getDeclaringClass(toSort);
		if (clazz == null) {
			System.err.println("OrderedTestRunner can only run test classes that"
					+ " don't have test methods inherited from superclasses");
			return Collections.emptyList();
		}
		ClassPool pool = ClassPool.getDefault();
		try {
			CtClass cc = pool.get(clazz.getName());
			for (FrameworkMethod m : toSort) {
				String methodName = m.getName();
				CtMethod method = cc.getDeclaredMethod(methodName);
				testMethods.put(method.getMethodInfo().getLineNumber(0), m);
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		return new ArrayList<>(testMethods.values());
	}

	private Class<?> getDeclaringClass(List<FrameworkMethod> methods) {
		Class<?> clazz = methods.get(0).getMethod().getDeclaringClass();
		for (int iterator = 1; iterator < methods.size(); iterator++) {
			if (!methods.get(iterator).getMethod().getDeclaringClass().equals(clazz)) {
				return null;
			}
		}
		return clazz;
	}
}