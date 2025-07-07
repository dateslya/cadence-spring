package da.teslya.springframework.cadence.worker;

import com.uber.cadence.worker.WorkerFactory;
import java.util.Map;

/**
 * @author Dmitry Teslya
 */
public interface WorkerFactoryConfigurer {

  void configure(WorkerFactory factory, Map<String, Class<?>> workflowImplementations,
      Map<String, Class<?>> activityImplementations);
}
