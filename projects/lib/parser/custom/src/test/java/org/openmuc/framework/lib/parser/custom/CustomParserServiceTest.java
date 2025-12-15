package org.openmuc.framework.lib.parser.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.openmuc.framework.data.DoubleValue;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.IntValue;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.datalogger.spi.LoggingRecord;
import org.openmuc.framework.parser.spi.SerializationException;

public class CustomParserServiceTest {

    @Test
    public void testSerialize() {
        CustomParserServiceImpl parser = new CustomParserServiceImpl();

        String channelId = "bms1_cell1_R";
        long timestamp = 1234567890000L;
        Record record = new Record(new DoubleValue(0.003), timestamp, Flag.VALID);
        LoggingRecord loggingRecord = new LoggingRecord(channelId, record);

        byte[] result = parser.serialize(loggingRecord);
        String json = new String(result);

        // Expected: {"bms_1":[{"ts":1234567890000,"values":{"cell1_R":0.003}}]}
        // Note: JSON object key order may vary, so we check for components
        assertTrue(json.contains("\"bms_1\""), "JSON should contain bms_1 key");
        assertTrue(json.contains("\"ts\":" + timestamp), "JSON should contain timestamp");
        assertTrue(json.contains("\"cell1_R\":0.003"), "JSON should contain cell1_R value");
        assertTrue(json.contains("\"values\""), "JSON should contain values object");
    }

    @Test
    public void testSerializeWithDifferentId() {
        CustomParserServiceImpl parser = new CustomParserServiceImpl();

        // Test with another ID format to verify regex
        String channelId = "str1_cell1_R";
        long timestamp = 9876543210000L;
        Record record = new Record(new DoubleValue(1.23), timestamp, Flag.VALID);
        LoggingRecord loggingRecord = new LoggingRecord(channelId, record);

        byte[] result = parser.serialize(loggingRecord);
        String json = new String(result);

        // Expected: {"str_1":[{"ts":9876543210000,"values":{"cell1_R":1.23}}]}
        assertTrue(json.contains("\"str_1\""), "JSON should contain str_1 key");
        assertTrue(json.contains("\"ts\":" + timestamp), "JSON should contain timestamp");
        assertTrue(json.contains("\"cell1_R\":1.23"), "JSON should contain cell1_R value");
        assertTrue(json.contains("\"values\""), "JSON should contain values object");
    }

    @Test
    public void testSerializeMultipleChannels() throws SerializationException {
        CustomParserServiceImpl parser = new CustomParserServiceImpl();

        // Create multiple records with same prefix (str1)
        long timestamp = 1483228800000L;

        List<LoggingRecord> records = new ArrayList<>();
        records.add(new LoggingRecord("str1_cell1_R", new Record(new IntValue(300), timestamp, Flag.VALID)));
        records.add(new LoggingRecord("str1_cell1_V", new Record(new IntValue(205), timestamp, Flag.VALID)));
        records.add(new LoggingRecord("str1_cell1_T", new Record(new IntValue(254), timestamp, Flag.VALID)));

        byte[] result = parser.serialize(records);
        String json = new String(result);

        // Expected:
        // {"str_1":[{"ts":1483228800000,"values":{"cell1_R":300,"cell1_V":205,"cell1_T":254}}]}
        // Note: JSON object key order may vary, so we check for components
        assertTrue(json.contains("\"str_1\""), "JSON should contain str_1 key");
        assertTrue(json.contains("\"ts\":" + timestamp), "JSON should contain timestamp");
        assertTrue(json.contains("\"cell1_R\":300"), "JSON should contain cell1_R value");
        assertTrue(json.contains("\"cell1_V\":205"), "JSON should contain cell1_V value");
        assertTrue(json.contains("\"cell1_T\":254"), "JSON should contain cell1_T value");
        assertTrue(json.contains("\"values\""), "JSON should contain values object");
    }
}
