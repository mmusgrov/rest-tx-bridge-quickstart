package org.jboss.jbossts.resttxbridge.quickstart.combined;

import static org.junit.Assert.assertEquals;

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
public class CombinedTest {
    /**
     * Transaction manager`s URL.
     */
    public static final String TXN_MGR_URL = "http://localhost:8080/rest-tx/tx/transaction-manager";

    /**
     * Messaging service`s URL.
     */
    public static final String JMS_BASE_URL = "http://localhost:8080/rest-tx-bridge-quickstart-jms-1.0-SNAPSHOT/";

    /**
     * Tasks service`s URL.
     */
    public static final String JPA_BASE_URL = "http://localhost:8080/rest-tx-bridge-quickstart-jpa-1.0-SNAPSHOT/";

    public static final String USERNAME = "gytis";

    private static final String TASK_TITLE = "task";

    private static final String MESSAGE = "Task was created";

    /**
     * Deployment of messaging service.
     * 
     * @return war archive of messaging service.
     */
    @Deployment(name = "JMS")
    public static WebArchive createJMSDeployment() {
        WebArchive archive = getWebArchiveFromRepository("org.jboss.jbossts:rest-tx-bridge-quickstart-jms:war:1.0-SNAPSHOT");

        if (archive == null) {
            throw new RuntimeException("Archive not found.");
        }

        return archive;
    }

    /**
     * Deployment of tasks service.
     * 
     * @return was archive of tasks service.
     */
    @Deployment(name = "JPA")
    public static WebArchive createJPADeployment() {
        WebArchive archive = getWebArchiveFromRepository("org.jboss.jbossts:rest-tx-bridge-quickstart-jpa:war:1.0-SNAPSHOT");

        if (archive == null) {
            throw new RuntimeException("Archive not found.");
        }

        return archive;
    }

    /**
     * Clean up after each test.
     * 
     * @throws Exception
     */
    @After
    public void cleanup() throws Exception {
        new ClientRequest(JPA_BASE_URL + "tasks").delete();
        new ClientRequest(JPA_BASE_URL + "users").delete();
    }

    /**
     * Tests transaction commit.
     * 
     * 1. Starts transaction. 2. Creates user and task. 3. Sends message. 4. Commits transaction. 5. Gets all tasks of the
     * created user and asserts (should be only one task with the same name and user like in step 2). 6. Gets available message
     * and asserts (should be only one message with the same text like in step 3).
     * 
     * @throws Exception
     */
    @Test
    public void testCommit() throws Exception {
        System.out.println("===== testCommit =====");

        // 1. Starting transaction.
        System.out.println("Starting transaction...");
        TxSupport txn = new TxSupport(TXN_MGR_URL);
        txn.startTx();

        // 2. Creating user and task.
        System.out.println("Creating task '" + TASK_TITLE + "' with user '" + USERNAME + "'");
        ClientResponse<String> response = new ClientRequest(JPA_BASE_URL + "tasks/" + USERNAME + "/" + TASK_TITLE).addLink(
                new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null)).post(String.class);
        assertEquals(201, response.getStatus());

        // 3. Sending message.
        System.out.println("Sending message...");
        response = new ClientRequest(JMS_BASE_URL).queryParameter("message", MESSAGE)
                .addLink(new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null))
                .post(String.class);
        assertEquals(200, response.getStatus());

        // 4. Committing transaction.
        System.out.println("Commiting transaction...");
        txn.commitTx();

        // 5. Getting all messages.
        System.out.println("Getting all tasks...");
        response = new ClientRequest(JPA_BASE_URL + "tasks/" + USERNAME).accept(MediaType.APPLICATION_JSON).get(String.class);
        JSONArray jsonArray = new JSONArray(response.getEntity());
        System.out.println("All tasks:");
        System.out.println(jsonArray);
        assertEquals(1, jsonArray.length());
        assertEquals(USERNAME, jsonArray.getJSONObject(0).getString("owner"));
        assertEquals(TASK_TITLE, jsonArray.getJSONObject(0).getString("title"));

        // 6. Getting message.
        System.out.println("Getting message...");
        response = new ClientRequest(JMS_BASE_URL).get(String.class);
        System.out.println("Received message: " + response.getEntity());
        assertEquals(MESSAGE, response.getEntity());
    }

    /**
     * Tests transaction roll back.
     * 
     * 1. Starts transaction. 2. Creates user and task. 3. Sends message. 4. Rolls back transaction. 5. Gets all tasks of the
     * created user and asserts (should be an empty array). 6. Gets available message and asserts (should be null).
     * 
     * @throws Exception
     */
    @Test
    public void testRollback() throws Exception {
        System.out.println("===== testRollback =====");

        // 1. Starting transaction.
        System.out.println("Starting transaction...");
        TxSupport txn = new TxSupport(TXN_MGR_URL);
        txn.startTx();

        // 2. Creating user and task.
        System.out.println("Creating task '" + TASK_TITLE + "' with user '" + USERNAME + "'");
        ClientResponse<String> response = new ClientRequest(JPA_BASE_URL + "tasks/" + USERNAME + "/" + TASK_TITLE).addLink(
                new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null)).post(String.class);
        assertEquals(201, response.getStatus());

        // 3. Sending message.
        System.out.println("Sending message...");
        response = new ClientRequest(JMS_BASE_URL).queryParameter("message", MESSAGE)
                .addLink(new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null))
                .post(String.class);
        assertEquals(200, response.getStatus());

        // 4. Committing transaction.
        System.out.println("Commiting transaction...");
        txn.rollbackTx();

        // 5. Getting all messages.
        System.out.println("Getting all tasks...");
        response = new ClientRequest(JPA_BASE_URL + "tasks/" + USERNAME).get(String.class);
        JSONArray jsonArray = new JSONArray(response.getEntity());
        System.out.println("All tasks:");
        System.out.println(jsonArray);
        assertEquals(0, jsonArray.length());

        // 6. Getting message.
        System.out.println("Getting message...");
        response = new ClientRequest(JMS_BASE_URL).get(String.class);
        System.out.println("Received message: " + response.getEntity());
        assertEquals(null, response.getEntity());
    }

    /**
     * Gets all files of the maven artifact specified with coordinates and returns WebArchive instance of the first war file (it
     * should be only one).
     * 
     * @param coordinates
     * @return
     */
    private static WebArchive getWebArchiveFromRepository(String coordinates) {
        File[] files = DependencyResolvers.use(MavenDependencyResolver.class).artifact(coordinates).resolveAsFiles();

        for (File f : files) {
            String name = f.getName().toLowerCase();
            if (name.length() > 4 && name.length() < Integer.MAX_VALUE
                    && ".war".equals(name.substring((int) name.length() - 4))) {
                return ShrinkWrap.createFromZipFile(WebArchive.class, f);
            }
        }

        return null;
    }

}
