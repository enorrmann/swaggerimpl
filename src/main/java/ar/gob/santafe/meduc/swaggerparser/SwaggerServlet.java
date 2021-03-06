package ar.gob.santafe.meduc.swaggerparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 *
 * @author enorrmann
 */
public class SwaggerServlet extends HttpServlet {

    private String read(String resource) throws IOException {
        InputStream input = getServletContext().getResourceAsStream(resource);
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input))) {
            return buffer.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }
    protected DataSource dataSource;
    private SwaggerImpl swaggerImpl;

    protected void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void init() {
        try {
            //String swaggerString = new String(Files.readAllBytes(Paths.get("/home/enorrmann/swagger.yaml")));
            String swaggerString = read("/WEB-INF/classes/api.yaml");
            String scriptString = read("/WEB-INF/classes/impl.groovy");
            this.swaggerImpl = new SwaggerImpl(swaggerString, scriptString, dataSource);
        } catch (IOException ex) {
            Logger.getLogger(SwaggerServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (request.getRequestURI().contains("api.yaml")) {
            response.setContentType("application/x-yaml;charset=UTF-8");
            response.getWriter().print(swaggerImpl.getSwaggerString());
        } else {

            response.setContentType("application/json;charset=UTF-8");
            try {
                if (swaggerImpl.canServe(request)) {
                    Object result = swaggerImpl.invoke(request);
                    response.getWriter().print(result);
                } else {
                    response.sendError(404);
                }
            } catch (Exception e) {
                response.sendError(500, e.getMessage());
                Logger.getLogger(SwaggerServlet.class.getName()).log(Level.SEVERE, null, e);
            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

}
