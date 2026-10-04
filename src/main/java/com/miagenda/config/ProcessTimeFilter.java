package com.miagenda.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ProcessTimeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.nanoTime();
        HttpServletResponseWrapper timed = new HttpServletResponseWrapper(response) {
            private void stamp() {
                if (!isCommitted()) {
                    setHeader("X-Process-Time", elapsed(start));
                }
            }

            @Override
            public void sendError(int status) throws IOException {
                stamp();
                super.sendError(status);
            }

            @Override
            public void sendError(int status, String message) throws IOException {
                stamp();
                super.sendError(status, message);
            }

            @Override
            public void sendRedirect(String location) throws IOException {
                stamp();
                super.sendRedirect(location);
            }

            @Override
            public void flushBuffer() throws IOException {
                stamp();
                super.flushBuffer();
            }

            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                stamp();
                return super.getOutputStream();
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                stamp();
                return super.getWriter();
            }
        };

        filterChain.doFilter(request, timed);
        if (!timed.isCommitted()) {
            timed.setHeader("X-Process-Time", elapsed(start));
        }
    }

    private static String elapsed(long startNanos) {
        double seconds = (System.nanoTime() - startNanos) / 1_000_000_000d;
        return String.format(Locale.US, "%.4f s", seconds);
    }
}
