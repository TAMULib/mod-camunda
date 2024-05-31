package org.folio.rest.camunda.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.folio.rest.camunda.exception.ScriptEngineLoadFailed;
import org.folio.rest.camunda.exception.ScriptEngineUnsupported;
import org.folio.rest.workflow.enums.ScriptType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

/**
 * Execute custom scripts for supported languages.
 */
@Service
public class ScriptEngineService {

  private static final String UTILS_PREFIX = "scripts/utils.";

  private static final String ENGINE_PREFIX = "scripts/engine.";

  private static final Map<ScriptType, String> SCRIPT_ENGINE_TYPES = new EnumMap<ScriptType, String>(ScriptType.class);

  static {
    for (ScriptType type : ScriptType.values()) {
      SCRIPT_ENGINE_TYPES.put(type, type.getExtension());
    }
  }

  private ScriptEngineManager scriptEngineManager;

  private Map<String, ScriptEngine> scriptEngines;

  public ScriptEngineService() {
    configureScriptEngines();
  }

  /**
   * Initialize the engines for this class.
   */
  private void configureScriptEngines() {
    scriptEngineManager = new ScriptEngineManager();
    scriptEngines = new HashMap<String, ScriptEngine>();

    System.out.print("\n\n\nDEBUG: by extension = " + scriptEngineManager.getEngineByExtension(".py") + "\n\n\n");
    System.out.print("\n\n\nDEBUG: by name = " + scriptEngineManager.getEngineByName("python") + "\n\n\n");

    scriptEngineManager.getEngineFactories().forEach(sef -> {
      System.out.print("\n\n\nDEBUG: by factory = " + sef.getEngineName() + ", " + sef.getEngineVersion() + ", lang = " + sef.getLanguageName() + ", engine = " + sef.getScriptEngine() + "\n\n\n");
    });
  }

  /**
   * Register a script for later execution.
   *
   * @param extension The extension of the language associated with the desired
   *                  engine.
   * @param name      The name of the script function to execute.
   * @param script    The code associated with the given language that will be
   *                  executed when the script engine for the given extension is
   *                  run.
   * @throws ScriptException
   *
   * @throws IOException
   * @throws ScriptEngineUnsupported
   * @throws NoSuchMethodException
   * @throws ScriptEngineLoadFailed
   */
  public void registerScript(String extension, String name, String script)
      throws ScriptException, IOException, ScriptEngineUnsupported, NoSuchMethodException, ScriptEngineLoadFailed {
//    if (!SCRIPT_ENGINE_TYPES.containsValue(extension)) {
//      throw new ScriptEngineUnsupported(extension);
//    }

    System.out.print("\n\n\nDEBUG: by extension = " + scriptEngineManager.getEngineByExtension(".py") + "\n\n\n");
    System.out.print("\n\n\nDEBUG: by name = " + scriptEngineManager.getEngineByName("python") + "\n\n\n");

    scriptEngineManager.getEngineFactories().forEach(sef -> {
      System.out.print("\n\n\nDEBUG: by factory = " + sef.getEngineName() + ", " + sef.getEngineVersion() + ", lang = " + sef.getLanguageName() + "\n\n\n");
    });

    Optional<ScriptEngine> maybeScriptEngine = Optional.ofNullable(scriptEngines.get(extension));

    if (!maybeScriptEngine.isPresent()) {
      System.out.print("\n\n\nDEBUG: script engine is NULL\n\n\n");
      ScriptEngine newEngine = scriptEngineManager.getEngineByExtension(extension);

      if (newEngine == null) {
        throw new ScriptEngineLoadFailed(extension);
      }

      scriptEngines.put(extension, newEngine);

      if (SCRIPT_ENGINE_TYPES.containsValue(extension)) {
        newEngine.eval(loadScript(UTILS_PREFIX + extension));
      }

      maybeScriptEngine = Optional.ofNullable(newEngine);
    }

    ScriptEngine scriptEngine = maybeScriptEngine.get();
    String scriptFunctionName = name;
    scriptEngine.eval(
        String.format(loadScript(ENGINE_PREFIX + extension), scriptFunctionName, preprocessScript(script, extension)));
  }

  /**
   * Execute a registered script.
   *
   * @param extension The extension of the language associated with the desired
   *                  engine.
   * @param name      The name of the script function to execute.
   * @param args      Additional arguments to pass to the script function.
   *
   * @return The return results of the executed script. This will often be a JSON
   *         encoded String.
   *
   * @throws NoSuchMethodException
   * @throws ScriptException
   * @throws ScriptEngineLoadFailed 
   * @throws IOException 
   */
  public Object runScript(String extension, String name, Object... args) throws NoSuchMethodException, ScriptException, ScriptEngineLoadFailed, IOException {
    System.out.print("\n\n\nDEBUG: extension" + extension + "\n\n\n");
    ScriptEngine scriptEngine = scriptEngines.get(extension);
    System.out.print("\n\n\nDEBUG: script engine is " + scriptEngine + "\n\n\n");
    System.out.print("\n\n\nDEBUG: scriptEngines = ");
    scriptEngines.forEach((str, se) -> {
      System.out.print("\nse (" + str + ") = " + se);
    });
    System.out.print("\n\n\n");

    if (scriptEngine == null) {
      System.out.print("\n\n\nDEBUG: script engine is NULL\n\n\n");
      ScriptEngine newEngine = scriptEngineManager.getEngineByExtension(extension);

      if (newEngine == null) {
        throw new ScriptEngineLoadFailed(extension);
      }

      scriptEngines.put(extension, newEngine);

      if (SCRIPT_ENGINE_TYPES.containsValue(extension)) {
        newEngine.eval(loadScript(UTILS_PREFIX + extension));
      }

      scriptEngine = newEngine;
    }

    Invocable invocable = (Invocable) scriptEngine;
    return invocable.invokeFunction(name, args);
  }

  /**
   * Load a script from the resources directory.
   *
   * @param filename The path to a file, within the resource directory.
   *
   * @return The contents of the script file.
   *
   * @throws IOException
   */
  private String loadScript(String filename) throws IOException {
    InputStream inputStream = new ClassPathResource(filename).getInputStream();
    return StreamUtils.copyToString(inputStream, Charset.defaultCharset());
  }

  /**
   * Pre-process a script to ensure that it is more friendly for any exceptional
   * language.
   *
   * For example, Python is space/tab conscious, so make the tabs consistent with
   * the two spaces used in the engine.py.
   *
   * @param script    The script to potentially pre-process.
   * @param extension The extension representing the language of the script.
   *
   * @return The pre-processed script.
   */
  private String preprocessScript(String script, String extension) {

    if (extension.equals(ScriptType.PYTHON.getExtension())) {
      // Ensure that 2 leading spaces exist before newlines and that
      // there are no trailing white spaces.
      String processed = script.replace("\r\n", "\n");
      processed = script.replace("\r", "\n");
      processed = script.replace("\n", "\n  ");
      processed.trim();
      return processed;
    }

    return script;
  }

}
