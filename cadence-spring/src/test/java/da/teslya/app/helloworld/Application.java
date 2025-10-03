package da.teslya.app.helloworld;

import com.uber.cadence.activity.ActivityOptions;
import com.uber.cadence.client.WorkflowOptions;
import com.uber.cadence.worker.Worker;
import com.uber.cadence.worker.WorkerFactory;
import da.teslya.springframework.cadence.annotation.EnableCadence;
import da.teslya.springframework.cadence.stub.ActivityOptionsConfigurer;
import da.teslya.springframework.cadence.stub.WorkflowOptionsConfigurer;
import da.teslya.springframework.cadence.worker.WorkerFactoryConfigurer;
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
public class Application implements WorkerFactoryConfigurer, WorkflowOptionsConfigurer,
    ActivityOptionsConfigurer, ApplicationContextAware {

  private ApplicationContext applicationContext;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void configure(String factoryName, WorkerFactory factory,
      Map<String, Class<?>> workflowImplementations,
      Map<String, Class<?>> activityImplementations) {

    Worker worker = factory.newWorker("my_task_list");

    workflowImplementations.forEach((name, clazz) -> {
      Class<?> workflowInterface = clazz.getInterfaces()[0];
      worker.addWorkflowImplementationFactory((Class) workflowInterface,
          () -> applicationContext.getBean(name, workflowInterface));
    });
    worker.registerActivitiesImplementations(
        activityImplementations.keySet().stream().map(applicationContext::getBean).toArray());
  }

  @Override
  public void configure(String workflowName, WorkflowOptions.Builder optionsBuilder) {
    optionsBuilder
        .setTaskList("my_task_list")
        .setExecutionStartToCloseTimeout(Duration.ofSeconds(5));
  }

  @Override
  public void configure(String activityName, ActivityOptions.Builder optionsBuilder) {
    optionsBuilder
        .setScheduleToCloseTimeout(Duration.ofSeconds(5));
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
