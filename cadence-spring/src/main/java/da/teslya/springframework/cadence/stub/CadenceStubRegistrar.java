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

import da.teslya.springframework.cadence.annotation.ActivityStub;
import da.teslya.springframework.cadence.annotation.ChildWorkflowStub;
import da.teslya.springframework.cadence.annotation.EnableCadence;
import da.teslya.springframework.cadence.annotation.LocalActivityStub;
import da.teslya.springframework.cadence.annotation.WorkflowStub;
import java.beans.Introspector;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * @author Dmitry Teslya
 */
@Slf4j
public class CadenceStubRegistrar implements ImportBeanDefinitionRegistrar {

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
      BeanDefinitionRegistry registry) {

    Set<BeanDefinition> candidateComponents = getCandidateComponent(importingClassMetadata);
    for (BeanDefinition candidateComponent : candidateComponents) {
      if (candidateComponent instanceof AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        if (metadata.hasAnnotation(WorkflowStub.class.getName())) {
          registerWorkflowStub(registry, metadata);
        } else if (metadata.hasAnnotation(ChildWorkflowStub.class.getName())) {
          registerChildWorkflowStub(registry, metadata);
        } else if (metadata.hasAnnotation(ActivityStub.class.getName())) {
          registerActivityStub(registry, metadata);
        } else if (metadata.hasAnnotation(LocalActivityStub.class.getName())) {
          registerLocalActivityStub(registry, metadata);
        }
      }
    }
  }

  private Set<BeanDefinition> getCandidateComponent(AnnotationMetadata importingClassMetadata) {

    String importingClassName = importingClassMetadata.getClassName();
    Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(
        EnableCadence.class.getName());
    Assert.notNull(attributes,
        String.format("@%S should be specified", EnableCadence.class.getSimpleName()));

    AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(attributes);
    Class<?>[] stubs = annotationAttributes.getClassArray("stubs");

    Set<BeanDefinition> candidateComponents = new LinkedHashSet<>();

    if (ObjectUtils.isEmpty(stubs)) {
      ClassPathScanningCandidateComponentProvider provider = createCandidateComponentProvider();
      provider.addIncludeFilter(new AnnotationTypeFilter(WorkflowStub.class));
      provider.addIncludeFilter(new AnnotationTypeFilter(ChildWorkflowStub.class));
      provider.addIncludeFilter(new AnnotationTypeFilter(ActivityStub.class));
      provider.addIncludeFilter(new AnnotationTypeFilter(LocalActivityStub.class));

      Set<String> basePackages = getBasePackages(importingClassName, annotationAttributes);
      for (String basePackage : basePackages) {
        candidateComponents.addAll(provider.findCandidateComponents(basePackage));
      }
    } else {
      for (Class<?> clazz : stubs) {
        candidateComponents.add(new AnnotatedGenericBeanDefinition(clazz));
      }
    }

    return candidateComponents;
  }

  private Set<String> getBasePackages(String importingClassName,
      AnnotationAttributes annotationAttributes) {

    Set<String> basePackages = new HashSet<>();

    for (String pkg : annotationAttributes.getStringArray("value")) {
      if (StringUtils.hasText(pkg)) {
        basePackages.add(pkg);
      }
    }

    for (String pkg : annotationAttributes.getStringArray("basePackages")) {
      if (StringUtils.hasText(pkg)) {
        basePackages.add(pkg);
      }
    }

    for (Class<?> clazz : annotationAttributes.getClassArray("basePackageClasses")) {
      basePackages.add(ClassUtils.getPackageName(clazz));
    }

    if (basePackages.isEmpty()) {
      basePackages.add(ClassUtils.getPackageName(importingClassName));
    }

    return basePackages;
  }

  private ClassPathScanningCandidateComponentProvider createCandidateComponentProvider() {
    return new ClassPathScanningCandidateComponentProvider(false) {
      @Override
      protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        AnnotationMetadata metadata = beanDefinition.getMetadata();
        return metadata.isIndependent() && !metadata.isAnnotation();
      }
    };
  }

  private void registerWorkflowStub(BeanDefinitionRegistry registry, AnnotationMetadata metadata) {
    registerStub(registry, metadata, WorkflowStubFactoryBean.class, WorkflowStub.class);
  }

  private void registerChildWorkflowStub(BeanDefinitionRegistry registry,
      AnnotationMetadata metadata) {
    registerStub(registry, metadata, ChildWorkflowStubFactoryBean.class, ChildWorkflowStub.class);
  }

  private void registerActivityStub(BeanDefinitionRegistry registry, AnnotationMetadata metadata) {
    registerStub(registry, metadata, ActivityStubFactoryBean.class, ActivityStub.class);
  }

  private void registerLocalActivityStub(BeanDefinitionRegistry registry,
      AnnotationMetadata metadata) {
    registerStub(registry, metadata, LocalActivityStubFactoryBean.class, LocalActivityStub.class);
  }

  private void registerStub(BeanDefinitionRegistry registry, AnnotationMetadata metadata,
      Class<?> factoryBeanType, Class<?> annotationType) {

    String className = metadata.getClassName();

    Assert.isTrue(metadata.isInterface(),
        String.format("@%s can only be specified on an interface",
            annotationType.getSimpleName()));

    AnnotationAttributes attributes = AnnotationAttributes.fromMap(
        metadata.getAnnotationAttributes(annotationType.getName()));
    Assert.notNull(attributes,
        String.format("%s doesn't have @%s", className, annotationType.getName()));

    Class<?> type = ClassUtils.resolveClassName(className, null);
    String name = getBeanName(attributes, type);

    AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(
            factoryBeanType)
        .addConstructorArgValue(name)
        .addConstructorArgValue(type)
        .getBeanDefinition();
    BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, name);
    BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);

    log.info("Registered {}{name = '{}', interface = '{}'}", annotationType.getSimpleName(), name,
        type.getName());
  }

  private String getBeanName(AnnotationAttributes attributes, Class<?> type) {

    String name = attributes.getString("value");

    if (!StringUtils.hasText(name)) {
      name = Introspector.decapitalize(type.getSimpleName());
    }

    return name;
  }
}
