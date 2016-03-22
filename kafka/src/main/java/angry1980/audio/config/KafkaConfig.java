package angry1980.audio.config;

import angry1980.audio.dao.TrackDAO;
import angry1980.audio.similarity.TracksToCalculate;
import angry1980.audio.similarity.TracksToCalculateImpl;
import angry1980.audio.similarity.TracksToCalculateKafkaImpl;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Properties;

@Configuration
@Profile(value = {"KAFKA", "KAFKA_TRACKS"})
public class KafkaConfig {

    @Autowired
    private TrackDAO trackDAO;

    @Bean
    public Properties kafkaProducerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.LongSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    @Bean(destroyMethod = "close")
    public Producer<Long, String> kafkaProducer() {
        return new KafkaProducer<>(kafkaProducerProperties());
    }

    @Bean
    public Properties kafkaConsumerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.LongDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }

    @Bean
    @Profile("KAFKA_TRACKS")
    public TracksToCalculate tracksToCalculate(){
        return new TracksToCalculateKafkaImpl(trackDAO, kafkaConsumerProperties(), "tracks");
    }

}
