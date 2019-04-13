import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.servlet.ServletContext
import javax.ws.rs.core.Context

@Path("/")
class ExampleFile {

    @Context
    var context: ServletContext? = null

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun get() = "hello world"

    @Path("/id")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun id() = context!!.getResourceAsStream("/WEB-INF/build.id").reader().readText()
}