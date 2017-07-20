package ar.gob.santafe.meduc.swaggerparser;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.sql.DataSource;

/**
 *
 * @author enorrmann
 */
@WebServlet(name = "TheServlet", urlPatterns = {"/api/*"})
public class ApiServlet extends SwaggerServlet {

    @Resource(name = "jdbc/libreta") // para tomcat -> name
    //@Resource(lookup = "jdbc/libreta") // para glassfish 4 -> lookup
    protected void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}
