package da.teslya.app.helloworld;

import com.uber.cadence.activity.ActivityOptions;
import com.uber.cadence.client.WorkflowOptions;
import com.uber.cadence.worker.Worker;
import com.uber.cadence.worker.WorkerFactory;
import da.teslya.springframework.cadence.annotation.EnableCadence;
import da.teslya.springframework.cadence.stub.ActivityOptionsCustomizer;
import da.teslya.springframework.cadence.stub.WorkflowOptionsCustomizer;
import da.teslya.springframework.cadence.worker.WorkerFactoryCustomizer;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author Dmitry Teslya
 */
@EnableCadence
@SpringBootApplication
public class Application implements WorkerFactoryCustomizer, WorkflowOptionsCustomizer,
    ActivityOptionsCustomizer, ApplicationContextAware {

  private static final String TASK_LIST = "my_task_list";

  private ApplicationContext applicationContext;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void customize(String factoryName, WorkerFactory factory,
      Map<String, Class<?>> workflowImplementations,
      Map<String, Class<?>> activityImplementations) {

    Worker worker = factory.newWorker(TASK_LIST);

    workflowImplementations.forEach((name, clazz) -> {
      Class<?> workflowInterface = clazz.getInterfaces()[0];
      worker.addWorkflowImplementationFactory((Class) workflowInterface,
          () -> applicationContext.getBean(name, workflowInterface));
    });
    worker.registerActivitiesImplementations(
        activityImplementations.keySet().stream().map(applicationContext::getBean).toArray());
  }

  @Override
  public void customize(String workflowName, WorkflowOptions.Builder optionsBuilder) {
    optionsBuilder
        .setTaskList(TASK_LIST)
        .setExecutionStartToCloseTimeout(Duration.ofSeconds(5));
  }

  @Override
  public void customize(String activityName, ActivityOptions.Builder optionsBuilder) {
    optionsBuilder
        .setScheduleToCloseTimeout(Duration.ofSeconds(5));
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
