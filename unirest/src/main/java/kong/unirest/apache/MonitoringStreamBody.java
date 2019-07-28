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

import kong.unirest.ProgressMonitor;
import kong.unirest.UnirestException;
import org.apache.hc.client5.http.entity.mime.InputStreamBody;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

class MonitoringStreamBody extends InputStreamBody {
    private final ProgressMonitor monitor;
    private final long length;
    private final String name;
    private final String fileName;

    public MonitoringStreamBody(InputStream in,
                                ContentType contentType,
                                String fileName,
                                String fieldName,
                                ProgressMonitor monitor) {
        super(in, contentType, fileName);
        this.fileName = fileName;
        try {
            this.monitor = monitor;
            this.name = fieldName;
            this.length = in.available();
        }catch (IOException e){
            throw new UnirestException(e);
        }
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        if(Objects.nonNull(monitor)){
            super.writeTo(new MonitoringStream(out, length, name, fileName, monitor));
        } else {
            super.writeTo(out);
        }
    }
}
