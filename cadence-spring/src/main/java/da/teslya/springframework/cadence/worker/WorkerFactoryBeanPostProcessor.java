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

import com.uber.cadence.activity.ActivityMethod;
import com.uber.cadence.worker.WorkerFactory;
import com.uber.cadence.workflow.WorkflowMethod;
import da.teslya.springframework.cadence.annotation.ActivityImplementation;
import da.teslya.springframework.cadence.annotation.WorkflowImplementation;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * @author Dmitry Teslya
 */
@Slf4j
public class WorkerFactoryBeanPostProcessor implements BeanDefinitionRegistryPostProcessor,
    BeanPostProcessor, ApplicationContextAware {

  private final Map<String, Class<?>> workflowImplementations = new HashMap<>();
  private final Map<String, Class<?>> activityImplementations = new HashMap<>();

  private ApplicationContext applicationContext;

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {

    for (String beanName : registry.getBeanDefinitionNames()) {
      BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
      if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
        AnnotationMetadata metadata = annotatedBeanDefinition.getMetadata();
        if (metadata.isConcrete()) {
          if (metadata.hasAnnotation(WorkflowImplementation.class.getName())) {
            ImplementationRegistrar.WORKFLOW.register(beanName, annotatedBeanDefinition,
                workflowImplementations::put);
          } else if (metadata.hasAnnotation(ActivityImplementation.class.getName())) {
            ImplementationRegistrar.ACTIVITY.register(beanName, annotatedBeanDefinition,
                activityImplementations::put);
          }
        }
      }
    }
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

    if (bean instanceof WorkerFactory factory) {
      WorkerFactoryConfigurer configurer = applicationContext.getBean(
          WorkerFactoryConfigurer.class);
      configurer.configure(factory, workflowImplementations, activityImplementations);
    }

    return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @RequiredArgsConstructor
  private enum ImplementationRegistrar {

    WORKFLOW(WorkflowMethod.class),
    ACTIVITY(ActivityMethod.class);

    private final Class<? extends Annotation> annotationType;

    private void register(String beanName, AnnotatedBeanDefinition beanDefinition,
        BiFunction<String, Class<?>, Class<?>> collector) {
      AnnotationMetadata metadata = beanDefinition.getMetadata();
      Class<?> implementationInterface = resolve(metadata.getClassName());
      collector.apply(beanName, implementationInterface);
      beanDefinition.setAutowireCandidate(false);
    }

    private Class<?> resolve(String className) {
      Class<?> clazz = forName(className);
      return Arrays.stream(clazz.getInterfaces())
          .filter(this::hasAnnotatedMethods)
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException(
              String.format("%s doesn't have interface with @%s", className,
                  annotationType.getSimpleName())));
    }

    @SneakyThrows
    private Class<?> forName(String className) {
      return ClassUtils.forName(className, null);
    }

    private boolean hasAnnotatedMethods(Class<?> clazz) {
      return Arrays.stream(clazz.getMethods())
          .map(m -> AnnotationUtils.getAnnotation(m, annotationType))
          .anyMatch(Objects::nonNull);
    }
  }
}
