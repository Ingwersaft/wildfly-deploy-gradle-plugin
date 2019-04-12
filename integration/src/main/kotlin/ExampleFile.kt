import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/")
class ExampleFile {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun get() = "hello world"
}