package com.jamesmcnee.redissontransactional;

import org.redisson.api.RLock;
import org.redisson.api.RTransaction;
import org.redisson.api.RedissonClient;
import org.redisson.api.TransactionOptions;
import org.redisson.spring.transaction.RedissonTransactionHolder;
import org.redisson.spring.transaction.RedissonTransactionObject;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.concurrent.TimeUnit;

public class RedissonLockingTransactionManager extends AbstractPlatformTransactionManager implements ResourceTransactionManager {

    private static final long serialVersionUID = -6151310954082124041L;
    private static final String TRANSACTION_LOCK = "TRANSACTION_LOCK";

    private RedissonClient redisson;

    public RedissonLockingTransactionManager(RedissonClient redisson) {
        this.redisson = redisson;
    }

    public RTransaction getCurrentTransaction() {
        RedissonTransactionHolder to = (RedissonTransactionHolder) TransactionSynchronizationManager.getResource(redisson);
        if (to == null) {
            throw new NoTransactionException("No transaction is available for the current thread");
        }
        return to.getTransaction();
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        RedissonTransactionObject transactionObject = new RedissonTransactionObject();

        RedissonTransactionHolder holder = (RedissonTransactionHolder) TransactionSynchronizationManager.getResource(redisson);
        if (holder != null) {
            transactionObject.setTransactionHolder(holder);
        }
        return transactionObject;
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        RedissonTransactionObject transactionObject = (RedissonTransactionObject) transaction;
        return transactionObject.getTransactionHolder() != null;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        RedissonTransactionObject tObject = (RedissonTransactionObject) transaction;

        if (tObject.getTransactionHolder() == null) {
            int timeout = determineTimeout(definition);
            TransactionOptions options = TransactionOptions.defaults();
            if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                options.timeout(timeout, TimeUnit.SECONDS);
            }

            RLock transactionLock = redisson.getFairLock(TRANSACTION_LOCK);
            transactionLock.lock(5, TimeUnit.SECONDS);

            RTransaction trans = redisson.createTransaction(options);
            RedissonTransactionHolder holder = new RedissonTransactionHolder();
            holder.setTransaction(trans);
            tObject.setTransactionHolder(holder);
            TransactionSynchronizationManager.bindResource(redisson, holder);
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        RedissonTransactionObject to = (RedissonTransactionObject) status.getTransaction();
        try {
            to.getTransactionHolder().getTransaction().commit();
            redisson.getFairLock(TRANSACTION_LOCK).unlock();
        } catch (TransactionException e) {
            throw new TransactionSystemException("Unable to commit transaction", e);
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        RedissonTransactionObject to = (RedissonTransactionObject) status.getTransaction();
        try {
            to.getTransactionHolder().getTransaction().rollback();
            redisson.getFairLock(TRANSACTION_LOCK).unlock();
        } catch (TransactionException e) {
            throw new TransactionSystemException("Unable to commit transaction", e);
        }
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        RedissonTransactionObject to = (RedissonTransactionObject) status.getTransaction();
        to.setRollbackOnly(true);
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        TransactionSynchronizationManager.unbindResourceIfPossible(redisson);
        RedissonTransactionObject to = (RedissonTransactionObject) transaction;
        to.getTransactionHolder().setTransaction(null);
    }

    @Override
    public Object getResourceFactory() {
        return redisson;
    }

}
