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

package da.teslya.springframework.cadence.stub;

import com.uber.cadence.workflow.ChildWorkflowOptions;
import com.uber.cadence.workflow.Workflow;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Dmitry Teslya
 */
@RequiredArgsConstructor
public class ChildWorkflowStubFactoryBean<T> implements StubFactoryBean<T>,
    ApplicationContextAware, InitializingBean {

  private final String name;
  private final Class<T> type;

  private ApplicationContext applicationContext;
  private List<ChildWorkflowOptionsCustomizer> optionsCustomizers;

  @Override
  public void afterPropertiesSet() throws Exception {
    optionsCustomizers = applicationContext.getBeanProvider(ChildWorkflowOptionsCustomizer.class)
        .orderedStream().toList();
  }

  @Override
  public T getObject() throws Exception {

    ChildWorkflowOptions.Builder builder = new ChildWorkflowOptions.Builder();
    optionsCustomizers.forEach(c -> c.customize(name, builder));
    ChildWorkflowOptions options = builder.build();

    return Workflow.newChildWorkflowStub(type, options);
  }

  @Override
  public Class<?> getObjectType() {
    return type;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
