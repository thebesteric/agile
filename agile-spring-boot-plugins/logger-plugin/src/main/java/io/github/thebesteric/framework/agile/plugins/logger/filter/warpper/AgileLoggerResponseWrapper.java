package io.github.thebesteric.framework.agile.plugins.logger.filter.warpper;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * AgileLoggerResponseWrapper
 *
 * @author Eric Joe
 * @since 1.0
 */
public class AgileLoggerResponseWrapper extends HttpServletResponseWrapper {

    private final ByteArrayOutputStream buffer;
    private final ServletOutputStream out;

    @Getter
    @Setter
    private Throwable exception;

    public AgileLoggerResponseWrapper(HttpServletResponse response) {
        super(response);
        buffer = new ByteArrayOutputStream();
        out = new OutputStreamWrapper(buffer);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return out;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (out != null) out.flush();
    }

    public void setBuffer(String message) throws IOException {
        buffer.reset();
        buffer.write(message.getBytes());
    }

    public byte[] getByteArray() throws IOException {
        flushBuffer();
        return buffer.toByteArray();
    }

    private static class OutputStreamWrapper extends ServletOutputStream {

        private final ByteArrayOutputStream byteArrayOutputStream;

        public OutputStreamWrapper(ByteArrayOutputStream byteArrayOutputStream) {
            this.byteArrayOutputStream = byteArrayOutputStream;
        }

        @Override
        public void write(int b) throws IOException {
            byteArrayOutputStream.write(b);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }
    }
}
