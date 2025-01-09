package org.folio.rest.camunda.delegate;

import static org.folio.rest.camunda.utility.TestUtility.TEST_FILE_PATH;
import static org.folio.rest.camunda.utility.TestUtility.i;
import static org.folio.rest.workflow.enums.FileOp.DELETE;
import static org.folio.rest.workflow.enums.FileOp.LIST;
import static org.folio.rest.workflow.enums.FileOp.WRITE;
import static org.folio.spring.test.mock.MockMvcConstant.JSON_OBJECT;
import static org.folio.spring.test.mock.MockMvcConstant.NULL_STR;
import static org.folio.spring.test.mock.MockMvcConstant.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.codehaus.plexus.util.FileUtils;
import org.folio.rest.camunda.exception.DelegateMissingRequiredProperty;
import org.folio.rest.camunda.service.ScriptEngineService;
import org.folio.rest.workflow.enums.FileOp;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Perform File Delegate unit testing.
 *
 * While this is technically a unit test, the actual files do get created.
 * It is impractical to mock the entire file operations.
 * Utilize temporary copies of files to perform operations.
 */
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class FileDelegateTest {

  /**
   * Test files are stored in an unused directory that is gitignored to help ensure isolation.
   */
  private static final String TMP_PATH = "tmp/";

  /**
   * Source test files are stored in a common directory and are expected to not be modified during tests.
   */
  private static final String FILES_PATH = "files/";

  private static final String SRC = "source";

  @Spy
  protected ObjectMapper objectMapper;

  @Spy
  protected RuntimeService runtimeService;

  @Mock
  private ScriptEngineService scriptEngineService;

  @Mock
  private Expression inputVariables;

  @Mock
  private Expression outputVariable;

  @Mock
  private Expression path;

  @Mock
  private Expression line;

  @Mock
  private Expression op;

  @Mock
  private Expression target;

  @Mock
  private DelegateExecution execution;

  @Mock
  private FlowElement element;

  @InjectMocks
  private FileDelegate fileDelegate;

  private static String procVarsData;

  @BeforeAll
  static void beforeAll() throws IOException {
    File tmpPath = new File(TEST_FILE_PATH + TMP_PATH);
    if (!tmpPath.exists()) {
      tmpPath.mkdir();
    }

    procVarsData = i("/" + FILES_PATH + "procVars.json");
  }

  @BeforeEach
  void beforeEach() {
    when(execution.getBpmnModelElementInstance()).thenReturn(element);
    when(element.getName()).thenReturn(fileDelegate.getClass().getSimpleName());

    lenient().when(execution.getCurrentActivityId()).thenReturn(UUID);
  }

  @AfterAll
  static void afterAll() throws IOException {
    FileUtils.deleteDirectory(TEST_FILE_PATH + TMP_PATH);
  }

  @ParameterizedTest
  @MethodSource("executeWorksBasicFor")
  void executeWorksNoOpTest(Boolean isDir, Boolean create, String name, String input, String output, int row) throws Exception {
    String sourceName = DELETE.name() + (isDir ? "dir" : "reg") + name;
    File filePath = prepareFilePath(isDir, create, sourceName, row);

    mockVars(null, input, output, null, filePath.toString());

    assertThrows(DelegateMissingRequiredProperty.class, () -> fileDelegate.execute(execution));
  }

  @ParameterizedTest
  @MethodSource("executeWorksBasicFor")
  void executeWorksDeleteTest(Boolean isDir, Boolean create, String name, String input, String output, int row) throws Exception {
    String sourceName = DELETE.name() + (isDir ? "dir" : "reg") + name;
    File filePath = prepareFilePath(isDir, create, sourceName, row);

    mockVars(DELETE, input, output, null, filePath.toString());

    fileDelegate.execute(execution);

    if (isDir && create) {
      // This is not supported yet in delegate, so the directory is expected to fail to be deleted.
      assertTrue(filePath.exists(), "Path " + filePath.toString() + " should exist #" + row);
    } else {
      assertFalse(filePath.exists(), "Path " + filePath.toString() + "should not exist #" + row);
    }
  }

  @ParameterizedTest
  @MethodSource("executeWorksBasicFor")
  void executeWorksListTest(Boolean isDir, Boolean create, String name, String input, String output, int row) throws Exception {
    String sourceName = LIST.name() + (isDir ? "dir" : "reg") + name;
    File filePath = prepareFilePath(isDir, create, sourceName, row);

    mockVars(LIST, input, output, null, filePath.toString());

    fileDelegate.execute(execution);

    if (create && isDir) {
      // TODO: handle this case.
      //assertEquals(???, outputVariable.getValue(execution), "outputVariable should be ??? #" + row);
    } else {
      // TODO: handle this case
      //assertEquals(???, outputVariable.getValue(execution), "outputVariable should be ??? #" + row);
    }
  }

  @ParameterizedTest
  @MethodSource("executeWorksBasicFor")
  void executeWorksWriteTest(Boolean isDir, Boolean create, String name, String input, String output, int row) throws Exception {
    String sourceName = WRITE.name() + (isDir ? "dir" : "reg") + name;
    File filePath = prepareFilePath(isDir, create, sourceName, row);

    mockVars(WRITE, input, output, null, filePath.toString());
    when(this.target.getValue(any(DelegateExecution.class))).thenReturn("" + filePath);

    fileDelegate.execute(execution);

    assertTrue(filePath.exists(), "Path " + filePath.toString() + " should exist #" + row);
  }

  /**
   * Clear out and delete the test files as needed.
   *
   * @param isDir TRUE if operating on directory, FALSE otherwise. 
   * @param create Create or do not create files.
   * @param sourceName The partial name of the files to use.
   * @param row The row number for the specific test.
   *
   * @return The file or NULL.
   *
   * @throws IOException On I/O error.
   */
  private File prepareFilePath(Boolean isDir, Boolean create, String sourceName, int row) throws IOException {
    File filePath = null;
    File fileSrc = new File(TEST_FILE_PATH + FILES_PATH + sourceName);
    filePath = new File(TEST_FILE_PATH + TMP_PATH + sourceName + row);

    if (filePath.exists()) {
      if (isDir) {
        FileUtils.deleteDirectory(filePath);
      } else {
        filePath.delete();
      }
    }

    if (create) {
      if (isDir) {
        FileUtils.copyDirectory(fileSrc, filePath);
      } else {
        FileUtils.copyFile(fileSrc, filePath);
      }
    }

    return filePath;
  }

  /**
   * Add the appropriate mocks for common variables.
   *
   * @param op The op field.
   * @param input The input variables field.
   * @param output The output variable field.
   * @param lineNum The line field.
   * @param filePath The path field.
   */
  private void mockVars(FileOp fileOp, String input, String output, String lineNum, String filePath) {
    fileDelegate.setOp(fileOp == null ? null : op);
    fileDelegate.setInputVariables(input == null ? null : inputVariables);
    fileDelegate.setOutputVariable(output == null ? null : outputVariable);
    fileDelegate.setLine(lineNum == null ? null : line);
    fileDelegate.setPath(filePath == null ? null : path);

    lenient().when(this.op.getValue(any(DelegateExecution.class))).thenReturn(fileOp == null ? "" : fileOp.toString());
    lenient().when(this.inputVariables.getValue(any(DelegateExecution.class))).thenReturn("" + input);
    lenient().when(this.outputVariable.getValue(any(DelegateExecution.class))).thenReturn("" + output);
    lenient().when(this.line.getValue(any(DelegateExecution.class))).thenReturn("" + lineNum);
    lenient().when(this.path.getValue(any(DelegateExecution.class))).thenReturn("" + filePath);
  }

  /**
   * Helper function for basic operation parameterized tests.
   *
   * @return
   *   The arguments array stream with the stream columns as:
   *     - Boolean isDir TRUE if operating on directory, FALSE otherwise. 
   *     - Boolean create Create or do not create files.
   *     - String name The partial name of the files to use.
   *     - String input The input variables JSON or null.
   *     - String output The output variable JSON or null.
   *     - int row A row number used for generating unique test files.
   */
  private static Stream<Arguments> executeWorksBasicFor() {
    return Stream.of(
      Arguments.of(false, false, SRC, procVarsData, JSON_OBJECT, 0),
      Arguments.of(false, true,  SRC, procVarsData, JSON_OBJECT, 1),
      Arguments.of(true,  false, SRC, procVarsData, JSON_OBJECT, 2),
      Arguments.of(true,  true,  SRC, procVarsData, JSON_OBJECT, 3),
      Arguments.of(false, false, SRC, procVarsData, NULL_STR,    4),
      Arguments.of(false, true,  SRC, procVarsData, NULL_STR,    5),
      Arguments.of(true,  false, SRC, procVarsData, NULL_STR,    6),
      Arguments.of(true,  true,  SRC, procVarsData, NULL_STR,    7),
      Arguments.of(false, false, SRC, NULL_STR,     JSON_OBJECT, 8),
      Arguments.of(false, true,  SRC, NULL_STR,     JSON_OBJECT, 9),
      Arguments.of(true,  false, SRC, NULL_STR,     JSON_OBJECT, 10),
      Arguments.of(true,  true,  SRC, NULL_STR,     JSON_OBJECT, 11),
      Arguments.of(false, false, SRC, NULL_STR,     NULL_STR,    12),
      Arguments.of(false, true,  SRC, NULL_STR,     NULL_STR,    13),
      Arguments.of(true,  false, SRC, NULL_STR,     NULL_STR,    14),
      Arguments.of(true,  true,  SRC, NULL_STR,     NULL_STR,    15)
    );
  }

}
