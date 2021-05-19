package tutorial1;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerDemokeys {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        final Logger logger= LoggerFactory.getLogger(ProducerDemokeys.class);

        //create producer properties
        String bootstrapServers="127.0.0.1:9092";
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());


        //create producer
        KafkaProducer<String,String> producer =new KafkaProducer<String,String>(properties);


        for(int i=0;i<10;i++){
            String topic="second_topic";
            String value="Hello world:"+Integer.toString(i);
            String key="Id_"+Integer.toString(i);
            //create producer record
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic,key,value);
            //send data into producer
            logger.info("key: "+key);
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    //executes everytime data is successfully sent or an exception occurs
                    if (e == null) {
                        logger.info(" \n Received new Metadata: " + recordMetadata.topic() + "\n"
                                + "Partition: " + recordMetadata.partition()
                                + "\n" + "offeset:" + recordMetadata.offset() + "\n"
                                + "Timestamp:" + recordMetadata.timestamp());
                    } else {
                        logger.error("error while producing: " + e);
                    }

                }
            }).get(); //blocks the send
        }
        //flush data
        producer.flush();
        //flush and close producer
        producer.close();
    }
}
