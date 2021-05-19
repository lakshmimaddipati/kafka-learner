package tutorial1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoassignandseek {
    public static void main(String[] args) {
        Logger logger= LoggerFactory.getLogger(ConsumerDemoassignandseek.class.getName());
        String bootstrapservers="127.0.0.1:9092";
        String topic="second_topic";

        Properties properties=new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapservers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
        //properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");

        //create consumer
        KafkaConsumer<String,String> consumer=new KafkaConsumer<String, String>(properties);

        //assign and seek are mostly used to  replay the data or fetch a specific message
        TopicPartition partitiontoReadfrom = new TopicPartition(topic,0);
        long offesettoReadfrom=15L;
        consumer.assign(Arrays.asList(partitiontoReadfrom));
        consumer.seek(partitiontoReadfrom,offesettoReadfrom);

        int numberofmessagestoread=5;
        Boolean KeeponReading=true;
        int numberofmessagesreadsofar=0;
        //poll for new data
        while(KeeponReading){
            ConsumerRecords<String,String> records=consumer.poll(Duration.ofMillis(100));
            for(ConsumerRecord<String,String> record:records){
                numberofmessagesreadsofar+=1;
                logger.info("key: "+record.key()+" "+"value: "+record.value());
                logger.info("partition: "+record.partition()+" "+"offset: "+record.offset());
                if(numberofmessagesreadsofar>numberofmessagestoread){
                    KeeponReading=false;
                    break;
                }
            }
        }
        logger.info("Exiting the application!");
    }
}
