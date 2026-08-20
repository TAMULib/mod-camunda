package org.folio.rest.camunda.config;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.folio.rest.camunda.client.FolioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ReactorResourceFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.resources.LoopResources;

@Configuration
public class WebClientConfig {

  @Bean
  NioEventLoopGroup nioEventLoopGroup() {
    return new NioEventLoopGroup(128);
  }

  @Bean
  ConnectionProvider connectionProvider() {
    return ConnectionProvider.builder("camunda-web-client-thread-pool")
      .maxConnections(100)
      .build();
  }

  @Bean
  ReactorResourceFactory reactorResourceFactory(NioEventLoopGroup group, ConnectionProvider provider) {
    ReactorResourceFactory factory = new ReactorResourceFactory();
    factory.setLoopResources(new LoopResources() {
      @Override
      public EventLoopGroup onServer(boolean b) {
        return group;
      }
    });
    factory.setUseGlobalResources(false);
    factory.setConnectionProvider(provider);
    return factory;
  }

  @Bean
  ReactorClientHttpConnector reactorClientHttpConnector(ReactorResourceFactory factory) {
    return new ReactorClientHttpConnector(factory, connection -> connection);
  }

  @Bean
  WebClient webClient(WebClient.Builder builder, ReactorClientHttpConnector connector) {
    return builder.clientConnector(connector).build();
  }

  @Bean
  FolioClient userClient() {
    RestClient restClient = RestClient.builder()
      .baseUrl("https://api.example.com")
      .build();

    HttpServiceProxyFactory factory = HttpServiceProxyFactory
      .builderFor(RestClientAdapter.create(restClient))
      .build();

    return factory.createClient(FolioClient.class);
  }

}
