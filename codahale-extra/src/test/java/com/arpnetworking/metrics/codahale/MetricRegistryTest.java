/**
 * Copyright 2015 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arpnetworking.metrics.codahale;

import com.arpnetworking.metrics.Metrics;
import com.arpnetworking.metrics.MetricsFactory;
import com.arpnetworking.metrics.impl.TsdMetricsFactory;
import com.codahale.metrics.Gauge;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Tests for the MetricRegistry.
 *
 * @author Brandon Arp (barp at groupon dot com)
 */
public class MetricRegistryTest {

    private AutoCloseable _mocks;

    @Before
    public void setUp() {
        _mocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        if (_mocks != null) {
            _mocks.close();
        }
    }

    @Test
    public void getMetricsfactory() {
        final MetricRegistry metricRegistry = new MetricRegistry(Mockito.mock(MetricsFactory.class));
        final MetricsFactory metricsFactory = metricRegistry.getMetricsFactory();
        Assert.assertFalse(metricsFactory instanceof TsdMetricsFactory);
    }

    @Test
    public void getDefaultMetricsfactory() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final MetricsFactory metricsFactory = metricRegistry.getMetricsFactory();
        MatcherAssert.assertThat(metricsFactory, Matchers.instanceOf(TsdMetricsFactory.class));
    }

    @Test
    public void counter() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final com.codahale.metrics.Counter counter = metricRegistry.counter("foo");
        MatcherAssert.assertThat(counter, CoreMatchers.instanceOf(Counter.class));
    }

    @Test
    public void multipleCounter() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final com.codahale.metrics.Counter counter = metricRegistry.counter("foo");
        MatcherAssert.assertThat(counter, CoreMatchers.instanceOf(Counter.class));
        final com.codahale.metrics.Counter counter2 = metricRegistry.counter("foo");
        MatcherAssert.assertThat(counter2, CoreMatchers.instanceOf(Counter.class));
    }

    @Test
    public void timer() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final com.codahale.metrics.Timer timer = metricRegistry.timer("foo");
        MatcherAssert.assertThat(timer, CoreMatchers.instanceOf(Timer.class));
    }

    @Test
    public void multipleTimer() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final com.codahale.metrics.Timer timer = metricRegistry.timer("foo");
        MatcherAssert.assertThat(timer, CoreMatchers.instanceOf(Timer.class));
        final com.codahale.metrics.Timer timer2 = metricRegistry.timer("foo");
        MatcherAssert.assertThat(timer2, CoreMatchers.instanceOf(Timer.class));
    }

    @Test
    public void meter() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final com.codahale.metrics.Meter meter = metricRegistry.meter("foo");
        MatcherAssert.assertThat(meter, CoreMatchers.instanceOf(Meter.class));
    }

    @Test
    public void multipleMeter() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final com.codahale.metrics.Meter meter = metricRegistry.meter("foo");
        MatcherAssert.assertThat(meter, CoreMatchers.instanceOf(Meter.class));
        final com.codahale.metrics.Meter meter2 = metricRegistry.meter("foo");
        MatcherAssert.assertThat(meter2, CoreMatchers.instanceOf(Meter.class));
    }

    @Test
    public void histogram() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final com.codahale.metrics.Histogram histogram = metricRegistry.histogram("foo");
        MatcherAssert.assertThat(histogram, CoreMatchers.instanceOf(Histogram.class));
    }

    @Test
    public void multipleHistogram() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final com.codahale.metrics.Histogram histogram = metricRegistry.histogram("foo");
        MatcherAssert.assertThat(histogram, CoreMatchers.instanceOf(Histogram.class));
        final com.codahale.metrics.Histogram histogram2 = metricRegistry.histogram("foo");
        MatcherAssert.assertThat(histogram2, CoreMatchers.instanceOf(Histogram.class));
    }

    @Test
    public void closer() {
        final MetricRegistry registry = new MetricRegistry();
        @SuppressWarnings("unchecked")
        final Gauge<Integer> gauge = (Gauge<Integer>) (Gauge<?>) Mockito.mock(Gauge.class);
        registry.register("some_gauge", gauge);
        Mockito.when(gauge.getValue()).thenReturn(15);
        final MetricRegistry.Closer closer = new MetricRegistry.Closer(_lock, _factory, _reference, registry);
        final Metrics original = Mockito.mock(Metrics.class);
        final Metrics after = Mockito.mock(Metrics.class);
        _reference.set(original);

        Mockito.when(_factory.create()).thenReturn(after);

        closer.run();
        Mockito.verifyNoInteractions(_factory);
        Mockito.verify(_lock).writeLocked(_callback.capture());
        Assert.assertSame(original, _reference.get());
        _callback.getValue().accept(original);

        Mockito.verify(original).setGauge("some_gauge", 15d);
        Mockito.verify(original).close();
        Assert.assertSame(after, _reference.get());
        Mockito.verifyNoInteractions(after);
    }

    @Test
    public void gaugeString() {
        final MetricRegistry registry = new MetricRegistry();
        @SuppressWarnings("unchecked")
        final Gauge<String> gauge = (Gauge<String>) (Gauge<?>) Mockito.mock(Gauge.class);
        registry.register("some_gauge", gauge);
        Mockito.when(gauge.getValue()).thenReturn("not a number");
        final MetricRegistry.Closer closer = new MetricRegistry.Closer(_lock, _factory, _reference, registry);
        final Metrics original = Mockito.mock(Metrics.class);
        final Metrics after = Mockito.mock(Metrics.class);
        _reference.set(original);

        Mockito.when(_factory.create()).thenReturn(after);

        closer.run();
        Mockito.verifyNoInteractions(_factory);
        Mockito.verify(_lock).writeLocked(_callback.capture());
        Assert.assertSame(original, _reference.get());
        _callback.getValue().accept(original);

        Mockito.verify(original).close();
        Assert.assertSame(after, _reference.get());
        Mockito.verifyNoInteractions(after);
    }

    @Test
    public void gaugeThrows() {
        final MetricRegistry registry = new MetricRegistry();
        @SuppressWarnings("unchecked")
        final Gauge<Integer> gauge = (Gauge<Integer>) (Gauge<?>) Mockito.mock(Gauge.class);
        registry.register("some_gauge", gauge);
        Mockito.when(gauge.getValue()).thenThrow(new IllegalStateException());
        final MetricRegistry.Closer closer = new MetricRegistry.Closer(_lock, _factory, _reference, registry);
        final Metrics original = Mockito.mock(Metrics.class);
        final Metrics after = Mockito.mock(Metrics.class);
        _reference.set(original);

        Mockito.when(_factory.create()).thenReturn(after);

        closer.run();
        Mockito.verifyNoInteractions(_factory);
        Mockito.verify(_lock).writeLocked(_callback.capture());
        Assert.assertSame(original, _reference.get());
        _callback.getValue().accept(original);

        Mockito.verify(original).close();
        Assert.assertSame(after, _reference.get());
        Mockito.verifyNoInteractions(after);
    }

    @Mock
    private SafeRefLock<Metrics> _lock;
    @Mock
    private MetricsFactory _factory;
    private AtomicReference<Metrics> _reference = new AtomicReference<>();
    @Captor
    private ArgumentCaptor<Consumer<Metrics>> _callback;
}
