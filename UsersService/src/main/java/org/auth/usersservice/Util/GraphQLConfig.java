//package org.auth.usersservice.Util;
//
//import graphql.kickstart.execution.context.GraphQLContext;
//import graphql.kickstart.servlet.context.DefaultGraphQLServletContext;
//import graphql.kickstart.servlet.context.DefaultGraphQLServletContextBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.server.ServerWebExchange;
//
//@Configuration
//public class GraphQLConfig {
//
//	@Bean
//	public DefaultGraphQLServletContextBuilder graphQLContextBuilder() {
//		return new DefaultGraphQLServletContextBuilder() {
//			@Override
//			public GraphQLContext build(ServerWebExchange exchange) {
//				return DefaultGraphQLServletContext.createServletContext()
//						.attribute("serverWebExchange", exchange)
//						.build();
//			}
//		};
//	}
//
//
//}