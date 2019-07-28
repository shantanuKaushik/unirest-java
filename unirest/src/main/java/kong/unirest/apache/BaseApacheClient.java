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

package kong.unirest.apache;

import kong.unirest.*;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;

import java.util.function.Function;

abstract class BaseApacheClient {

    protected RequestConfigFactory configFactory = new DefaultFactory();


    protected CredentialsProvider toApacheCreds(Proxy proxy) {
        if(proxy != null && proxy.isAuthenticated()) {
            BasicCredentialsProvider proxyCreds = new BasicCredentialsProvider();
            proxyCreds.setCredentials(new AuthScope(proxy.getHost(), proxy.getPort()),
                    new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword().toCharArray()));
            return proxyCreds;
        }
        return null;
    }

    protected <T> HttpResponse<T> transformBody(Function<RawResponse, HttpResponse<T>> transformer, RawResponse rr) {
        try {
            return transformer.apply(rr);
        }catch (RuntimeException e){
            String originalBody = recoverBody(rr);
            return new BasicResponse(rr, originalBody, e);
        }
    }

    private String recoverBody(RawResponse rr){
        try {
            return rr.getContentAsString();
        }catch (Exception e){
            return null;
        }
    }

    public void setConfigFactory(RequestConfigFactory configFactory) {
        this.configFactory = configFactory;
    }
}
