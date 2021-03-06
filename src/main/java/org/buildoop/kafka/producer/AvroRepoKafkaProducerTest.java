package org.buildoop.kafka.producer;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

import com.linkedin.camus.etl.kafka.coders.KafkaAvroMessageEncoder;

public class AvroRepoKafkaProducerTest 
{
    public static void main(String[] args) throws Exception	
    {
    	if (args.length != 1) {
    		  System.err.println("Use: java -jar avroKafkatestProducer.jar <configFile>"); 
    	      System.exit(1);
    	    }
    	File configuration= new File(args[0]);
        if (!configuration.canRead()) {
            System.err.println("Cannot read file: " + configuration);
            System.exit(1);
         }
        
        Properties props = new Properties();
        props.load(new BufferedInputStream(new FileInputStream(configuration)));    	
    	
    	try	{
    		
    		String avroSchemaFileName = props.getProperty("avro.schema.file");
    		String topic = props.getProperty("kafka.topic");
    		    		
    		File avroSchemaFile = new File(avroSchemaFileName);    		
	
	    	ProducerConfig config = new ProducerConfig(props);
	
	        Producer<String, byte[]> producer = new Producer<String, byte[]>(config);
	
	        Record record = fillRecord(fillAvroTestSchema(avroSchemaFile));
	        byte[] avroRecord = encodeMessage(topic,record,props);
	        
	        System.out.println();
	        
	        // Print ID received from avro schema repo server
	        for (int n = 1;n <= 4 ; n++){
	        	System.out.print(avroRecord[n]);
	        }
	        System.out.println();
	        
	        //Send message to kafka brokers
	        KeyedMessage<String, byte[]> data = new KeyedMessage<String, byte[]>(topic, avroRecord);
            producer.send(data);
	    	
			System.out.println();
			producer.close();
    	}	
    	catch (IOException e)
    	{
    	e.printStackTrace();		
    	}
    }
    
    @SuppressWarnings("deprecation")
	private static Schema fillAvroTestSchema(File jsonSchemaFile) throws IOException{
     	//Schema.Parser schemaParser = Schema.Parser();
    	return Schema.parse(jsonSchemaFile);
    }
    
    private static Record fillRecord(Schema schema){
		Record record = new Record(schema);
		record.put("test","Hello World");		    	
    	return record;
    }
    
    private static byte[] encodeMessage(String topic, IndexedRecord record, Properties props){
    	KafkaAvroMessageEncoder encoder = new KafkaAvroMessageEncoder(topic, null);
    	encoder.init(props, topic);
    	return encoder.toBytes(record);
    }
}
