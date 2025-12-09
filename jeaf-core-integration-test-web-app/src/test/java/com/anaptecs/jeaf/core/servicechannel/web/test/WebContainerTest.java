/**
 * Copyright 2004 - 2022 anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 *
 * All rights reserved.
 */
package com.anaptecs.jeaf.core.servicechannel.web.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.anaptecs.jeaf.fastlane.impl.FastLaneServer;
import com.anaptecs.jeaf.fastlane.impl.WebContainerState;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebContainerTest {
  @Test
  public void testWebContainer( ) throws Exception {
    FastLaneServer lServer = null;
    try {
      // Create new JEAF Fast Lane Server and start it
      lServer = new FastLaneServer();

      // Start server.
      lServer.start();
      assertEquals(WebContainerState.RUNNING, lServer.getState());

      // Create new http client
      OkHttpClient lClientBuilder = new OkHttpClient();
      OkHttpClient lHttpClient = lClientBuilder.newBuilder().build();

      // Execute simple GET request to our REST service
      Request lRequest = new Request.Builder().url("http://localhost:8090/rest/products").get().build();
      Response lResponse = lHttpClient.newCall(lRequest).execute();
      assertEquals(200, lResponse.code());
      assertEquals(
          "[{\"productID\":\"c513b71f-433d-4118-be8b-7190226eb155\",\"uri\":\"https://products.anaptecs.de/123456789\"}]",
          lResponse.body().string());

      // Send empty post request
      MediaType lMediaType = MediaType.parse("application/xml; charset=utf-8");
      RequestBody lBody = RequestBody.create("", lMediaType);
      lRequest = new Request.Builder().url("http://localhost:8090/rest/products/ChannelCode").post(lBody).build();
      lResponse = lHttpClient.newCall(lRequest).execute();
      assertEquals(500, lResponse.code());

      // Shutdown server again.
      lServer.shutdown(0);
      lServer = null;
    }
    finally {
      if (lServer != null) {
        lServer.shutdown(-1);
      }
    }
  }
}
