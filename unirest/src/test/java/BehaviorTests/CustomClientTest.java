/**
 * The MIT License
 *
 * Copyright for portions of unirest-java are held by Kong Inc (c) 2013.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package BehaviorTests;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.apache.ApacheAsyncClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CustomClientTest extends BddTest {

    private final String url = "http://localhost/getme";
    boolean requestConfigUsed = false;


    @Override
    @After
    public void tearDown() {
        super.tearDown();
        requestConfigUsed = false;


    }

    @Test
    public void settingACustomClient() {
      //  HttpClientMock client = getMockClient();

       // Unirest.config().httpClient(client);

        assertMockResult();
    }

    @Test
    public void settingACustomClientWithBuilder() {
      //  HttpClientMock client = getMockClient();

//        Unirest.config().httpClient(ApacheClient.builder(client)
//                .withRequestConfig((c, w) -> {
//                    requestConfigUsed = true;
//                    return RequestConfig.custom().build();
//                }));

        assertMockResult();

        Unirest.config().reset();

        assertMockResult();
    }

    @Test
    public void canSetACustomAsyncClientWithBuilder() throws Exception {
        try(CloseableHttpAsyncClient client = HttpAsyncClientBuilder.create().build()) {
            client.start();

            Unirest.config().asyncClient(ApacheAsyncClient.builder(client)
                    .withRequestConfig((c, w) -> {
                        requestConfigUsed = true;
                        return RequestConfig.custom().build();
                    })
            );

            assertAsyncResult();
            assertTrue(requestConfigUsed);
        }
    }

    private void assertAsyncResult() throws Exception {
        MockServer.setStringResponse("Howdy Ho!");
        HttpResponse<String> result =  Unirest.get(MockServer.GET).asStringAsync().get();
        assertEquals(200, result.getStatus());
        assertEquals("Howdy Ho!", result.getBody());
    }


    private void assertMockResult() {
        HttpResponse<String> result =  Unirest.get(url).asString();
        assertEquals(202, result.getStatus());
        assertEquals("Howdy Ho!", result.getBody());
    }
}
