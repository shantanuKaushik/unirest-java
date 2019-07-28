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

import kong.unirest.Config;
import kong.unirest.Headers;
import kong.unirest.RawResponseBase;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.http.HttpResponse;

import java.io.InputStream;
import java.io.InputStreamReader;

class ApacheAsyncResponse extends RawResponseBase {

    private final SimpleHttpResponse r;

    ApacheAsyncResponse(SimpleHttpResponse r, Config config) {
        super(r, config);
        this.r = r;
    }

    @Override
    public InputStream getContent() {
        return null;
    }

    @Override
    public byte[] getContentAsBytes() {
        return new byte[0];
    }

    @Override
    public String getContentAsString() {
        return null;
    }

    @Override
    public String getContentAsString(String charset) {
        return null;
    }

    @Override
    public InputStreamReader getContentReader() {
        return null;
    }

    @Override
    public boolean hasContent() {
        return false;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getEncoding() {
        return null;
    }
}
