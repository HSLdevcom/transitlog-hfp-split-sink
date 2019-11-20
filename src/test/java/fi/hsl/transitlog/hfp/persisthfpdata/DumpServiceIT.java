package fi.hsl.transitlog.hfp.persisthfpdata;

import config.H2Configuration;
import config.PulsarConfiguration;
import fi.hsl.Main;
import fi.hsl.transitlog.hfp.domain.VehiclePosition;
import fi.hsl.transitlog.hfp.domain.repositories.VehiclePositionRepository;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;
import utils.FileReader;
import utils.MqttPublisher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.List;

@ActiveProfiles(value = "integration-test")
@TestPropertySource(value = "classpath:/application.properties")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {H2Configuration.class, Main.class, PulsarConfiguration.class})
@DependsOn(value = {"pulsarApplication"})
class DumpServiceIT {

    private VehiclePositionRepository vehiclePositionRepository;
    private FileReader fileReader;
    private MqttPublisher mqttPublisher;
    private String mqttBrokerHost;
    private GenericContainer hfpMqttGateway;

    @Autowired
    DumpServiceIT(VehiclePositionRepository vehiclePositionRepository) {
        this.vehiclePositionRepository = vehiclePositionRepository;
        this.fileReader = new FileReader();
    }

    @Rule
    public GenericContainer mqttBroker = new GenericContainer<>("eclipse-mosquitto:1.6.3");
    @Rule
    public GenericContainer hfpParser = new GenericContainer<>("hsldevcom/transitdata-hfp-parser:1")
            .withEnv("PULSAR_CONSUMER_TOPIC", "persistent://public/default/test-hfp-raw")
            .withEnv("PULSAR_PRODUCER_TOPIC", "persistent://public/default/test-hfp-parsed")
            .withEnv("PULSAR_CONSUMER_SUBSCRIPTION", "test-hfp-parser-sub");

    @BeforeEach
    public void setupTestContainers() {
        // setup and start mqtt broker container
        this.mqttBroker.setNetworkMode("host");
        this.mqttBroker.start();

        // setup and start mqtt gateway container
        this.mqttBrokerHost = "tcp://"+ this.mqttBroker.getContainerIpAddress() +":1883";
        this.hfpMqttGateway = new GenericContainer<>("hsldevcom/mqtt-pulsar-gateway:1")
                .withEnv("MQTT_TOPIC", "/hfp/v2/#")
                .withEnv("PULSAR_PRODUCER_TOPIC", "persistent://public/default/test-hfp-raw")
                .withEnv("MQTT_BROKER_HOST", this.mqttBrokerHost);
        this.hfpMqttGateway.setNetworkMode("host");
        this.hfpMqttGateway.start();

        // setup and start hfp parser container
        this.hfpParser.setNetworkMode("host");
        this.hfpParser.start();
        this.mqttPublisher = new MqttPublisher(this.mqttBrokerHost);
    }

    @Test
    void dump() {
        String topic = "/hfp/v2/journey/ongoing/vp/bus/0030/00053/1074/2/Hakaniemi/10:10/1411119/4/60;25/20/77/01";
        String payload = this.fileReader.readHfpMqttPayload_1();
        try {
            this.mqttPublisher.publish(topic, payload);
            Thread.sleep(4000); // wait for pulsar to deliver the message and the dump service to write the message to H2
        } catch (Exception e) {
            System.out.println("Mqtt publish failed");
        }

        // TODO fix findAll throws InvalidDataAccessResourceUsageException (could not prepare statement...)
        List<VehiclePosition> allEvents = this.vehiclePositionRepository.findAll();

        //TODO assertoi menikö ja mitä meni kantaan
        assertNotSame(allEvents.size(), 0);
    }
}