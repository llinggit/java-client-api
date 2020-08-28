package com.marklogic.client.test.dataservices;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.marklogic.client.dataservices.IOEndpoint;
import com.marklogic.client.dataservices.InputEndpoint;
import com.marklogic.client.document.JSONDocumentManager;
import com.marklogic.client.io.JacksonHandle;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ErrorListenerInputEndpointTest {

    static ObjectNode apiObj;
    static String apiName = "errorListenerBulkIOInputCaller.api";
    static String scriptPath;
    static String apiPath;
    static JSONDocumentManager docMgr;
    int counter = 0;

    @BeforeClass
    public static void setup() throws Exception {
        docMgr = IOTestUtil.db.newJSONDocumentManager();
        apiObj = IOTestUtil.readApi(apiName);
        scriptPath = IOTestUtil.getScriptPath(apiObj);
        apiPath = IOTestUtil.getApiPath(scriptPath);
        IOTestUtil.load(apiName, apiObj, scriptPath, apiPath);
    }
    @Test
    public void bulkInputEndpointTestWithRetry() {

        counter = 0;
        String endpointState = "{\"next\":" + 1 + "}";
        String workUnit      = "{\"max\":10,\"collection\":\"bulkInputTest_1\"}";

        String endpointState1 = "{\"next\":" + 1 + "}";
        String workUnit1      = "{\"max\":10,\"collection\":\"bulkInputTest_2\"}";

        InputEndpoint loadEndpt = InputEndpoint.on(IOTestUtil.db, new JacksonHandle(apiObj));
        IOEndpoint.CallContext[] callContextArray = {loadEndpt.newCallContext()
                .withEndpointState(new ByteArrayInputStream(endpointState.getBytes()))
                .withWorkUnit(new ByteArrayInputStream(workUnit.getBytes())), loadEndpt.newCallContext()
                .withEndpointState(new ByteArrayInputStream(endpointState1.getBytes()))
                .withWorkUnit(new ByteArrayInputStream(workUnit1.getBytes()))};
        InputEndpoint.BulkInputCaller loader = loadEndpt.bulkCaller(callContextArray);

        InputEndpoint.BulkInputCaller.ErrorListener errorListener =
                (retryCount, throwable, callContext, input)
                        -> IOEndpoint.BulkIOEndpointCaller.ErrorDisposition.RETRY;
        loader.setErrorListener(errorListener);

        Stream<InputStream> input         = Stream.of(
                IOTestUtil.asInputStream("{\"docNum\":1, \"docName\":\"doc1\"}"),
                IOTestUtil.asInputStream("{\"docNum\":2, \"docName\":\"doc2\"}"),
                IOTestUtil.asInputStream("{\"docNum\":3, \"docName\":\"doc3\"}"),
                IOTestUtil.asInputStream("{\"docNum\":4, \"docName\":\"doc4\"}"),
                IOTestUtil.asInputStream("{\"docNum\":5, \"docName\":\"doc5\"}"),
                IOTestUtil.asInputStream("{\"docNum\":6, \"docName\":\"doc6\"}"),
                IOTestUtil.asInputStream("{\"docNum\":7, \"docName\":\"doc7\"}"),
                IOTestUtil.asInputStream("{\"docNum\":8, \"docName\":\"doc8\"}"),
                IOTestUtil.asInputStream("{\"docNum\":9, \"docName\":\"doc9\"}"),
                IOTestUtil.asInputStream("{\"docNum\":10, \"docName\":\"doc10\"}"),
                IOTestUtil.asInputStream("{\"docNum\":11, \"docName\":\"doc11\"}"),
                IOTestUtil.asInputStream("{\"docNum\":12, \"docName\":\"doc12\"}"),
                IOTestUtil.asInputStream("{\"docNum\":13, \"docName\":\"doc13\"}"),
                IOTestUtil.asInputStream("{\"docNum\":14, \"docName\":\"doc14\"}"),
                IOTestUtil.asInputStream("{\"docNum\":15, \"docName\":\"doc15\"}"),
                IOTestUtil.asInputStream("{\"docNum\":16, \"docName\":\"doc16\"}")
        );
        input.forEach(loader::accept);
        loader.awaitCompletion();
        checkDocuments("bulkInputTest_1");
        checkDocuments("bulkInputTest_2");
        assertTrue("Number of documents written not as expected." + counter, counter >= 7);
    }

    @Test
    public void bulkInputEndpointTestWithSkip() {

        counter = 0;
        String endpointState = "{\"next\":" + 1 + "}";
        String workUnit      = "{\"max\":10,\"collection\":\"bulkInputTest_1\"}";

        String endpointState1 = "{\"next\":" + 1 + "}";
        String workUnit1      = "{\"max\":10,\"collection\":\"bulkInputTest_2\"}";

        InputEndpoint loadEndpt = InputEndpoint.on(IOTestUtil.db, new JacksonHandle(apiObj));
        IOEndpoint.CallContext[] callContextArray = {loadEndpt.newCallContext()
                .withEndpointState(new ByteArrayInputStream(endpointState.getBytes()))
                .withWorkUnit(new ByteArrayInputStream(workUnit.getBytes())), loadEndpt.newCallContext()
                .withEndpointState(new ByteArrayInputStream(endpointState1.getBytes()))
                .withWorkUnit(new ByteArrayInputStream(workUnit1.getBytes()))};
        InputEndpoint.BulkInputCaller loader = loadEndpt.bulkCaller(callContextArray);

        InputEndpoint.BulkInputCaller.ErrorListener errorListener =
                (retryCount, throwable, callContext, input)
                        -> IOEndpoint.BulkIOEndpointCaller.ErrorDisposition.SKIP_CALL;
        loader.setErrorListener(errorListener);

        Stream<InputStream> input         = Stream.of(
                IOTestUtil.asInputStream("{\"docNum\":1, \"docName\":\"doc1\"}"),
                IOTestUtil.asInputStream("{\"docNum\":2, \"docName\":\"doc2\"}"),
                IOTestUtil.asInputStream("{\"docNum\":3, \"docName\":\"doc3\"}"),
                IOTestUtil.asInputStream("{\"docNum\":4, \"docName\":\"doc4\"}"),
                IOTestUtil.asInputStream("{\"docNum\":5, \"docName\":\"doc5\"}"),
                IOTestUtil.asInputStream("{\"docNum\":6, \"docName\":\"doc6\"}"),
                IOTestUtil.asInputStream("{\"docNum\":7, \"docName\":\"doc7\"}"),
                IOTestUtil.asInputStream("{\"docNum\":8, \"docName\":\"doc8\"}"),
                IOTestUtil.asInputStream("{\"docNum\":9, \"docName\":\"doc9\"}"),
                IOTestUtil.asInputStream("{\"docNum\":10, \"docName\":\"doc10\"}"),
                IOTestUtil.asInputStream("{\"docNum\":11, \"docName\":\"doc11\"}"),
                IOTestUtil.asInputStream("{\"docNum\":12, \"docName\":\"doc12\"}"),
                IOTestUtil.asInputStream("{\"docNum\":13, \"docName\":\"doc13\"}"),
                IOTestUtil.asInputStream("{\"docNum\":14, \"docName\":\"doc14\"}"),
                IOTestUtil.asInputStream("{\"docNum\":15, \"docName\":\"doc15\"}"),
                IOTestUtil.asInputStream("{\"docNum\":16, \"docName\":\"doc16\"}")
        );
        input.forEach(loader::accept);
        loader.awaitCompletion();
        checkDocuments("bulkInputTest_1");
        checkDocuments("bulkInputTest_2");
        assertTrue("Number of documents written not as expected." + counter, counter >= 4);
    }

    @Test
    public void bulkInputEndpointTestWithStop() {

        counter = 0;
        String endpointState = "{\"next\":" + 1 + "}";
        String workUnit      = "{\"max\":10,\"collection\":\"bulkInputTest_1\"}";

        String endpointState1 = "{\"next\":" + 1 + "}";
        String workUnit1      = "{\"max\":10,\"collection\":\"bulkInputTest_2\"}";

        InputEndpoint loadEndpt = InputEndpoint.on(IOTestUtil.db, new JacksonHandle(apiObj));
        IOEndpoint.CallContext[] callContextArray = {loadEndpt.newCallContext()
                .withEndpointState(new ByteArrayInputStream(endpointState.getBytes()))
                .withWorkUnit(new ByteArrayInputStream(workUnit.getBytes())), loadEndpt.newCallContext()
                .withEndpointState(new ByteArrayInputStream(endpointState1.getBytes()))
                .withWorkUnit(new ByteArrayInputStream(workUnit1.getBytes()))};
        InputEndpoint.BulkInputCaller loader = loadEndpt.bulkCaller(callContextArray);

        InputEndpoint.BulkInputCaller.ErrorListener errorListener =
                (retryCount, throwable, callContext, input)
                        -> IOEndpoint.BulkIOEndpointCaller.ErrorDisposition.STOP_ALL_CALLS;
        loader.setErrorListener(errorListener);

        Stream<InputStream> input         = Stream.of(
                IOTestUtil.asInputStream("{\"docNum\":1, \"docName\":\"doc1\"}"),
                IOTestUtil.asInputStream("{\"docNum\":2, \"docName\":\"doc2\"}"),
                IOTestUtil.asInputStream("{\"docNum\":3, \"docName\":\"doc3\"}"),
                IOTestUtil.asInputStream("{\"docNum\":4, \"docName\":\"doc4\"}"),
                IOTestUtil.asInputStream("{\"docNum\":5, \"docName\":\"doc5\"}"),
                IOTestUtil.asInputStream("{\"docNum\":6, \"docName\":\"doc6\"}"),
                IOTestUtil.asInputStream("{\"docNum\":7, \"docName\":\"doc7\"}"),
                IOTestUtil.asInputStream("{\"docNum\":8, \"docName\":\"doc8\"}"),
                IOTestUtil.asInputStream("{\"docNum\":9, \"docName\":\"doc9\"}"),
                IOTestUtil.asInputStream("{\"docNum\":10, \"docName\":\"doc10\"}"),
                IOTestUtil.asInputStream("{\"docNum\":11, \"docName\":\"doc11\"}"),
                IOTestUtil.asInputStream("{\"docNum\":12, \"docName\":\"doc12\"}"),
                IOTestUtil.asInputStream("{\"docNum\":13, \"docName\":\"doc13\"}"),
                IOTestUtil.asInputStream("{\"docNum\":14, \"docName\":\"doc14\"}"),
                IOTestUtil.asInputStream("{\"docNum\":15, \"docName\":\"doc15\"}"),
                IOTestUtil.asInputStream("{\"docNum\":16, \"docName\":\"doc16\"}")
        );
        input.forEach(loader::accept);
        loader.awaitCompletion();
        checkDocuments("bulkInputTest_1");
        checkDocuments("bulkInputTest_2");
        assertTrue("Number of documents written not as expected." + counter, counter >= 1);
    }

    @AfterClass
    public static void cleanup(){
        IOTestUtil.modMgr.delete(scriptPath, apiPath);
        for(int i=2; i<9; i++) {
            String uri = "/marklogic/ds/test/bulkInputCaller/bulkInputTest_1/" +i+".json";
            docMgr.delete(uri);
            uri = "/marklogic/ds/test/bulkInputCaller/bulkInputTest_2/" +i+".json";
            docMgr.delete(uri);
        }
    }

    private void checkDocuments(String collection) {

        for(int i=2; i<9; i++) {
            String uri = "/marklogic/ds/test/bulkInputCaller/"+collection+"/"+i+".json";
            if(docMgr.exists(uri)!=null) {
                counter++;
            }
        }
    }
}

