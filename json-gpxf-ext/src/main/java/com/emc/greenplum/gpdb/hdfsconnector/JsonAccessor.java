package com.emc.greenplum.gpdb.hdfsconnector;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;

import com.emc.greenplum.gpdb.hdfsconnector.HDFSMetaData;
import com.emc.greenplum.gpdb.hdfsconnector.OneRow;
import com.emc.greenplum.gpdb.hdfsconnector.SplittableFileAccessor;
import com.gopivotal.mapred.input.JsonInputFormat;

public class JsonAccessor extends SplittableFileAccessor {

	private Text key = new Text();
	private NullWritable value = NullWritable.get();
	private String identifier = "";
	private boolean oneRecordPerLine = true;

	public JsonAccessor(HDFSMetaData meta) throws Exception {
		super(meta, new JsonInputFormat());

		if (meta.getParametersMap().containsKey("X-GP-IDENTIFIER")) {
			identifier = meta.getProperty("X-GP-IDENTIFIER");
		}

		if (meta.getParametersMap().containsKey("X-GP-ONERECORDPERLINE")) {
			oneRecordPerLine = Boolean.parseBoolean(meta.getProperty("X-GP-ONERECORDPERLINE"));
		}
	}

	@Override
	public OneRow LoadNextObject() throws IOException {

		if (this.reader.next(key, value)) {
			return new OneRow(null, key);
		}

		if (getNextSplit()) {
			if (this.reader.next(key, value)) {
				return new OneRow(null, key);
			}
			return null;
		}

		return null;
	}

	@Override
	protected Object getReader(JobConf conf, InputSplit split)
			throws IOException {
		conf.set(JsonInputFormat.RECORD_IDENTIFIER, identifier);

		if (oneRecordPerLine) {
			return new JsonInputFormat.SimpleJsonRecordReader(conf,
					(FileSplit) split);
		} else {
			return new JsonInputFormat.JsonRecordReader(conf, (FileSplit) split);
		}
	}
}
