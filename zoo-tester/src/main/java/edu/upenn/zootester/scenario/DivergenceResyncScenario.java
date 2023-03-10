package edu.upenn.zootester.scenario;

import edu.upenn.zootester.ensemble.ZKEnsemble;
import edu.upenn.zootester.ensemble.ZKProperty;
import edu.upenn.zootester.harness.SequentialConsistency;
import edu.upenn.zootester.util.Assert;
import edu.upenn.zootester.util.Config;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DivergenceResyncScenario implements Scenario {

    private static final Logger LOG = LoggerFactory.getLogger(DivergenceResyncScenario.class);

    private static final int TOTAL_SERVERS = 3;
    private static final List<String> KEYS = List.of("/key0", "/key1");
    private static final ZKProperty CONSISTENT_VALUES = new SequentialConsistency(KEYS,
            Set.of(Map.of("/key0", 0, "/key1", 1001),
                    Map.of("/key0", 1000, "/key1", 1001)));

    private final ZKEnsemble zkEnsemble = new ZKEnsemble(TOTAL_SERVERS);

    @Override
    public void init(final Config config) throws IOException {
        zkEnsemble.init();
    }

    @Override
    public void execute() throws Exception {
        try (final AutoCloseable cleanUp = () -> {
            zkEnsemble.stopEnsemble();
            zkEnsemble.tearDown();
        }) {
            zkEnsemble.startEnsemble();

            final int srvC = zkEnsemble.getLeader();

            // Create initial znodes
            zkEnsemble.handleRequest(srvC, (zk, serverId) -> {
                zk.create(KEYS.get(0), "0".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                zk.create(KEYS.get(1), "1".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            });
            zkEnsemble.stopAllServers();
        }
    }
}
