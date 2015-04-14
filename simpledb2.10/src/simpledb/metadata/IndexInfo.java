package simpledb.metadata;

import static java.sql.Types.INTEGER;
import static simpledb.file.Page.BLOCK_SIZE;
import simpledb.server.SimpleDB;
import simpledb.tx.Transaction;
import simpledb.record.*;
import simpledb.index.Index;
import simpledb.index.hash.HashIndex; 
import simpledb.index.btree.BTreeIndex; //in case we change to btree indexing


/**
 * The information about an index.
 * This information is used by the query planner in order to
 * estimate the costs of using the index,
 * and to obtain the schema of the index records.
 * Its methods are essentially the same as those of Plan.
 * @author Edward Sciore
 */
public class IndexInfo {
	//Shashank: Index type added
   private String idxname, idxtype, fldname;
   private Transaction tx;
   private TableInfo ti;
   private StatInfo si;
   
   /**
    * Creates an IndexInfo object for the specified index.
    * @param idxname the name of the index
    * @param tblname the name of the table
    * @param fldname the name of the indexed field
    * @param tx the calling transaction
    */
   public IndexInfo(String idxname, String idxtype, String tblname, String fldname,
                    Transaction tx) {
      this.idxname = idxname;
      this.idxtype = idxtype;
      this.fldname = fldname;
      this.tx = tx;
      ti = SimpleDB.mdMgr().getTableInfo(tblname, tx);
      si = SimpleDB.mdMgr().getStatInfo(tblname, ti, tx);
   }
   
   /**
    * Opens the index described by this object.
    * @return the Index object associated with this information
    */
   public Index open() {
      Schema sch = schema();
      //Shashank
      if (idxtype.equals("hash")){
    	  return new HashIndex(idxname, sch, tx);
      }else if (idxtype.equals("btree")){
    	  return new BTreeIndex(idxname, sch, tx);
      }else if (idxtype.equals("btreeinv")){
    	  //TODO: Include BTreeInvIndex
    	  System.out.println("IndexInfo: Index type mentioned is wrong. Creating BTreeIndex.");
    	  return new BTreeIndex(idxname, sch, tx);
    	  //return new BTreeInvIndex(idxname, sch, tx);
      }else{
    	  //Taking default as Hash
    	  System.out.println("IndexInfo: Index type mentioned is wrong. Taking as Hash as default.");
    	  return new HashIndex(idxname, sch, tx);
      }
   }
   
   /**
    * Estimates the number of block accesses required to
    * find all index records having a particular search key.
    * The method uses the table's metadata to estimate the
    * size of the index file and the number of index records
    * per block.
    * It then passes this information to the traversalCost
    * method of the appropriate index type,
    * which provides the estimate.
    * @return the number of block accesses required to traverse the index
    */
   public int blocksAccessed() {
      TableInfo idxti = new TableInfo("", schema());
      int rpb = BLOCK_SIZE / idxti.recordLength();
      int numblocks = si.recordsOutput() / rpb;
      //Shashank
      if (idxtype.equals("hash")){
    	  return HashIndex.searchCost(numblocks, rpb);
      }else if (idxtype.equals("btree")){
    	  return BTreeIndex.searchCost(numblocks, rpb);
      }else if (idxtype.equals("btreeinv")){
    	  //TODO: Include BTreeInvIndex
    	  System.out.println("IndexInfo: Index type mentioned is wrong. Creating BTreeIndex.");
    	  return BTreeIndex.searchCost(numblocks, rpb);
    	  //return BTreeInvIndex.searchCost(numblocks, rpb);
      }else{
    	  //Taking default as Hash
    	  System.out.println("IndexInfo: Index type mentioned is wrong. Taking as Hash as default.");
    	  return HashIndex.searchCost(numblocks, rpb);
      }
   }
   
   /**
    * Returns the estimated number of records having a
    * search key.  This value is the same as doing a select
    * query; that is, it is the number of records in the table
    * divided by the number of distinct values of the indexed field.
    * @return the estimated number of records having a search key
    */
   public int recordsOutput() {
      return si.recordsOutput() / si.distinctValues(fldname);
   }
   
   /** 
    * Returns the distinct values for a specified field 
    * in the underlying table, or 1 for the indexed field.
    * @param fname the specified field
    */
   public int distinctValues(String fname) {
      if (fldname.equals(fname))
         return 1;
      else 
         return Math.min(si.distinctValues(fldname), recordsOutput());
   }
   
   /**
    * Returns the schema of the index records.
    * The schema consists of the dataRID (which is
    * represented as two integers, the block number and the
    * record ID) and the dataval (which is the indexed field).
    * Schema information about the indexed field is obtained
    * via the table's metadata.
    * @return the schema of the index records
    */
   private Schema schema() {
      Schema sch = new Schema();
      sch.addIntField("block");
      sch.addIntField("id");
      if (ti.schema().type(fldname) == INTEGER)
         sch.addIntField("dataval");
      else {
         int fldlen = ti.schema().length(fldname);
         sch.addStringField("dataval", fldlen);
      }
      return sch;
   }
}
