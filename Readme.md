# Current example shows how to use confluent schema registry JSON schema with kafka.
- For CI-CD integration used kafka-schema-registry-maven-plugin
- For contract testing please look at the test folder
- for running kafka with schema registry please look at the docker-compose file

current example covers only producer validation because KafkaJsonSchemaDeserializer has a bug if some put into kafka some wrong data,
consumer validation work via validation framework and use .DLT pattern for wrong data 