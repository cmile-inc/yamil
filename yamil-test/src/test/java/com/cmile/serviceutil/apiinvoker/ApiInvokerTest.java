package com.cmile.serviceutil.apiinvoker;

import com.cmile.serviceutil.auth.jwt.ClientScope;
import com.cmile.serviceutil.auth.jwt.JwtTokenProvider;
import com.cmile.serviceutil.gcp.GCPServiceProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApiInvokerTest {

  @Mock
  private GCPServiceProject gcpServiceProject;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  private ApiInvoker apiInvoker;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    apiInvoker = new ApiInvoker(gcpServiceProject, jwtTokenProvider);
  }

  @Test
  void testInvoke_Success() throws Exception {
    when(gcpServiceProject.getDomain()).thenReturn("http://test-domain.com");
    when(gcpServiceProject.getApplicationName()).thenReturn("TestApp");
    when(gcpServiceProject.getDu()).thenReturn("test-du");
    when(jwtTokenProvider.createToken(
        eq(ClientScope.SERVICE),
        eq("TestApp"),
        any(),
        any(),
        any(),
        eq("test-du")))
        .thenReturn("mock-token");

    TestApiClient mockApiClient = mock(TestApiClient.class);
    doNothing().when(mockApiClient).setBasePath("http://test-domain.com");
    doNothing()
        .when(mockApiClient)
        .addDefaultHeader("Authorization", "Bearer mock-token");

    Callable<Mono<String>> mockApiMethod = mock(Callable.class);
    when(mockApiMethod.call()).thenReturn(Mono.just("Success"));

    String result = apiInvoker.invoke(mockApiClient, mockApiMethod);

    verify(mockApiClient).setBasePath("http://test-domain.com");
    verify(mockApiClient).addDefaultHeader("Authorization", "Bearer mock-token");
    assertEquals("Success", result);
  }

  @Test
  void testInvoke_Failure() throws Exception {
    when(gcpServiceProject.getDomain()).thenReturn("http://test-domain.com");
    when(jwtTokenProvider.createToken(
        eq(ClientScope.SERVICE),
        any(),
        any(),
        any(),
        any(),
        any()))
        .thenReturn("mock-token");

    TestApiClient mockApiClient = mock(TestApiClient.class);

    Callable<Mono<String>> mockApiMethod = mock(Callable.class);
    when(mockApiMethod.call()).thenThrow(new RuntimeException("API error"));

    Exception exception =
        assertThrows(
            RuntimeException.class,
            () -> apiInvoker.invoke(mockApiClient, mockApiMethod));

    assertEquals(
        "An error occurred while interacting with the API client. Error: API error",
        exception.getMessage());
  }

  static class TestApiClient {
    public void setBasePath(String basePath) {
    }

    public void addDefaultHeader(String key, String value) {
    }
  }
}
