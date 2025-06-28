package da.teslya.springframework.cadence.worker;

import com.uber.cadence.worker.WorkerFactory;
import java.util.Map;

public interface WorkerFactoryConfigurer {

  void configure(WorkerFactory factory, Map<String, Class<?>> workflowBeans,
      Map<String, Class<?>> activityBeans);
}
