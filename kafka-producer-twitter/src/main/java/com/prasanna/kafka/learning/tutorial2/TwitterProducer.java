package com.prasanna.kafka.learning.tutorial2;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {
    Logger logger= LoggerFactory.getLogger(TwitterProducer.class.getName());

    String consumerKey="rFj9JE01ulSnODViH8A42rTZA";
    String consumerSecret="5p9H6ybk5KuHqkLn0Z7PX48w9e78b243O0i8FhVSfJVMp18wFA";
    String token="1333678850664906752-oR0ZX2Cadglk4q5iDjRqxllgvvl92c";
    String secret="MgIX56Osf98DAigCeqR8dVYNRCyEbqwEqQQb2TbHhgw5n";

    List<String> terms = Lists.newArrayList("ipl","badminton","politics","india");

    public TwitterProducer(){}
    public static void main(String[] args) {
        new TwitterProducer().run();

        }
    public void run(){

        logger.info("setup");

        //Setting up blocking queues
        BlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>(100000);
        //create twitter client
        Client client=createTwitterClient(msgQueue);
        // Attempts to establish a connection.
        client.connect();
        //create kafka producer
        KafkaProducer<String,String> producer=createKafkaProducer();
        //loop tweets into producer
        //create a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            logger.info("stopping application...");
            logger.info("shutting down client from twitter..");
            client.stop();
            logger.info("closing producer");
            producer.close();
            logger.info("Done!");
        }));
        // on a different thread, or multiple different threads....
        while (!client.isDone()) {
            String msg = null;
            try {
                msg = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }
            if(msg!=null){
                logger.info(msg);
                producer.send(new ProducerRecord<>("twitter_tweets", null, msg), new Callback() {
                    //@Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if(e!=null){
                            logger.error("Smthng bad happened!",e);
                        }
                    }
                });
            }
        }
        logger.info("End of application");
    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue){


        /** Declare the host you want to connect to, the endpoint, and authentication (basic auth or oauth) */
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();

        hosebirdEndpoint.trackTerms(terms);

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(consumerKey,consumerSecret,
                token, secret);

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        Client hosebirdClient = builder.build();
        return hosebirdClient;


    }
    public KafkaProducer<String,String> createKafkaProducer(){
        String bootstrapServers="127.0.0.1:9092";
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        //Enable idempotent producer
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,"true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG,"all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG,Integer.toString(Integer.MAX_VALUE));
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION,"5");

        //high throughput producer(at the expense of a bit of latency and some cpu operations
        properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG,"snappy");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG,"20");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG,Integer.toString(32*1024));

        //create producer
        KafkaProducer<String,String> producer =new KafkaProducer<String,String>(properties);
        return producer;

    }
}
