package ru.practicum.kafka.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory = EncoderFactory.get();
    private BinaryEncoder encoder;

    @Override
    public byte[] serialize(String topic, SpecificRecordBase event) {
        if (event == null) {
            return null;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            encoder = encoderFactory.binaryEncoder(outputStream, encoder);
            DatumWriter<SpecificRecordBase> datumWriter = new SpecificDatumWriter<>(event.getSchema());
            datumWriter.write(event, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new SerializationException("Error on serialization topic=" + topic, e);
        }
    }
}
