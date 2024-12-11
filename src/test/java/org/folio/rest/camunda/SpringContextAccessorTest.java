package org.folio.rest.camunda;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.folio.spring.tenant.properties.TenantProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
  classes = {
    SpringContextAccessor.class,
    TenantProperties.class
  },
  webEnvironment = WebEnvironment.MOCK
)
class SpringContextAccessorTest {

  @ParameterizedTest
  @ValueSource(classes = {
      TenantProperties.class
  })
  void testGetTenantPropertiesBean(Class<?> bean) {
    assertNotNull(SpringContextAccessor.getBean(bean));
  }

}
