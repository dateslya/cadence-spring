package da.teslya.springframework.cadence.annotation;

import da.teslya.springframework.cadence.worker.WorkerContainer;
import da.teslya.springframework.cadence.worker.WorkerFactoryBeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

public class CadenceBootstrapConfiguration implements ImportBeanDefinitionRegistrar {

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
      BeanDefinitionRegistry registry) {

    if (!registry.containsBeanDefinition(CadenceConfigUtils.WORKER_FACTORY_PROCESSOR_BEAN_NAME)) {
      registry.registerBeanDefinition(CadenceConfigUtils.WORKER_FACTORY_PROCESSOR_BEAN_NAME,
          new RootBeanDefinition(WorkerFactoryBeanPostProcessor.class));
    }

    if (!registry.containsBeanDefinition(CadenceConfigUtils.WORKER_CONTAINER_BEAN_NAME)) {
      registry.registerBeanDefinition(CadenceConfigUtils.WORKER_CONTAINER_BEAN_NAME,
          new RootBeanDefinition(WorkerContainer.class));
    }
  }
}
