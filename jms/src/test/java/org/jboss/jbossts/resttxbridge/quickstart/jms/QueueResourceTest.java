package org.jboss.jbossts.resttxbridge.quickstart.jms;

import static org.junit.Assert.*;

import java.io.File;

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
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author Gytis Trikleris
 * 
 */
@RunWith(Arquillian.class)
public class QueueResourceTest {

    private static final String ManifestMF = "Manifest-Version: 1.0\n" + "Dependencies: org.jboss.jts\n";

    public static final String TXN_MGR_URL = "http://localhost:8080/rest-tx/tx/transaction-manager";

    public static final String BASE_URL = "http://localhost:8080/rest-tx-bridge-quickstart-jms-test/";

    public static final String MESSAGE = "Hello World";

    @Deployment
    public static WebArchive createDeployment() {
        MavenDependencyResolver resolver = DependencyResolvers.use(MavenDependencyResolver.class)
                .loadMetadataFromPom("pom.xml");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "rest-tx-bridge-quickstart-jms-test.war")
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/rest-tx-bridge-quickstart-jms.xml"))
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/beans.xml"))
                .addAsWebInfResource(new File("src/main/webapp", "WEB-INF/web.xml"))
                .addPackages(true, "org.jboss.jbossts.resttxbridge.quickstart.jms").setManifest(new StringAsset(ManifestMF))
                .addAsLibraries(resolver.artifact("org.jboss.jbossts:rest-tx-bridge:1.0-SNAPSHOT").resolveAsFiles())
                .addAsLibraries(resolver.artifact("org.jboss.narayana.rts:restat-util:5.0.0.M2-SNAPSHOT").resolveAsFiles());

        return archive;
    }

    @Test
    public void testCommit() throws Exception {
        System.out.println("===== testCommit =====");

        System.out.println("Starting transaction...");
        TxSupport txn = new TxSupport(TXN_MGR_URL);
        txn.startTx();

        System.out.println("Sending message...");
        ClientResponse<String> response = new ClientRequest(BASE_URL).queryParameter("message", MESSAGE)
                .addLink(new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null))
                .post(String.class);
        assertEquals(200, response.getStatus());

        System.out.println("Commiting transaction...");
        txn.commitTx();

        System.out.println("Getting message...");
        response = new ClientRequest(BASE_URL).get(String.class);
        System.out.println("Received message: " + response.getEntity());
        assertEquals(MESSAGE, response.getEntity());
    }

    @Test
    public void testRollback() throws Exception {
        System.out.println("===== testRollback =====");

        System.out.println("Starting transaction...");
        TxSupport txn = new TxSupport(TXN_MGR_URL);
        txn.startTx();

        System.out.println("Sending message...");
        ClientResponse<String> response = new ClientRequest(BASE_URL).queryParameter("message", MESSAGE)
                .addLink(new Link(TxSupport.PARTICIPANT_LINK, TxSupport.PARTICIPANT_LINK, txn.txUrl(), null, null))
                .post(String.class);
        assertEquals(200, response.getStatus());

        System.out.println("Rolling back transaction...");
        txn.rollbackTx();

        System.out.println("Getting message...");
        response = new ClientRequest(BASE_URL).get(String.class);
        System.out.println("Received message: " + response.getEntity());
        assertEquals(null, response.getEntity());
    }
}
