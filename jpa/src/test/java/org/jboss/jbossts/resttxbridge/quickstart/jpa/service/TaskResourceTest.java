package org.jboss.jbossts.resttxbridge.quickstart.jpa.service;

import static org.junit.Assert.*;

import java.io.File;

import javax.ws.rs.core.MediaType;

import org.codehaus.jettison.json.JSONArray;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.jbossts.star.util.TxSupport;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.Link;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author Gytis Trikleris
 * 
 */
@RunWith(Arquillian.class)
public class TaskResourceTest {

    private static final String ManifestMF = "Manifest-Version: 1.0\n" + "Dependencies: org.jboss.jts\n";

    public static final String TXN_MGR_URL = "http://localhost:8080/rest-tx/tx/transaction-manager";

    public static final String BASE_URL = "http://localhost:8080/rest-tx-bridge-quickstart-jpa-test/";

    private static final String USERNAME = "gytis";

    private static final String TASK_TITLE1 = "task1";

    private static final String TASK_TITLE2 = "task2";

    @Deployment
    public static WebArchive createDeployment() {
        MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class)
                .loadMetadataFromPom("pom.xml");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "rest-tx-bridge-quickstart-jpa-test.war")
                .addAsResource(new File("src/main/resources", "META-INF/persistence.xml"), "META-INF/persistence.xml")
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/rest-tx-bridge-quickstart-jpa-ds.xml"))
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/beans.xml"))
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/web.xml"))
                .addPackages(true, "org.jboss.jbossts.resttxbridge.quickstart.jpa").setManifest(new StringAsset(ManifestMF))
                .addAsLibraries(resolver.artifact("org.codehaus.jettison:jettison:1.3.1").resolveAsFiles())
                .addAsLibraries(resolver.artifact("org.jboss.jbossts:rest-tx-bridge:1.0-SNAPSHOT").resolveAsFiles())
                .addAsLibraries(resolver.artifact("org.jboss.narayana.rts:restat-util:5.0.0.M2-SNAPSHOT").resolveAsFiles());

        System.out.println(archive.toString(true));
        return archive;
    }

    @Test
    public void testCommit() throws Exception {
        System.out.println("===== testCommit =====");

        System.out.println("Starting transaction...");
        TxSupport txn = new TxSupport(TXN_MGR_URL);
        txn.startTx();

        // Create user and register task
        System.out.println("Creating task " + TASK_TITLE1 + " with user " + USERNAME);
        ClientResponse<String> response = new ClientRequest(BASE_URL + "tasks/" + USERNAME + "/" + TASK_TITLE1).addLink(
                new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null)).post(String.class);
        assertEquals(201, response.getStatus());

        System.out.println("Commiting transaction...");
        txn.commitTx();

        System.out.println("Getting all tasks...");
        response = new ClientRequest(BASE_URL + "tasks/" + USERNAME).accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray jsonArray = new JSONArray(response.getEntity());
        System.out.println("All tasks:");
        System.out.println(jsonArray);

        assertEquals(1, jsonArray.length());
        assertEquals(USERNAME, jsonArray.getJSONObject(0).getString("owner"));
        assertEquals(TASK_TITLE1, jsonArray.getJSONObject(0).getString("title"));
    }

    @Test
    public void testRollback() throws Exception {
        System.out.println("===== testRollback =====");

        System.out.println("Starting transaction...");
        TxSupport txn = new TxSupport(TXN_MGR_URL);
        txn.startTx();

        // Create user and register task
        System.out.println("Creating task " + TASK_TITLE1 + " with user " + USERNAME);
        ClientResponse<String> response = new ClientRequest(BASE_URL + "tasks/" + USERNAME + "/" + TASK_TITLE1).addLink(
                new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null)).post(String.class);
        assertEquals(201, response.getStatus());

        System.out.println("Rolling back transaction...");
        txn.rollbackTx();

        System.out.println("Getting all tasks...");
        response = new ClientRequest(BASE_URL + "tasks/" + USERNAME).get(String.class);
        JSONArray jsonArray = new JSONArray(response.getEntity());
        System.out.println("All tasks:");
        System.out.println(jsonArray);

        assertEquals(0, jsonArray.length());
    }

    @Test
    public void testCommitWithTwoTasks() throws Exception {
        System.out.println("===== testCommitWithTwoTasks =====");

        System.out.println("Starting transaction...");
        TxSupport txn = new TxSupport(TXN_MGR_URL);
        txn.startTx();

        System.out.println("Creating task " + TASK_TITLE1 + " with user " + USERNAME);
        ClientResponse<String> response = new ClientRequest(BASE_URL + "tasks/" + USERNAME + "/" + TASK_TITLE1).addLink(
                new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null)).post(String.class);
        assertEquals(201, response.getStatus());

        System.out.println("Creating task " + TASK_TITLE2);
        response = new ClientRequest(BASE_URL + "tasks/" + USERNAME + "/" + TASK_TITLE2).addLink(
                new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null)).post(String.class);
        assertEquals(201, response.getStatus());

        System.out.println("Commiting transaction...");
        txn.commitTx();

        System.out.println("Getting all tasks...");
        response = new ClientRequest(BASE_URL + "tasks/" + USERNAME).get(String.class);
        JSONArray jsonArray = new JSONArray(response.getEntity());
        System.out.println("All tasks:");
        System.out.println(jsonArray);

        assertEquals(2, jsonArray.length());
        assertEquals(USERNAME, jsonArray.getJSONObject(0).getString("owner"));
        assertEquals(TASK_TITLE1, jsonArray.getJSONObject(0).getString("title"));
        assertEquals(USERNAME, jsonArray.getJSONObject(1).getString("owner"));
        assertEquals(TASK_TITLE2, jsonArray.getJSONObject(1).getString("title"));
    }

    @Test
    public void testRollbackWithTwoTasks() throws Exception {
        System.out.println("===== testRollbackWithTwoTasks =====");

        System.out.println("Starting transaction...");
        TxSupport txn = new TxSupport(TXN_MGR_URL);
        txn.startTx();

        System.out.println("Creating task " + TASK_TITLE1 + " with user " + USERNAME);
        ClientResponse<String> response = new ClientRequest(BASE_URL + "tasks/" + USERNAME + "/" + TASK_TITLE1).addLink(
                new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null)).post(String.class);
        assertEquals(201, response.getStatus());

        System.out.println("Creating task " + TASK_TITLE2);
        response = new ClientRequest(BASE_URL + "tasks/" + USERNAME + "/" + TASK_TITLE2).addLink(
                new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null)).post(String.class);
        assertEquals(201, response.getStatus());

        System.out.println("Rolling back transaction...");
        txn.rollbackTx();

        System.out.println("Getting all tasks...");
        response = new ClientRequest(BASE_URL + "tasks/" + USERNAME).get(String.class);
        JSONArray jsonArray = new JSONArray(response.getEntity());
        System.out.println("All tasks:");
        System.out.println(jsonArray);

        assertEquals(0, jsonArray.length());
    }

    @After
    public void cleanup() throws Exception {
        new ClientRequest(BASE_URL + "tasks").delete();
        new ClientRequest(BASE_URL + "users").delete();
    }

}
