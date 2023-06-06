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
import com.arpnetworking.metrics.Units;
import com.codahale.metrics.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Tests for the Timer class.
 *
 * @author Brandon Arp (barp at groupon dot com)
 */
public class TimerTest {

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

    @SuppressWarnings("deprecation")
    @Test
    public void setTimer() {
        final Timer timer = new Timer("foo", _lock, Clock.defaultClock());
        timer.update(18, TimeUnit.MILLISECONDS);
        Mockito.verifyNoInteractions(_metrics);
        Mockito.verify(_lock).readLocked(_delegateCaptor.capture());
        _delegateCaptor.getValue().accept(_metrics);
        Mockito.verify(_metrics).setTimer("foo", 18, Units.MILLISECOND);
    }

    @Test
    public void time() {
        final Timer timer = new Timer("foo", _lock, Clock.defaultClock());
        timer.time();
    }

    @Mock
    private SafeRefLock<Metrics> _lock;
    @Captor
    private ArgumentCaptor<Consumer<Metrics>> _delegateCaptor;
    @Mock
    private Metrics _metrics;
}
