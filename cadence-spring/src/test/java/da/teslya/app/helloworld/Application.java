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

@EnableCadence
@SpringBootApplication
public class Application implements WorkerFactoryConfigurer, WorkflowOptionsConfigurer,
    ActivityOptionsConfigurer, ApplicationContextAware {

  private ApplicationContext applicationContext;

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void configure(WorkerFactory factory, Map<String, Class<?>> workflowBeans,
      Map<String, Class<?>> activityBeans) {

    Worker worker = factory.newWorker("my_task_list");

    workflowBeans.forEach((name, clazz) ->
        worker.addWorkflowImplementationFactory((Class) clazz,
            () -> applicationContext.getBean(name)));

    worker.registerActivitiesImplementations(
        activityBeans.keySet().stream().map(applicationContext::getBean).toArray());
  }

  @Override
  public void configure(String name, WorkflowOptions.Builder builder) {
    builder
        .setTaskList("my_task_list")
        .setExecutionStartToCloseTimeout(Duration.ofSeconds(5));
  }

  @Override
  public void configure(String name, ActivityOptions.Builder builder) {
    builder
        .setScheduleToCloseTimeout(Duration.ofSeconds(5));
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
