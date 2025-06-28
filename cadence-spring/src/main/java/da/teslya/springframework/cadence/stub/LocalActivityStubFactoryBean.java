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

import com.uber.cadence.activity.LocalActivityOptions;
import com.uber.cadence.workflow.Workflow;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Dmitry Teslya
 */
@Setter
@RequiredArgsConstructor
public class LocalActivityStubFactoryBean<T> implements StubFactoryBean<T> {

  private final String name;
  private final Class<T> type;

  @Autowired
  private LocalActivityOptionsConfigurer optionsConfigurer;

  @Override
  public T getObject() throws Exception {

    LocalActivityOptions.Builder builder = new LocalActivityOptions.Builder();
    optionsConfigurer.configure(name, builder);
    LocalActivityOptions options = builder.build();

    return Workflow.newLocalActivityStub(type, options);
  }

  @Override
  public Class<?> getObjectType() {
    return type;
  }
}
