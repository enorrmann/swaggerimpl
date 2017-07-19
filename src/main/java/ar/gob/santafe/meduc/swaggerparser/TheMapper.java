package ar.gob.santafe.meduc.swaggerparser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.toMap;
import java.util.stream.Stream;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

/**
 *
 * @author enorrmann
 */
public class TheMapper {

    public static void main(String[] args) {
        try {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

            ScriptContext newContext = new SimpleScriptContext();
            newContext.setBindings(getBindings(), ScriptContext.ENGINE_SCOPE);
            engine.setContext(newContext);
            engine.eval(new FileReader("/home/enorrmann/workspace_gf3/swaggerparser/src/main/resources/script.js"));

            Invocable invocable = (Invocable) engine;

            Object result = invocable.invokeFunction("fun1", "Peter Parker");
            invocable.invokeFunction("fun3");
            System.out.println(result);
            System.out.println(result.getClass());

        } catch (ScriptException | NoSuchMethodException | FileNotFoundException ex) {
            Logger.getLogger(TheMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Bindings getBindings() {
        return new SimpleBindings(Stream.of(
                new SimpleEntry<>("a", 10),
                new SimpleEntry<>("b", 20))
                .collect(toMap(SimpleEntry::getKey, SimpleEntry::getValue)));
    }
}
