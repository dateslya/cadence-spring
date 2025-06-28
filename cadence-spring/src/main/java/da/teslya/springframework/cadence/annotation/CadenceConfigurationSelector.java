package da.teslya.springframework.cadence.annotation;

import org.springframework.context.annotation.DeferredImportSelector;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

@Order
public class CadenceConfigurationSelector implements DeferredImportSelector {

  @Override
  public String[] selectImports(AnnotationMetadata importingClassMetadata) {
    return new String[]{CadenceBootstrapConfiguration.class.getName()};
  }
}
