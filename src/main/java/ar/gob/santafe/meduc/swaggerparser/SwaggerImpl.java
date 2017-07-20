package ar.gob.santafe.meduc.swaggerparser;

import groovy.json.JsonSlurper;
import groovy.lang.GroovyShell;
import groovy.lang.MetaMethod;
import groovy.lang.Script;
import groovy.sql.Sql;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import javax.ws.rs.HttpMethod;

/**
 *
 * @author enorrmann
 */
public class SwaggerImpl {

    private String swaggerString;
    private Swagger swagger;
    private DataSource dataSource;
    //private String scriptString;
    private Script script;
    private Status status;

    public Object invoke(HttpServletRequest request) {

        Sql sql = null;
        try {
            Object response = null;

            if (dataSource != null) {
                sql = new Sql(dataSource);
            }

            String pathString = extractPath(swagger, request);
            Path path = swagger.getPath(pathString);
            String operationId = extractOperationId(path, request);
            Map parameters = extractParameters(swagger.getBasePath() + pathString, request);
            parameters.put("$sql", sql);
            response = script.invokeMethod(operationId, parameters);

            sql.close();

            return response;

        } catch (Exception e) {
            if (sql != null) {
                sql.close();
            }
            throw e;
        }
    }

    public SwaggerImpl(String swaggerString, String scriptString) {
        this(swaggerString, scriptString, null);
    }

    public SwaggerImpl(String swaggerString, String scriptString, DataSource dataSource) {
        this.swaggerString = swaggerString;
        //this.scriptString = scriptString;
        this.swagger = new SwaggerParser().parse(swaggerString);
        GroovyShell shell = new GroovyShell();
        this.script = shell.parse(scriptString);
        this.dataSource = dataSource;
        this.status = new Status();

    }

    private boolean matches(String url, String pattern) {
        String[] urlParts = url.split("/");
        String[] patternParts = pattern.split("/");
        if (urlParts.length != patternParts.length) {
            return false;
        }
        for (int i = 0; i < urlParts.length; i++) {
            if (!urlParts[i].equals(patternParts[i]) && !isVariable(patternParts[i])) {
                return false;
            }
        }
        return true;

    }

    private boolean isVariable(String patternPart) {
        return patternPart != null
                && !patternPart.isEmpty()
                && patternPart.startsWith("{")
                && patternPart.endsWith("}");
    }

    private String extractPath(Swagger swagger, HttpServletRequest request) {

        String basePath = swagger.getBasePath();
        String requestUri = request.getRequestURI();

        String ruta = null;

        for (String unPath : swagger.getPaths().keySet()) {
            boolean matches = matches(requestUri, basePath + unPath);
            if (matches) {
                ruta = unPath;
            }
        }

        return ruta;

    }

    private Map extractParameters(String pathString, HttpServletRequest request) {
        Map q1 = extractGetParameters(request);
        Map q2 = extractPathParameters(pathString, request);
        Map q3 = extractPostParameters(request);
        Map q = new HashMap();
        q.putAll(q1);
        q.putAll(q2);
        q.putAll(q3);
        return q;
    }

    private Map extractPathParameters(String pattern, HttpServletRequest request) {
        String pathString = request.getRequestURI();
        Map parameters = new HashMap<>();
        String[] urlParts = pathString.split("/");
        String[] patternParts = pattern.split("/");
        if (urlParts.length == patternParts.length) {
            for (int i = 0; i < urlParts.length; i++) {
                if (!urlParts[i].equals(patternParts[i]) && isVariable(patternParts[i])) {
                    parameters.put(patternParts[i], urlParts[i]);
                }
            }
        }
        return parameters;
    }

    private Map extractGetParameters(HttpServletRequest request) {
        Map parameters = new HashMap<>();
        Map<String, String[]> paramMap = request.getParameterMap();
        for (String aParameter : paramMap.keySet()) {
            parameters.put(aParameter, paramMap.get(aParameter)[0]);
        }
        return parameters;
    }

    private Map extractPostParameters(HttpServletRequest request) {
        Map parameters = new HashMap<>();
        try {
            String postData = request.getReader().lines().collect(java.util.stream.Collectors.joining(System.lineSeparator()));
            if (postData != null && !postData.isEmpty()) {
                JsonSlurper js = new JsonSlurper();
                Map data = (Map) js.parseText(postData);
                data.forEach(parameters::putIfAbsent);

            }
        } catch (Exception ex) {
            Logger.getLogger(SwaggerServlet.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return parameters;

    }

    private String extractOperationId(Path path, HttpServletRequest request) {
        String opID = null;
        if (request.getMethod().equals(HttpMethod.GET)) {
            opID = path.getGet().getOperationId();
        } else if (request.getMethod().equals(HttpMethod.PUT)) {
            opID = path.getPut().getOperationId();
        } else if (request.getMethod().equals(HttpMethod.POST)) {
            opID = path.getPost().getOperationId();
        } else if (request.getMethod().equals(HttpMethod.DELETE)) {
            opID = path.getDelete().getOperationId();
        }

        return opID;

    }

    public String getSwaggerString() {
        return this.swaggerString;
    }

    public Status getStatus() {
        return this.status;
    }

    public boolean canServe(HttpServletRequest request) {
        String pathString = extractPath(swagger, request);
        return pathString != null && !pathString.isEmpty();
    }

    class Status {

        private Map<String, Operation> operations = new HashMap<>();
        private Map<String, Operation> implemented = new HashMap<>();
        private Map<String, Operation> notImplemented = new HashMap<>();

        public boolean isOk() {
            return swagger != null && script != null;
        }

        @Override
        public String toString() {
            return "swagger ok :" + (swagger != null)
                    + "script ok :" + (script != null);
        }

        public Status() {
            swagger.getPaths().forEach((pathName, path) -> {
                path.getOperations().forEach(operation -> {
                    operations.put(operation.getOperationId(), operation);
                });
            });

            for (String operationId : operations.keySet()) {
                List<MetaMethod> methods = script.getMetaClass().getMethods();
                for (MetaMethod aMethod : methods) {
                    if (aMethod.getName().equals(operationId)) {
                        implemented.put(operationId, operations.get(operationId));
                    }
                }
            }

            for (String operationId : operations.keySet()) {
                if (implemented.get(operationId) == null) {
                    notImplemented.put(operationId, operations.get(operationId));
                }

            }

        }

    }
}
