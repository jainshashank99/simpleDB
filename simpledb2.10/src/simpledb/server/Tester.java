package simpledb.server;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import simpledb.metadata.IndexInfo;
import simpledb.tx.Transaction;

public class Tester {

	public static void main(String[] args) {
		SimpleDB.init("studentdb");
		Map<String, IndexInfo> mappy = SimpleDB.mdMgr().getIndexInfo("student", new Transaction());
		Iterator<Entry<String, IndexInfo>> iter = mappy.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, IndexInfo> entry = iter.next();
            System.out.println(entry.getKey() + " " + entry.getValue());
        }
	}
}
