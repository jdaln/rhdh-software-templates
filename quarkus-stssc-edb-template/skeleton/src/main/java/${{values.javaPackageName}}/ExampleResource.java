package ${{values.javaPackageName}};

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/")
public class ExampleResource {

    @Inject
    NameRepository nameRepository;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/hello")
    public String hello() {
        Name name = nameRepository.getNextName();
        if (name != null && name.getName() != null) {
            return "Hello " + name.getName();
        }
        return "Hello RESTEasy";
    }

    // @GET
    // @Produces(MediaType.APPLICATION_JSON)
    // @Path("/listnames")
    // public List<String> listNames() {
    //     try {
    //         List<Name> names = nameRepository.listAll();
    //         return names.stream()
    //                 .map(Name::getName)
    //                 .collect(Collectors.toList());
    //     } catch (Exception e) {
    //         // Handle database connection errors gracefully
    //         return List.of();
    //     }
    // }
}