package com.bugsnag;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.bugsnag.callbacks.Callback;
import com.bugsnag.delivery.Delivery;
import com.bugsnag.serialization.Serializer;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ExceptionTest {

    private Exception exception;
    private RuntimeException ogThrowable;

    /**
     * Initialises a config
     *
     * @throws Throwable if config couldn't be initialised
     */
    @Before
    public void setUp() throws Throwable {
        Configuration config = new Configuration("api-key");
        ogThrowable = new RuntimeException("Test");
        exception = new Exception(config, ogThrowable);
    }

    @Test
    public void testDefaults() throws Throwable {
        assertEquals("java.lang.RuntimeException", exception.getErrorClass());
        assertEquals("Test", exception.getMessage());
        assertEquals(ogThrowable, exception.getThrowable());
        assertFalse(exception.getStacktrace().isEmpty());
    }

    @Test
    public void testClassOverride() throws Throwable {
        exception.setErrorClass("Hello");
        assertEquals("Hello", exception.getErrorClass());
        assertEquals("Test", exception.getMessage());
    }

    @Test
    public void testReportCallback() throws Throwable {
        Bugsnag bugsnag = new Bugsnag("apikey");
        bugsnag.setDelivery(new Delivery() {
            @Override
            public void deliver(Serializer serializer, Object object) {
            }

            @Override
            public void close() {
            }
        });
        assertTrue(bugsnag.notify(ogThrowable, new Callback() {
            @Override
            public void beforeNotify(Report report) {
                try {
                    assertEquals(ogThrowable, report.getException());
                    assertEquals("Test", report.getExceptionMessage());
                    assertEquals("java.lang.RuntimeException", report.getExceptionName());

                    report.setExceptionName("Foo");
                    assertEquals("Foo", report.getExceptionName());


                    List<Exception> exceptions = report.getExceptions();
                    assertEquals(1, exceptions.size());

                    Exception exception = exceptions.get(0);
                    assertNotNull(exception);
                    assertEquals("Foo", exception.getErrorClass());
                    assertEquals("Test", exception.getMessage());
                } catch (Throwable throwable) {
                    report.cancel();
                }
            }
        }));
    }

}
