import dto.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.oauth2;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;


public class RestAPITest {


    private Project project;
    private Issue issue;
    private Agiles agiles;
    private Articles articles;

    @BeforeClass
    public static void init() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/test/resources/token.properties"));
        RestAssured.baseURI = "http://193.233.193.42:9091/api";
        RestAssured.authentication = oauth2(properties.getProperty("ApiToken"));
    }


    @Test
    public void postProjectTest(){
        User leader = new User();
        leader.setName("shtrung.se@yandex.ru");
        leader.setId("2-4");

        project = new Project();
        project.setLeader(leader);
        project.setName("TestApi");
        project.setShortName("Test");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(project)
                .when()
                .post("/admin/projects");

        response.then()
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("$type", equalTo("Project"))
                .statusCode(200);

        project.setId(response.path("id"));
    }

    @Test(dependsOnMethods = "postProjectTest")
    public void validateProjectTest() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/test/resources/fields.properties"));

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("fields",properties.getProperty("fields_project"))
                .when()
                .get( String.format("/admin/projects/%s",project.getId()));

        response.then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schema/project_schema.json"));
    }

    @Test(dependsOnMethods = {"deleteIssueTest","deleteAgileTest","deleteArticlesTest"})
    public void deleteProjectTest(){
        Response response = given()
                .when()
                .delete(String.format("/admin/projects/%s",project.getId()));

        response.then().statusCode(200);
    }

    @Test(dependsOnMethods = "postProjectTest")
    public void postIssueInProjectTest(){
        issue = new Issue();
        issue.setSummary("TestApiTask");
        issue.setProject(project);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(issue)
                .when()
                .post("/issues");

        response.then()
                .contentType(ContentType.JSON)
                .body("id",notNullValue())
                .body("$type", equalTo("Issue"))
                .statusCode(200);

        issue.setId(response.path("id"));
    }

    @Test(dependsOnMethods = "postIssueInProjectTest")
    public void validateIssueTest() throws IOException{
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/test/resources/fields.properties"));

        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("fields",properties.getProperty("fields_issue"))
                .when()
                .get(String.format("/issues/%s",issue.getId()));

        response.then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schema/issue_schema.json"));
    }

    @Test(dependsOnMethods = "validateIssueTest")
    public void deleteIssueTest(){
        Response response = given()
                .when()
                .delete(String.format("/issues/%s",issue.getId()));

        response.then().statusCode(200);
    }

    @Test(dependsOnMethods = "postProjectTest")
    public void postAgileBoardTest(){
        User owner = new User();
        owner.setName("shtrung.se@yandex.ru");
        owner.setId("2-4");

        ArrayList<Project> projects = new ArrayList<>();
        projects.add(project);

        agiles = new Agiles();
        agiles.setName("TestCardApi");
        agiles.setOwner(owner);
        agiles.setProjects(projects);

        Response response = given()
                .queryParam("template","scrum")
                .contentType(ContentType.JSON)
                .body(agiles)
                .when()
                .post("/agiles");

        response.then()
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("$type", equalTo("Agile"))
                .statusCode(200);

        agiles.setId(response.path("id"));
    }

    @Test(dependsOnMethods = "postAgileBoardTest")
    public void validateAgileBoard() throws IOException{
        Properties properties = new Properties();
        properties.load(new FileInputStream("src/test/resources/fields.properties"));


        Response response = given()
                .contentType(ContentType.JSON)
                .queryParam("fields", properties.getProperty("fields_agiles"))
                .when()
                .get(String.format("/agiles/%s",agiles.getId()));


        response.then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schema/agiles_schema.json"));
    }

    @Test(dependsOnMethods = "validateAgileBoard")
    public void deleteAgileTest(){
      Response response = given()
              .when()
              .delete(String.format("/agiles/%s",agiles.getId()));

      response.then().statusCode(200);
    }


    @Test(dependsOnMethods = "postProjectTest")
    public void postArticlesTest(){
        articles = new Articles();
        articles.setProject(project);
        articles.setSummary("ArticlesTestApi");
        articles.setContent("Content");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(articles)
                .when()
                .post("/articles");

        response.then()
                .contentType(ContentType.JSON)
                .body("id",notNullValue())
                .body("$type", equalTo("Article"))
                .statusCode(200);

        articles.setId(response.path("id"));
    }

    @Test(dependsOnMethods = "postArticlesTest")
    public void deleteArticlesTest(){
        Response response = given()
                .when()
                .delete(String.format("/articles/%s",articles.getId()));

        response.then().statusCode(200);
    }
}
