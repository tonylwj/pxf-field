package com.emc.greenplum.gpdb.hdfsconnector;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emc.greenplum.gpdb.hadoop.io.GPDBWritable;

public class CassandraExtensionTest extends GpxfUnit {

	private static List<Pair<String, Integer>> columnDefs = null;
	private static List<Pair<String, String>> extraParams = new ArrayList<Pair<String, String>>();

	private static final String TEST_TABLE = "DEMO";

	private static boolean enableTests = true;

	@BeforeClass
	public static void setupClass() throws Exception {

		Configuration conf = new Configuration(false);
		conf.addResource(new FileInputStream(
				"src/test/resources/cassandra.conf"));

		enableTests = conf.getBoolean("enable.tests", true);

		if (!enableTests) {
			System.out.println("Tests are disabled");
			return;
		}

		String partitioner = conf.get("cassandra.partitioner");
		String address = conf.get("cassandra.address");

		columnDefs = new ArrayList<Pair<String, Integer>>();

		columnDefs
				.add(new Pair<String, Integer>("recordkey", GPDBWritable.TEXT));
		columnDefs.add(new Pair<String, Integer>("name", GPDBWritable.TEXT));
		columnDefs
				.add(new Pair<String, Integer>("password", GPDBWritable.TEXT));

		extraParams.add(new Pair<String, String>("ADDRESS", address));
		extraParams.add(new Pair<String, String>("COLUMN-FAMILY", "Users"));
		extraParams.add(new Pair<String, String>("PARTITIONER", partitioner));
	}

	@AfterClass
	public static void cleanupClass() throws Exception {

	}

	@Test
	public void testSimpleTable() throws Exception {
		if (!enableTests) {
			return;
		}

		List<String> output = new ArrayList<String>();

		output.add("1,adam,pw");
		output.add("1234,scott,tiger");

		super.assertUnorderedOutput(new Path(TEST_TABLE), output);
	}

	@Override
	public List<Pair<String, String>> getExtraParams() {
		return extraParams;
	}

	@Override
	public Class<? extends IDataFragmenter> getFragmenterClass() {
		return CassandraFragmenter.class;
	}

	@Override
	public Class<? extends IHdfsFileAccessor> getAccessorClass() {
		return CassandraAccessor.class;
	}

	@Override
	public Class<? extends IFieldsResolver> getResolverClass() {
		return CassandraResolver.class;
	}

	@Override
	public List<Pair<String, Integer>> getColumnDefinitions() {
		return columnDefs;
	}
}
