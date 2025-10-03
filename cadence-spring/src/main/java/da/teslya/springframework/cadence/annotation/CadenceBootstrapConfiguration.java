package da.teslya.springframework.cadence.annotation;

import da.teslya.springframework.cadence.worker.WorkerContainer;
import da.teslya.springframework.cadence.worker.WorkerFactoryBeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author Dmitry Teslya
 */
public class CadenceBootstrapConfiguration implements ImportBeanDefinitionRegistrar {

  public static final String WORKER_FACTORY_PROCESSOR_BEAN_NAME = "da.teslya.springframework.cadence.worker.internalWorkerFactoryBeanPostProcessor";
  public static final String WORKER_CONTAINER_BEAN_NAME = "da.teslya.springframework.cadence.worker.internalWorkerContainer";

  @Override
  public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
      BeanDefinitionRegistry registry) {

    if (!registry.containsBeanDefinition(WORKER_FACTORY_PROCESSOR_BEAN_NAME)) {
      registry.registerBeanDefinition(WORKER_FACTORY_PROCESSOR_BEAN_NAME,
          new RootBeanDefinition(WorkerFactoryBeanPostProcessor.class));
    }

    if (!registry.containsBeanDefinition(WORKER_CONTAINER_BEAN_NAME)) {
      registry.registerBeanDefinition(WORKER_CONTAINER_BEAN_NAME,
          new RootBeanDefinition(WorkerContainer.class));
    }
  }
}
