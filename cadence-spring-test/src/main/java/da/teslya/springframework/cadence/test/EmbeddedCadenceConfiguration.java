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

package da.teslya.springframework.cadence.test;

import com.uber.cadence.client.WorkflowClient;
import com.uber.cadence.client.WorkflowClientOptions;
import com.uber.cadence.converter.DataConverter;
import com.uber.cadence.serviceclient.IWorkflowService;
import com.uber.cadence.testing.TestEnvironmentOptions;
import com.uber.cadence.testing.TestWorkflowEnvironment;
import com.uber.cadence.worker.WorkerFactory;
import com.uber.cadence.worker.WorkerFactoryOptions;
import com.uber.cadence.workflow.WorkflowInterceptor;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.springframework.context.annotation.Bean;

/**
 * @author Dmitry Teslya
 */
public class EmbeddedCadenceConfiguration {

  @Bean
  public TestWorkflowEnvironment testWorkflowEnvironment(
      @Nullable WorkflowClientOptions workflowClientOptions,
      @Nullable WorkerFactoryOptions workerFactoryOptions, @Nullable DataConverter dataConverter,
      @Nullable Function<WorkflowInterceptor, WorkflowInterceptor> interceptorFactory) {

    TestEnvironmentOptions.Builder builder = new TestEnvironmentOptions.Builder();

    Optional.ofNullable(interceptorFactory).ifPresent(builder::setInterceptorFactory);
    Optional.ofNullable(dataConverter).ifPresent(builder::setDataConverter);
    Optional.ofNullable(workflowClientOptions).ifPresent(builder::setWorkflowClientOptions);
    Optional.ofNullable(workerFactoryOptions).ifPresent(builder::setWorkerFactoryOptions);

    return TestWorkflowEnvironment.newInstance(builder.build());
  }

  @Bean
  public IWorkflowService testWorkflowService(TestWorkflowEnvironment environment) {
    return environment.getWorkflowService();
  }

  @Bean
  public WorkflowClient testWorkflowClient(TestWorkflowEnvironment environment) {
    return environment.newWorkflowClient();
  }

  @Bean
  public WorkerFactory testWorkerFactory(TestWorkflowEnvironment environment) {
    return environment.getWorkerFactory();
  }
}
