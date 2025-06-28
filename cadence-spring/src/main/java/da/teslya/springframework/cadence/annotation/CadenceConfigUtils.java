package da.teslya.springframework.cadence.annotation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CadenceConfigUtils {

  public String WORKER_CONTAINER_BEAN_NAME = "da.teslya.springframework.cadence.worker.internalWorkerContainer";
  public String WORKER_FACTORY_PROCESSOR_BEAN_NAME = "da.teslya.springframework.cadence.worker.internalWorkerFactoryBeanPostProcessor";
}
