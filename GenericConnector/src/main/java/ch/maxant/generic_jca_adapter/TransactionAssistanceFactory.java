package ch.maxant.generic_jca_adapter;

import java.io.Serializable;
import java.util.Objects;

import javax.resource.Referenceable;
import javax.resource.ResourceException;

/**
 * The resource injected into say a Servlet or EJB.  Used to bind a resource into
 * the active XA transaction.
 */
public interface TransactionAssistanceFactory extends Serializable, Referenceable {

	/**
     * Get transaction assistant from factory so that a callback can be 
     * bound into the transaction as well as recovery which is controlled
     * by the app server's transaction manager.
     * @exception ResourceException Thrown if an assistant can't be obtained
     */
    public TransactionAssistant getTransactionAssistant() throws ResourceException;

    /** The application must register a callback
     * which can be used to commit or rollback transactions
     * as well as recover unfinished transactions.
     * Normally this is called once during application startup.
     * That way, the callback is available as soon as recovery 
     * might need it, e.g. in the scenario where the server
     * is starting after a crash. */
	public void registerCommitRollbackRecovery(CommitRollbackRecoveryCallback commitRollbackRecoveryCallback);

	/** unregister the callback previously registered.
	 * Note the resource adapter can only ever contain one 
	 * callback per connection factory, and will fail hard
	 * if you don't unregister before re-registering a callback.
	 * Normally this is only called when an application is shutdown. */
	public void unregisterCommitRollbackRecovery();
    
	public static interface CommitRollbackRecoveryCallback {
		
		/** The container will call this function during
		 * recovery which should call the EIS and must return 
		 * transaction IDs which are known to be incomplete (not 
		 * yet committed or rolled back. Note that if the 
		 * Resource Adapter is configured to manage transaction
		 * state internally, then this method will not
		 * be called and can habe an empty implementation. */
		String[] getTransactionsInNeedOfRecovery();

		/** The container will call this function 
		 * to commit a transaction was successful.
		 * The implementation of this method should
		 * call the EIS in order to commit
		 * the transaction. */
		void commit(String txid) throws Exception;
		
		/** The container will call this function 
		 * to rollback an unsuccessful transaction.
		 * The implementation of this method should
		 * call the EIS in order to rollback
		 * the transaction. */
		void rollback(String txid) throws Exception;

		/** Builder enabling use of Java8 SAMs */ 
		public static class Builder {
			private CommitRollbackFunction commit;
			private CommitRollbackFunction rollback;
			private RecoveryFunction recovery;
			public Builder withCommit(CommitRollbackFunction commit){
				this.commit = commit;
				return this;
			}
			public Builder withRollback(CommitRollbackFunction rollback){
				this.rollback = rollback;
				return this;
			}
			public Builder withRecovery(RecoveryFunction recovery){
				this.recovery = recovery;
				return this;
			}
			public CommitRollbackRecoveryCallback build(){
				Objects.requireNonNull(commit, "Please call withCommit(...)");
				Objects.requireNonNull(rollback, "Please call withRollback(...)");
				//recovery is optional, since you can configure adapter to handle state internally
				
				return new CommitRollbackRecoveryCallback(){
					@Override
					public void commit(String txid) throws Exception {
						commit.apply(txid);
					}
					@Override
					public void rollback(String txid) throws Exception {
						rollback.apply(txid);
					}
					@Override
					public String[] getTransactionsInNeedOfRecovery() {
						if(recovery == null){
							return new String[0];
						}else{
							return recovery.getTransactionsInNeedOfRecovery();
						}
					}
				};
			}
			
			public static interface RecoveryFunction {
				String[] getTransactionsInNeedOfRecovery();
			}
			
			public static interface CommitRollbackFunction {
				void apply(String txid) throws Exception;
			}
		}
	}
	
	
}