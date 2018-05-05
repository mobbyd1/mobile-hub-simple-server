package esmocyp.mobile.hub.reasoning;

import com.google.common.io.Files;
import com.google.gson.Gson;
import esmocyp.mobile.hub.model.StreamType;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruhan on 25/03/18.
 */
@Service
public class ReasoningServiceFacade {

    private static final String BASE_URI = "http://www.semanticweb.org/esmocyp#";
    private static final String NAMED_MODEL = "http://www.semanticweb.org/sensors-data";
    private static final String STREAMING_URL = "http://www.semanticweb.org/sensors/stream";

    private static final String SERVICE_BASE_URL = "http://192.168.0.5:8080/api/reasoner/";

    private static final String TOKEN = "Bearer a9c0c34e-1c67-4786-8959-03333323dca1";

    /**
     * Make a post call to init the reasoning service
     *
     */
    public void initService() throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(SERVICE_BASE_URL + "create" );
        buildHttpHeaders(httpPost);

        String query = getQuery();

        Map<String, Object> params = new HashMap<>();

        params.put("query", query);
//        params.put("tbox", tBox);
//        params.put("aBox", aBox);
        params.put("streamingURL", STREAMING_URL);
        params.put("namedModel", NAMED_MODEL);
        params.put("baseURI", BASE_URI);
        params.put("uuids", Collections.EMPTY_LIST);

        Gson gson = new Gson();
        String json = gson.toJson(params);

        httpPost.setEntity(new StringEntity(json));
        httpClient.execute(httpPost);
    }

    public void stream(
            String room
            , String sensorId
            , StreamType streamType) throws IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(SERVICE_BASE_URL + "stream" );
        buildHttpHeaders(httpPost);

        String subject = BASE_URI + room;
        String predicate = BASE_URI + streamType.getPredicate();
        String object = BASE_URI + sensorId;

        Map<String, String> params = new HashMap<>();

        params.put("subject", subject);
        params.put("predicate", predicate);
        params.put("object", object);

        Gson gson = new Gson();
        String json = gson.toJson(params);

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy:HH:mm:ss.sssss");
        Date now = new Date();

        System.out.println( dateFormat.format(now) + " Sent message: " + json );

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
                + "?s a :SalaEmPerigo ."
                + "} ";

        return queryBody;
    }

    private void buildHttpHeaders( HttpPost httpPost ) {
        httpPost.addHeader("Authorization", TOKEN);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
    }
}
