package edu.berkeley.cs186.database.concurrency;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The LockManager provides allows for table-level locking by keeping
 * track of which transactions own and are waiting for locks on specific tables.
 *
 * THIS CODE IS FOR PROJECT 3.
 */
public class LockManager {
  
  public enum LockType {SHARED, EXCLUSIVE};
  private ConcurrentHashMap<String, Lock> tableNameToLock;
  private WaitsForGraph waitsForGraph;
  
  public LockManager() {
    tableNameToLock = new ConcurrentHashMap<String, Lock>();
    waitsForGraph = new WaitsForGraph();
  }

  /**
   * Acquires a lock on tableName of type lockType for transaction transNum.
   *
   * @param tableName the database to lock on
   * @param transNum the transactions id
   * @param lockType the type of lock
   */
  public void acquireLock(String tableName, long transNum, LockType lockType) {
    if (!this.tableNameToLock.containsKey(tableName)) {
      this.tableNameToLock.put(tableName, new Lock(lockType));
    }
    Lock lock = this.tableNameToLock.get(tableName);

    handlePotentialDeadlock(lock, transNum, lockType);

    lock.acquire(transNum, lockType);

  }

  /**
   * Adds any nodes/edges caused the by the specified LockRequest to
   * this LockManager's WaitsForGraph
   * @param lock the lock on the table that the LockRequest is for
   * @param transNum the transNum of the lock request
   * @param lockType the lockType of the lock request
   */
  private void handlePotentialDeadlock(Lock lock, long transNum, LockType lockType) {
    //TODO: Implement Me!!

    if (lockType.equals(LockType.EXCLUSIVE) || lock.getType().equals(LockType.EXCLUSIVE)) {
      for (Long owners: lock.getOwners()) {
        if (owners == transNum) {
//          return;
          continue;
        }

        if (!waitsForGraph.edgeCausesCycle(transNum, owners)) {
          waitsForGraph.addNode(transNum);
          waitsForGraph.addEdge(transNum, owners);
        }
//        } else {
//          throw new DeadlockException("deadlocked");
//        }
      }
    }
    //doesn't account for shared shared in tests
    return;
  }


  /**
   * Releases transNum's lock on tableName.
   *
   * @param tableName the table that was locked
   * @param transNum the transaction that held the lock
   */
  public void releaseLock(String tableName, long transNum) {
    if (this.tableNameToLock.containsKey(tableName)) {
      Lock lock = this.tableNameToLock.get(tableName);
      lock.release(transNum);
    }
  }

  /**
   * Returns a boolean indicating whether or not transNum holds a lock of type lt on tableName.
   *
   * @param tableName the table that we're checking
   * @param transNum the transaction that we're checking for
   * @param lockType the lock type
   * @return whether the lock is held or not
   */
  public boolean holdsLock(String tableName, long transNum, LockType lockType) {
    if (!this.tableNameToLock.containsKey(tableName)) {
      return false;
    }

    Lock lock = this.tableNameToLock.get(tableName);
    return lock.holds(transNum, lockType);
  }
}
