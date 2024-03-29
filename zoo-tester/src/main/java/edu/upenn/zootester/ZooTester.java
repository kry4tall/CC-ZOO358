package edu.upenn.zootester;

import edu.upenn.zootester.ensemble.ZKHelper;
import edu.upenn.zootester.scenario.*;
import edu.upenn.zootester.util.AssertionFailureError;
import edu.upenn.zootester.util.Config;
import edu.upenn.zootester.util.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ZooTester {

    private static final Logger LOG = LoggerFactory.getLogger(ZooTester.class);

    public static void main(final String[] args) {
        try {
            final Config config = Config.parseArgs(args);
            LOG.info("Run zoo-tester use parameters: " + Arrays.toString(args));
            ZKHelper.setBasePort(config.getBasePort());
            final Scenario scenario;
            switch (config.getScenario()) {
                case "scenario-1":
                    scenario = new Scenario1();
                    break;
                case "scenario-2":
                    scenario = new Scenario2();
                    break;
                case "scenario-3":
                    scenario = new Scenario3();
                    break;
                case "scenario-4":
                    scenario = new Scenario4();
                    break;
                case "scenario-5":
                    scenario = new Scenario5();
                    break;
                case "scenario-6":
                    scenario = new Scenario6();
                    break;
                default:
                    LOG.error("Unknown scenario!");
                    throw new Exception("Unknown scenario");
            }
            scenario.init(config);
            scenario.execute();
        } catch (final ConfigException e) {
            LOG.error("Configuration exception", e);
            Config.showUsage();
        } catch (final AssertionFailureError e) {
            LOG.info("Assertion failure", e);
        } catch (final Exception e) {
            LOG.error("Exception failure", e);
        }
        System.exit(0);
    }
}
