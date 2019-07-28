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

import com.google.common.collect.Sets;
import kong.unirest.Config;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import kong.unirest.apache.AsyncIdleConnectionMonitorThread;
import kong.unirest.apache.SyncIdleConnectionMonitorThread;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.core5.reactor.IOReactorStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LifeCycleTest extends BddTest {

    @Mock
    private CloseableHttpClient httpc;
    @Mock
    private PoolingHttpClientConnectionManager clientManager;
    @Mock
    private SyncIdleConnectionMonitorThread connMonitor;
    @Mock
    private CloseableHttpAsyncClient asyncClient;
    @Mock
    private AsyncIdleConnectionMonitorThread asyncMonitor;
    @Mock
    private PoolingAsyncClientConnectionManager manager;

    @Override
    public void setUp() {
        super.setUp();
        clearUnirestHooks();
    }


    @Test
    public void ifClientsAreAlreadyRunningCanAddShutdownHooks() throws Exception  {
        assertShutdownHooks(0);

        Unirest.get(MockServer.GET).asEmpty();
        Unirest.get(MockServer.GET).asEmptyAsync();
        Unirest.config().addShutdownHook(true);
        Unirest.config().addShutdownHook(true);

        assertShutdownHooks(1);
    }

    @Test
    public void canAddShutdownHooks() throws Exception {
        assertShutdownHooks(0);

        Unirest.config().addShutdownHook(true).getClient();
        Unirest.config().addShutdownHook(true).getAsyncClient();

        assertShutdownHooks(1);
    }

    @Test
    public void settingClientAfterClientHasAlreadyBeenSet() {
        //HttpClientMock httpClientMock = new HttpClientMock();
        //httpClientMock.onGet("http://localhost/getme").doReturn(202, "Howdy Ho!");
        assertEquals(200, Unirest.get(MockServer.GET).asString().getStatus());
        //Unirest.config().httpClient(httpClientMock);
        HttpResponse<String> result =  Unirest.get("http://localhost/getme").asString();
        assertEquals(202, result.getStatus());
        assertEquals("Howdy Ho!", result.getBody());
    }

    @Test
    public void willNotShutdownInactiveAsyncClient() throws IOException {
        CloseableHttpAsyncClient asyncClient = mock(CloseableHttpAsyncClient.class);
        when(asyncClient.getStatus()).thenReturn(IOReactorStatus.ACTIVE);

        //Unirest.config().asyncClient(asyncClient);

        Unirest.shutDown();

        verify(asyncClient, never()).close();
    }

    @Test
    public void canDetectIfSystemIsRunning() {
        Unirest.get(MockServer.GET).asEmpty();
        assertTrue(Unirest.isRunning());

        Unirest.shutDown();
        assertFalse(Unirest.isRunning());

        Unirest.get(MockServer.GET).asEmpty();
        assertTrue(Unirest.isRunning());
    }

    @Test
    public void willReinitIfLibraryIsUsedAfterShutdown() {
        Unirest.shutDown();
        assertFalse(Unirest.isRunning());

        Unirest.get(MockServer.GET).asEmpty();
        assertTrue(Unirest.isRunning());
    }

    @Test
    public void canGetTheCommonInstanceOfUnirest(){
        assertSame(Unirest.primaryInstance(), Unirest.primaryInstance());
        assertNotSame(Unirest.primaryInstance(), Unirest.spawnInstance());
        assertNotSame(Unirest.spawnInstance(), Unirest.spawnInstance());
    }

    @Test
    public void shouldReuseThreadPool() {
        int startingCount = ManagementFactory.getThreadMXBean().getThreadCount();
        IntStream.range(0,100).forEach(i -> {
            Unirest.config().reset().getClient();
            Unirest.config().getAsyncClient();
        });
        assertThat(ManagementFactory.getThreadMXBean().getThreadCount(), is(lessThan(startingCount + 10)));
    }

    @Test
    public void testUnirestInstanceIsShutdownWhenClosed() {
        UnirestInstance reference;
        try (UnirestInstance instance = new UnirestInstance(new Config().setDefaultHeader("foo", "bar"))) {
            reference = instance;
            assertEquals(1, reference.config().getDefaultHeaders().size());
            assertEquals("bar", reference.config().getDefaultHeaders().get("foo").get(0));
        }
        assertEquals(0, reference.config().getDefaultHeaders().size());
    }

    private void assertShutdownHooks(int expected) {
        Set<Thread> threads = getShutdownHookMap();

        assertEquals(expected, threads.stream().filter(t -> "Unirest Apache Client Shutdown Hook".equals(t.getName())).count());
        assertEquals(expected, threads.stream().filter(t -> "Unirest Apache Async Client Shutdown Hook".equals(t.getName())).count());
    }

    private void clearUnirestHooks() {
        getShutdownHookMap()
                .stream()
                .filter(t -> t.getName().contains("Unirest"))
                .forEach(t -> Runtime.getRuntime().removeShutdownHook(t));
    }

    private Set<Thread> getShutdownHookMap() {
        try {
            // oh this is so dirty and horrible. Set to @Ignore if it starts to be a problem.
            Class clazz = Class.forName("java.lang.ApplicationShutdownHooks");
            Field field = clazz.getDeclaredField("hooks");
            field.setAccessible(true);
            Set<Thread> threads = ((Map<Thread, Thread>) field.get(null)).keySet();
            return Sets.newHashSet(threads);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
