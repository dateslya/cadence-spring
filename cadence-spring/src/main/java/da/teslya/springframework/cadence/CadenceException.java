package da.teslya.springframework.cadence;

import org.springframework.core.NestedRuntimeException;

/**
 * @author Dmitry Teslya
 */
public class CadenceException extends NestedRuntimeException {

  public CadenceException(String msg) {
    super(msg);
  }

  public CadenceException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
