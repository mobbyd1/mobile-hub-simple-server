package esmocyp.mobile.hub.reasoning;

import com.google.common.io.Files;
import com.google.gson.Gson;
import esmocyp.mobile.hub.model.TemperatureType;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by ruhan on 25/03/18.
 */
@Service
public class ReasoningServiceFacade {

    private static final String BASE_URI = "http://www.semanticweb.org/sensors#";
    private static final String NAMED_MODEL = "http://www.semanticweb.org/sensors-data";
    private static final String STREAMING_URL = "http://www.semanticweb.org/sensors/stream";

    private static final String SERVICE_BASE_URL = "http://192.168.25.189:8080/api/reasoner/";

    private static final String TOKEN = "Bearer 9346d5a5-208e-4ee8-adba-7fc1e511e185";

    /**
     * Make a post call to init the reasoning service
     *
     */
    public void initService() throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(SERVICE_BASE_URL + "create" );
        buildHttpHeaders(httpPost);

        String tBox = getFileAsString("esmocyp-temperature.owl");
        String aBox = getFileAsString("esmocyp-temperature-data.rdf");
        String query = getQuery();

        Map<String, String> params = new HashMap<>();

        params.put("query", query);
        params.put("tbox", tBox);
        params.put("aBox", aBox);
        params.put("streamingURL", STREAMING_URL);
        params.put("namedModel", NAMED_MODEL);
        params.put("baseURI", BASE_URI);

        Gson gson = new Gson();
        String json = gson.toJson(params);

        httpPost.setEntity(new StringEntity(json));
        httpClient.execute(httpPost);
    }

    public void stream(
            String room
            , String sensorId
            , TemperatureType temperatureType ) throws IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(SERVICE_BASE_URL + "stream" );
        buildHttpHeaders(httpPost);

        String subject = BASE_URI + room;
        String predicate = BASE_URI + temperatureType.getPredicate();
        String object = BASE_URI + sensorId;

        Map<String, String> params = new HashMap<>();

        params.put("subject", subject);
        params.put("predicate", predicate);
        params.put("object", object);

        Gson gson = new Gson();
        String json = gson.toJson(params);

        httpPost.setEntity(new StringEntity(json));
        httpClient.execute(httpPost);
    }

    private String getFileAsString(String path) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(path).getFile());

        byte[] ontologyBytes = Files.toByteArray(file);
        return new String(ontologyBytes);
    }

    private String getQuery() {
        String queryBody = "REGISTER QUERY staticKnowledge AS "
                + "PREFIX :<" + BASE_URI + "> "
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "SELECT ?s ?t "
                + "FROM STREAM <" + STREAMING_URL + "> [RANGE 1s STEP 1s] "
                + "FROM <" + NAMED_MODEL + "> "
                + "WHERE { "
                + "?s a :SalaPegandoFogo ."
                + "} ";

        return queryBody;
    }

    private void buildHttpHeaders( HttpPost httpPost ) {
        httpPost.addHeader("Authorization", TOKEN);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
    }
}
