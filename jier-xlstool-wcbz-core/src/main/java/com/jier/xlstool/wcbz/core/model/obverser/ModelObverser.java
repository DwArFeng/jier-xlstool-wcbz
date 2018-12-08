package com.jier.xlstool.wcbz.core.model.obverser;

import com.dwarfeng.dutil.basic.prog.Obverser;
import com.dwarfeng.dutil.develop.backgr.Task;

/**
 * 午餐补助程序观察器。
 * 
 * @author DwArFeng
 * @since 0.0.0-alpha
 */
public interface ModelObverser extends Obverser {

	/**
	 * 通知观察器指定的任务被提交。
	 * 
	 * @param task
	 *            指定的任务。
	 */
	public void fireBackgroundTaskSubmitted(Task task);

	/**
	 * 通知观察器指定的任务开始。
	 * 
	 * @param task
	 *            指定的任务。
	 */
	public void fireBackgroundTaskStarted(Task task);

	/**
	 * 通知观察器指定的任务结束。
	 * 
	 * @param task
	 *            指定的任务。
	 */
	public void fireBackgroundTaskFinished(Task task);

	/**
	 * 通知观察器指定的任务被移除。
	 * 
	 * @param task
	 *            指定的任务。
	 */
	public void fireBackgroundTaskRemoved(Task task);

	/**
	 * 通知观察器后台被关闭。
	 */
	public void fireBackgroundShutDown();

	/**
	 * 通知观察器后台被终结。
	 */
	public void fireBackgroundTerminated();
	
	

}
