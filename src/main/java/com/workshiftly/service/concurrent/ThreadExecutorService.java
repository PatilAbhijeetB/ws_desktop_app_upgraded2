/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.workshiftly.service.concurrent;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: ThreadExecutorService
 * 
 * Description: This Service wrapper class will be utilized for concurrent thread management.
 * This wrapper class would be able to used to initialized following thread tasks
 *      -> Immediately executing runnable tasks
 *      -> Execution is scheduled for future time and one time executing
 *      -> Periodic executing runnable tasks
 * 
 * This wrapper class was implemented around core java class (java.util.concurrent.ScheduledThreadPoolExecutor
 * Java doc: https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/ScheduledThreadPoolExecutor.html
 * 
 *  Refer the java doc before changes is done in future
 * 
 */
public class ThreadExecutorService {
    
    // Core pool size - uses fixed size thread pool
    private final static int CORE_POOL_SIZE = 6;
    // singleton object of ThreadExecutorService
    private static ThreadExecutorService executorObject;
    
    // ScheduledThreadPoolExecutor instance variable
    private final ScheduledThreadPoolExecutor poolExecutor;
    // TimeUnit used for schedule task and periodic tasks
    private final TimeUnit timeUnit;
    
    /**
     * Method Name: ThreadExecutorService
     * 
     * Method Type: Constructor
     * 
     * parameters:
     * @param timeUnit - time unit to initialize schedule one time and periodic tasks
     *  
     */
    private ThreadExecutorService(TimeUnit timeUnit) {
        
        this.timeUnit = timeUnit;
        this.poolExecutor = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
        this.poolExecutor.setRemoveOnCancelPolicy(true);
    }
    
    /**
     * Static initialize block for this class 
     */
    static {
        ThreadExecutorService.executorObject = new ThreadExecutorService(TimeUnit.SECONDS);
    }
    
    /**
     * Method Name: getPoolExecutor
     * 
     * Description: getter method for poolExecutor
     * @return 
     */
    private ScheduledThreadPoolExecutor getPoolExecutor() {
        return this.poolExecutor;
    }
    
    /**
     * Method Name: getInstance
     * 
     * Description: Get instance of singleton. if it is not initialize prior, then
     * it is initialized and return
     * @return 
     */
    public static synchronized ThreadExecutorService getInstance() {
        if (executorObject == null) {
            executorObject = new ThreadExecutorService(TimeUnit.SECONDS);
        }
        return executorObject;
    }
    
    /**
     * Method Name: executeTask
     * 
     * Description: execute runnable task which is need to execute immediately
     * 
     * @param task - runnable task
     * 
     * @throws RejectedExecutionException - if task can not be accepted for execution when
     *         executor has been shut down
     * @throws NullPointerException - if command is null
     */
    public static Future<?> executeTask(Runnable task) throws RejectedExecutionException, NullPointerException {
        
        ThreadExecutorService threadExecutor = getInstance();
        ScheduledThreadPoolExecutor poolExecutor = threadExecutor.getPoolExecutor();
        return poolExecutor.submit(task);
    }
    
    /**
     * Method Name: executeScheduledOnetimeTask
     * 
     * Description: execute runnable task which is need to execute future not now, this
     * should be used to execute one time task
     * 
     * @param task - runnable task
     * @param delay - delay of the execution (schedule from now) TimeUnit Seconds
     * 
     * @throws RejectedExecutionException - if task can not be accepted for execution when
     *         executor has been shut down
     * @throws NullPointerException - if command is null
     * 
     * @return ScheduleFutrue object
     */
    public static ScheduledFuture<?> executeScheduledOnetimeTask(Runnable task, long delay) 
            throws RejectedExecutionException, NullPointerException {
        
        ThreadExecutorService threadExecutor = getInstance();
        ScheduledThreadPoolExecutor poolExecutor = threadExecutor.getPoolExecutor();
        return poolExecutor.schedule(task, delay, threadExecutor.timeUnit);
    }
    
    /**
     * Method Name: executePeriodicTask
     * 
     * Description: execute runnable task which is need to execute periodically 
     * 
     * @param task - runnable task
     * @param initailDelay  - initial delay of the execution (schedule from now) TimeUnit Seconds
     * @param period - frequency in seconds
     * 
     * @throws RejectedExecutionException - if task can not be accepted for execution when
     *         executor has been shut down
     * @throws NullPointerException - if command is null
     * 
     * @return ScheduleFutrue object
     */
    public static ScheduledFuture<?> executePeriodicTask(Runnable task, long initailDelay, long period) 
            throws RejectedExecutionException, NullPointerException {
        
        ThreadExecutorService threadExecutor = getInstance();
        ScheduledThreadPoolExecutor poolExecutor = threadExecutor.getPoolExecutor();
        return poolExecutor.scheduleAtFixedRate(task, initailDelay, period, TimeUnit.SECONDS);
    }
    
    /**
     * Method Name: shutDownExecutor
     * 
     * Description: shutdown executor service. In here Attempts to stop all actively executing tasks, 
     * halts the processing of waiting tasks, and returns a list of the tasks that were awaiting execution
     * 
     * @return list of the tasks awaiting to execution
     * @throws SecurityException 
     */
    public static List<Runnable> shutDownExecutor() throws SecurityException {
        
        ThreadExecutorService threadExecutor = getInstance();
        ScheduledThreadPoolExecutor poolExecutor = threadExecutor.getPoolExecutor();
        return poolExecutor.shutdownNow();
    }
    
    public static <T> Future<T> submit(Callable<T> callable) {
        
        ThreadExecutorService threadExecutor = getInstance();
        ScheduledThreadPoolExecutor poolExecutor = threadExecutor.getPoolExecutor();
        return poolExecutor.submit(callable);
    }
}
