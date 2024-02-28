//package org.auth.authservice.Util;
//
//import graphql.ExecutionResult;
//import graphql.execution.instrumentation.InstrumentationContext;
//import graphql.execution.instrumentation.SimpleInstrumentation;
//import graphql.execution.instrumentation.parameters.InstrumentationExecuteOperationParameters;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//import reactor.util.context.ContextView;
//
//@Component
//public class ServerWebExchangeInstrumentation extends SimpleInstrumentation {
//
//	@Override
//	public InstrumentationContext<ExecutionResult> beginExecuteOperation(InstrumentationExecuteOperationParameters parameters) {
//		var executionInput = parameters.getExecutionInput();
//		var context = executionInput.getContext();
//
//		if (context instanceof ContextView) {
//			var serverWebExchange = ((ContextView) context).get(ServerWebExchange.class);
//			if (serverWebExchange != null) {
//				var newContext = executionInput.getGraphQLContext().put("serverWebExchange", serverWebExchange);
//				var newExecutionInput = executionInput.transform(builder -> builder.graphQLContext(newContext));
//				parameters = parameters.transform(builder -> builder.executionInput(newExecutionInput));
//			}
//		}
//
//		return super.beginExecuteOperation(parameters);
//	}
//}
