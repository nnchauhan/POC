package org.oiavorskyi.axondemo;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.viadeo.axonframework.eventhandling.cluster.ClassnameDynamicClusterSelectorFactory;
import com.viadeo.axonframework.eventhandling.cluster.ClusterFactory;
import com.viadeo.axonframework.eventhandling.cluster.ClusterSelectorFactory;
import com.viadeo.axonframework.eventhandling.terminal.kafka.ConsumerFactory;
import com.viadeo.axonframework.eventhandling.terminal.kafka.KafkaClusterListener;
import com.viadeo.axonframework.eventhandling.terminal.kafka.KafkaMetricHelper;
import com.viadeo.axonframework.eventhandling.terminal.kafka.KafkaTerminal;
import com.viadeo.axonframework.eventhandling.terminal.kafka.KafkaTerminalFactory;
import static com.viadeo.axonframework.eventhandling.terminal.kafka.KafkaTerminalFactory.from;
import com.viadeo.axonframework.eventhandling.terminal.kafka.PrefixTopicStrategy;
import com.viadeo.axonframework.eventhandling.terminal.kafka.TopicStatement;
import com.viadeo.axonframework.eventhandling.terminal.kafka.TopicStrategy;
import com.viadeo.axonframework.eventhandling.terminal.kafka.TopicStrategyFactory;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import kafka.consumer.ConsumerConnector;
import kafka.producer.KeyedMessage;
import org.axonframework.domain.EventMessage;
import org.axonframework.domain.GenericEventMessage;
import static org.axonframework.domain.GenericEventMessage.asEventMessage;
import org.axonframework.eventhandling.Cluster;
import org.axonframework.eventhandling.ClusteringEventBus;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.EventBusTerminal;
import org.axonframework.eventhandling.SimpleCluster;
import static org.oiavorskyi.axondemo.AxonConfig.KAFKA_PROPERTIES_MAP;
import static org.oiavorskyi.axondemo.AxonConfig.createClusterSelectorFactory;
import static org.oiavorskyi.axondemo.AxonConfig.createKafkaTerminalFactory;
import org.oiavorskyi.axondemo.api.CargoTrackingCommandMessage;


import java.util.*;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import static kafka.tools.StateChangeLogMerger.topic;
import org.axonframework.eventhandling.annotation.AnnotationEventListenerAdapter;

public class Application {

    public static final String DEFAULT_PROFILE = "default";
    private static final String[] VALID_PROFILES = new String[]{"production"};
    private static Logger log = LoggerFactory.getLogger(Application.class);

    public static String inputFromUser() {
        String msg = "";
        // KeyedMessage<Integer, String> keyedMsg=null;
        try {

            // String topic="mytopic";
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            msg = reader.readLine(); // Read message from console
            //Define topic name and message
            //  keyedMsg = new KeyedMessage<Integer, String>(topic, msg);
        } catch (Exception e) {
            System.out.println("Error at input");
        }

        return msg;
    }

    public static final ClusterFactory CLUSTER_FACTORY = new ClusterFactory() {
        @Override

        public Cluster create(String name) {
            return new SimpleCluster(name);
        }
    };

    public static KafkaTerminalFactory createKafkaTerminalFactory(final Map<String, String> properties) {
        return new KafkaTerminalFactory(from(properties));
    }
    private static final String PREFIX = "com.viadeo.axonframework.eventhandling.cluster";
    public static final ImmutableMap<String, String> KAFKA_PROPERTIES_MAP = ImmutableMap.<String, String>builder()
            // PRODUCER
            .put("metadata.broker.list", "localhost:9092")
            .put("request.required.acks", "1")
            .put("producer.type", "sync")
            //.put("serializer.class", "kafka.serializer.DefaultEncoder")
            //CONSUMER
            .put("zookeeper.connect", "localhost:2181")
            // this property will be overridden by the cluster
            .put("group.id", "0")
            // !important; without the following property then this suite is unstable (due to the process of the auto creation topic)
            .put("auto.offset.reset", "smallest")
            .build();

    public static class CustomEventMessage extends GenericEventMessage<String> {

        public CustomEventMessage(String payload) {
            super(payload);
        }
    }

   
//public void consume() {
//    
//    
//     Properties props = new Properties();
//          props.put("zookeeper.connect", "localhost:2181");
//          props.put("group.id", "testgroup");
//          props.put("zookeeper.session.timeout.ms", "400");
//          props.put("zookeeper.sync.time.ms", "300");
//          props.put("auto.commit.interval.ms", "1000");
//      ConsumerConfig conConfig = new ConsumerConfig(props);
//  ConsumerConnector consumerConnector = (ConsumerConnector) Consumer.createJavaConsumerConnector(conConfig);
//          //Key = topic name, Value = No. of threads for topic
//          Map<String, Integer> topicCount = new HashMap<String, Integer>();       
//          topicCount.put(topic, new Integer(1));
//         
//          //ConsumerConnector creates the message stream for each topic
//          Map<String, List<KafkaStream<byte[], byte[]>>> consumerStreams = consumerConnector.createMessageStreams(topicCount);         
//         
//          // Get Kafka stream for topic 'mytopic'
//          List<KafkaStream<byte[], byte[]>> kStreamList = consumerStreams.get(topic);
//          // Iterate stream using ConsumerIterator
//          for (final KafkaStream<byte[], byte[]> kStreams : kStreamList) {
//                 ConsumerIterator<byte[], byte[]> consumerIte = kStreams.iterator();
//                
//                 while (consumerIte.hasNext())
//                        System.out.println("Message consumed from topic [" + topic + "] : "+ new String(consumerIte.next().message()));              
//          }
//          //Shutdown the consumer connector
//          if (consumerConnector != null)   consumerConnector.shutdown();          
//    }
    
    
    
    
    
    
    
    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(Config.class);

        String executionProfile = identifyCurrentExecutionProfile();
        applyExecutionProfileToApplicationContext(executionProfile, context);

        context.refresh();

       // KafkaTerminal kt = createKafkaTerminalFactory(KAFKA_PROPERTIES_MAP).create();
        //kt.publish(eventMessage);
        final KafkaTerminalFactory terminalFactory = createKafkaTerminalFactory(KAFKA_PROPERTIES_MAP);

        TopicStrategyFactory currentTopicStrategyFactory = new TopicStrategyFactory() {
            @Override
            public TopicStrategy create() {
                final String prefix = UUID.randomUUID().toString();

                // set the generated prefix as property, TODO found a better way in order to pass this restriction to our consumer
                terminalFactory.setConsumerProperty(ConsumerFactory.CONSUMER_TOPIC_FILTER_REGEX, prefix + ".*");

                return new PrefixTopicStrategy(prefix);
            }
        };
        TopicStrategy currentTopicStrategy = currentTopicStrategyFactory.create();
        EventBusTerminal currentTerminal = terminalFactory.with(currentTopicStrategy).create();
        final String zkConnect = "localhost:2181";
        TopicStatement topicStatement = new TopicStatement(zkConnect);
       EventMessage eventMessage = new CustomEventMessage(inputFromUser());

        final String topic = currentTopicStrategy.getTopic(eventMessage);
        topicStatement.create(topic);
        System.out.println("topic created :" + topic);

        //topicStatement.create(topic);
        //  KafkaTerminal kt = createKafkaTerminalFactory(KAFKA_PROPERTIES_MAP).create();
        //return new ClusteringEventBus( createClusterSelectorFactory(PREFIX).create(),kt);
        //EventBus currentEventBus = new ClusteringEventBus(clusterSelectorFactory.create(),currentTerminal);
        Properties props = new Properties();
          props.put("zookeeper.connect", "localhost:2181");
          props.put("group.id", "testgroup");
          props.put("zookeeper.session.timeout.ms", "400");
          props.put("zookeeper.sync.time.ms", "300");
          props.put("auto.commit.interval.ms", "1000");
          ConsumerConfig conConfig = new ConsumerConfig(props);
          //consumerConnector = Consumer.createJavaConsumerConnector(conConfig);
          ConsumerFactory cf=new ConsumerFactory(conConfig);
          MetricRegistry mr=new MetricRegistry();
          KafkaMetricHelper kmh=new KafkaMetricHelper(mr,PREFIX);
        EventBus currentEventBus = new ClusteringEventBus(createClusterSelectorFactory(PREFIX).create(), currentTerminal);
       
        currentEventBus.subscribe(new AnnotationEventListenerAdapter(new KafkaClusterListener(cf, kmh, currentTopicStrategy,CLUSTER_FACTORY.create("Cluster1"),1)));
        
         currentEventBus.publish(asEventMessage(new CargoTrackingCommandMessage("START","testCargoId", "testCorrelationId", "someTimestamp").toString()));
      //  currentEventBus.publish(eventMessage);
        // eventBus.publish(asEventMessage(new ToDoItemCompletedEvent("todo1"))); 
        context.registerShutdownHook();

        log.info("Application has successfully started");

    }
    
    
    
    

    /**
     * Identifies execution profile to be used. Only Spring beans configured
     * within this profile or no profile at all will be loaded. This opens
     * possibility to switch between different environments without any code
     * changes.
     *
     * This method looks for a file with name "runtime.profile" in the directory
     * from where process was started and if it exists assumes first line in
     * this file as a name of profile.
     *
     * @return name of Spring profile to be used for execution of application
     */
    public static String identifyCurrentExecutionProfile() {
        String result = DEFAULT_PROFILE;

        log.debug("Identifying execution profile: working directory is {}",
                Paths.get("").toAbsolutePath().normalize().toString());

        Path pathToRuntimeProfileMarkerFile = FileSystems.getDefault().getPath("runtime.profile");
        boolean markerExists = Files.exists(pathToRuntimeProfileMarkerFile);

        if (markerExists) {
            try {
                List<String> values = Files.readAllLines(pathToRuntimeProfileMarkerFile,
                        Charset.defaultCharset());
                String profileName = values.get(0);
                log.debug("Identifying execution profile: found runtime.profile file with value "
                        + profileName);
                if (Arrays.binarySearch(VALID_PROFILES, profileName) >= 0) {
                    result = profileName;
                }
            } catch (IOException e) {
                // Ignore exception and assume default profile
            }
        } else {
            log.debug("Identifying execution profile: no runtime.profile file was found");
        }

        return result;
    }

    /**
     * Applies execution profile to Spring Application Context and registers
     * profile property so it could be used to add profile-specific property
     * files to the Environment.
     *
     * For property files to be actually added append PropertySource to the
     * @Configuration component like this:
     * <pre>
     *    @PropertySources( {
     * @PropertySource( "/my.properties" ),
     * @PropertySource( value = "/my-${execution.profile}.properties",
     * ignoreResourceNotFound = true ) } ) public class MyConfig { ... }
     * </pre>
     *
     * Make sure to use {code}ignoreResourceNotFound=true{code} as otherwise
     * Spring will throw exception when profile-specific property file is not
     * found.
     */
    public static void applyExecutionProfileToApplicationContext(String executionProfile,
            GenericApplicationContext ctx) {
        log.info("Identifying execution profile: {} execution profile was selected",
                executionProfile);
        ConfigurableEnvironment env = ctx.getEnvironment();
        env.setActiveProfiles(executionProfile);
        Map<String, Object> customProperties
                = Collections.singletonMap("execution.profile", (Object) executionProfile);
        env.getPropertySources().addFirst(new MapPropertySource("custom", customProperties));
        log.info("Identifying execution profile: *-{}.propeties files will be added to properties"
                + " resolution process", executionProfile);
    }

    @Configuration
    @ComponentScan({"org.oiavorskyi.axondemo"})
    @PropertySources({
        @PropertySource("/application.properties"),
        @PropertySource(value = "/application-${execution.profile}.properties",
                ignoreResourceNotFound = true)
    })
    public static class Config {

        @Bean
        public LocalValidatorFactoryBean validatorFactoryBean() {
            return new LocalValidatorFactoryBean();
        }

    }
}
