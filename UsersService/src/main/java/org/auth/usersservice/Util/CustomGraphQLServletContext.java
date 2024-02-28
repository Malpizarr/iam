//package org.auth.usersservice.Util;
//
//import graphql.execution.instrumentation.Instrumentation;
//import graphql.kickstart.execution.context.GraphQLContext;
//import graphql.schema.DataFetchingEnvironment;
//import org.dataloader.DataLoaderRegistry;
//import org.springframework.web.server.ServerWebExchange;
//
//import javax.security.auth.Subject;
//import java.util.Map;
//import java.util.Optional;
//
//public class CustomGraphQLServletContext implements GraphQLContext {
//	private ServerWebExchange serverWebExchange;
//
//	public CustomGraphQLServletContext(ServerWebExchange serverWebExchange) {
//		this.serverWebExchange = serverWebExchange;
//	}
//
//	@Override
//	public <T> T get(String key) {
//		return (T) serverWebExchange.getAttribute(key);
//	}
//
//	@Override
//	public void put(String key, Object value) {
//		serverWebExchange.getAttributes().put(key, value);
//	}
//
//	@Override
//	public Map<String, Object> getAttributes() {
//		return serverWebExchange.getAttributes();
//	}
//
//	@Override
//	public Instrumentation getInstrumentation() {
//		return null;
//	}
//
//	@Override
//	public DataFetchingEnvironment getEnvironment() {
//		return null;
//	}
//
//	public ServerWebExchange getServerWebExchange() {
//		return serverWebExchange;
//	}
//
//	@Override
//	public Optional<Subject> getSubject() {
//		return Optional.empty();
//	}
//
//	@Override
//	public @lombok.NonNull DataLoaderRegistry getDataLoaderRegistry() {
//		return null;
//	}
//}
