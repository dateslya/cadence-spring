/*
 * Copyright (c) 2022-2025 Dmitry Teslya
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package da.teslya.springframework.cadence.worker;

import com.uber.cadence.worker.WorkerFactory;
import da.teslya.springframework.cadence.event.WorkerStartedEvent;
import da.teslya.springframework.cadence.event.WorkerStartingEvent;
import da.teslya.springframework.cadence.event.WorkerStoppedEvent;
import da.teslya.springframework.cadence.event.WorkerStoppingEvent;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;

/**
 * @author Dmitry Teslya
 */
@Slf4j
@RequiredArgsConstructor
public class WorkerContainer implements SmartLifecycle, ApplicationEventPublisherAware {

  private final Lock lock = new ReentrantLock();

  private final WorkerFactory workerFactory;

  private ApplicationEventPublisher applicationEventPublisher;

  private volatile boolean running = false;

  @Override
  public void start() {
    lock.lock();
    try {
      if (!running) {
        applicationEventPublisher.publishEvent(new WorkerStartingEvent(this));
        workerFactory.start();
        running = true;
        log.info("Workers successfully started");
        applicationEventPublisher.publishEvent(new WorkerStartedEvent(this));
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void stop() {
    lock.lock();
    try {
      if (running) {
        applicationEventPublisher.publishEvent(new WorkerStoppingEvent(this));
        workerFactory.shutdown();
        running = false;
        log.info("Workers successfully stopped");
        applicationEventPublisher.publishEvent(new WorkerStoppedEvent(this));
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean isRunning() {
    lock.lock();
    try {
      return running;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }
}
