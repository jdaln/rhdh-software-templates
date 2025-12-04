package ${{values.javaPackageName}};

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ExampleResourceTest {

    @Test
    public void testHelloEndpoint() {
        // The endpoint will return "Hello RESTEasy" if no database connection or names available
        // or "Hello [name]" if database is available
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(anyOf(is("Hello RESTEasy"), is("Hello Alice"), is("Hello Bob"), 
                           is("Hello Charlie"), is("Hello Diana"), is("Hello Eve")));
    }
}