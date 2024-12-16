/*
 * Copyright 2024 cmile inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cmile.serviceutil.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingAspect {

  // Static final variables for log layer types
  private static final String CONTROLLER_LAYER = "Controller";
  private static final String SERVICE_LAYER = "Service";
  private static final String REPOSITORY_LAYER = "Repository";

  // Static final strings for log messages
  private static final String ENTERED = "Entered";
  private static final String EXCEPTION_IN = "Exception in";
  private static final String EXITED = "Exited";
  private static final String METHOD = "Method:";
  private static final String WITH_ARGUMENTS = "with arguments:";
  private static final String WITH_RESULT = "with result:";
  private static final String EXCEPTION = "Exception:";

  public LoggingAspect() {}

  // Pointcut for all controller methods
  @Pointcut("execution(* com.cmile..controllers..*(..))")
  public void controllerLayer() {}

  // Pointcut for all service methods
  @Pointcut("execution(* com.cmile..services..*(..))")
  public void serviceLayer() {}

  // Pointcut for all repository methods
  @Pointcut("execution(* com.cmile..repositories..*(..))")
  public void repositoryLayer() {}

  // Combined pointcut for application flow (controller -> service -> repository)
  @Pointcut("controllerLayer() || serviceLayer() || repositoryLayer()")
  public void applicationFlow() {}

  // Around advice for controller layer
  @Around("controllerLayer()")
  public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
    return logMethodExecution(joinPoint, CONTROLLER_LAYER);
  }

  // Around advice for service layer
  @Around("serviceLayer()")
  public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
    return logMethodExecution(joinPoint, SERVICE_LAYER);
  }

  // Around advice for repository layer
  @Around("repositoryLayer()")
  public Object logRepositoryMethods(ProceedingJoinPoint joinPoint) throws Throwable {
    return logMethodExecution(joinPoint, REPOSITORY_LAYER);
  }

  // Common method to handle logging
  private Object logMethodExecution(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
    CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();

    String className = methodSignature.getDeclaringTypeName();
    String methodName = methodSignature.getName();
    Object[] methodArgs = joinPoint.getArgs();
    String[] methodParamNames = codeSignature.getParameterNames();

    Logger logger = LoggerFactory.getLogger(className);

    // Log method entry
    if (logger.isDebugEnabled()) {
      logger.debug(buildEntryMessage(layer, className, methodName, methodParamNames, methodArgs));
    }

    try {
      Object result = joinPoint.proceed();

      // Log method exit
      if (logger.isDebugEnabled()) {
        logger.debug(buildExitMessage(layer, className, methodName, result));
      }

      return result;
    } catch (Throwable throwable) {
      // Log exception
      if (logger.isErrorEnabled()) {
        logger.error(buildExceptionMessage(layer, className, methodName, throwable));
      }
      throw throwable;
    }
  }

  private String buildEntryMessage(
      String layer, String className, String methodName, String[] params, Object[] args) {
    StringJoiner message = new StringJoiner(" ");
    message.add(ENTERED).add(layer + " " + METHOD).add(className + "." + methodName + "()");

    if (params != null && args != null && params.length == args.length) {
      Map<String, Object> paramMap = new HashMap<>();
      for (int i = 0; i < params.length; i++) {
        paramMap.put(params[i], args[i]);
      }
      message.add(WITH_ARGUMENTS).add(paramMap.toString());
    }

    return message.toString();
  }

  private String buildExitMessage(
      String layer, String className, String methodName, Object result) {
    return new StringJoiner(" ")
        .add(EXITED)
        .add(layer + " " + METHOD)
        .add(className + "." + methodName + "()")
        .add(WITH_RESULT)
        .add(Objects.toString(result, "null"))
        .toString();
  }

  private String buildExceptionMessage(
      String layer, String className, String methodName, Throwable throwable) {
    return new StringJoiner(" ")
        .add(EXCEPTION_IN)
        .add(layer + " " + METHOD)
        .add(className + "." + methodName + "()")
        .add(EXCEPTION)
        .add(throwable.getClass().getName() + ": " + throwable.getMessage())
        .toString();
  }
}
