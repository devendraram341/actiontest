package com.attunedlabs.eventsubscription.defaultretrystrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RetryPolicyConfiguration {

	int defaultRetryIntervalMultiplier() default 2;

	int defaultRetryInterval() default 1;

	int defaultMaximumRetryInterval() default 60;

	String defaultTimeIntervalUnit() default "MINUTES";

	int defaultRetryCount() default -1;

	int defaultRetryRecordsCount() default 15;

}
