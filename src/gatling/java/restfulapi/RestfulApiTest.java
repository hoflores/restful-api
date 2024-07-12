package restfulapi;

import java.time.Duration;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class RestfulApiTest extends Simulation {

    // Set to variables and define preconditions
    String baseUrl = System.getProperty("baseUrl", "https://api.restful-api.dev");
    String contentHeader = "Content-Type";
    String applicationHeader = "application/json";
    String concurrentUsers = System.getProperty("concurrentUsers", "10");

    // Define the data
    FeederBuilder.FileBased<Object> feeder = jsonFile("data/restfulFile.json").circular();

    // We want to test the Restful API with 10 Items

    // Define the base URL and headers
    private HttpProtocolBuilder httpProtocol = http
            .baseUrl(baseUrl);


    // Define the scenario

    // POST Method

    ScenarioBuilder scn = scenario("Result API Dev")
            .feed(feeder)
            .exec(http("CREATE Item")
                    .post("/objects")
                    .header(contentHeader, applicationHeader)
                    .body(StringBody(
                            """
                               {"name": "#{name}",
                               "data": {"year": "#{data.year}",
                               "price": "#{data.price}",
                               "CPUmodel": "#{data.model}",
                               "HardDiskSize": "#{data.size}"}}
                              """
                    )).asJson()
                    .check(jmesPath("id").find().saveAs("id"))
                    .check(bodyString().saveAs("body"))
                    .check(status().is(200))
            )
            .exec(
                    session -> {
                        System.out.println("Created Item Id: " + session.getString("id"));
                        System.out.println("Response Body: " + session.getString("body"));
                        return session;
                    }
            )

            // PUT Method
            .exec(http("UPDATE Item")
                    .put("/objects/#{id}")
                    .header(contentHeader, applicationHeader)
                    .body(StringBody(
                            """
                               {"name": "#{name}",
                               "data": {"year": "#{data.year}",
                               "price": "#{data.price}",
                               "CPUmodel": "#{data.model}",
                               "HardDiskSize": "#{data.size}",
                               "color": "#{data.color}"}}
                              """
                    )).asJson()
                    .check(bodyString().saveAs("body"))
                    .check(status().is(200))
            )

            .exec(
                    session -> {
                        System.out.println(" Updated Response Body: " + session.getString("body"));
                        return session;
                    }
            )

            // GET Method
            .exec(http("GET Item ")
                    .get("/objects/#{id}")
                    .header(contentHeader, applicationHeader)
                    .check(bodyString().saveAs("body"))
                    .check(status().is(200))
                    .check(jmesPath("id").isEL("#{id}"))
                    .check(jmesPath("name").isEL("#{name}"))
                    .check(jmesPath("data.year").isEL("#{data.year}"))
                    .check(jmesPath("data.price").isEL("#{data.price}"))
                    .check(jmesPath("data.CPUmodel").isEL("#{data.model}"))
                    .check(jmesPath("data.HardDiskSize").isEL("#{data.size}"))
                    .check(jmesPath("data.color").isEL("#{data.color}"))
            )

            .exec(
                    session -> {
                        System.out.println("Get Response Body: " + session.getString("body"));
                        return session;
                    }
            );

    // Set up the scenario
    {
        setUp(
                scn.injectClosed(
                        constantConcurrentUsers(Integer.parseInt(concurrentUsers)).during(Duration.ofSeconds(10)
                        )
                )
        ).protocols(httpProtocol);
    }
}
