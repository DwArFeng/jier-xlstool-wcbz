package com.jier.xlstool.wcbz.launcher;

import com.dwarfeng.dutil.develop.backgr.AbstractTask;
import com.jier.xlstool.wcbz.core.control.WCBZ;

/**
 * 启动任务。
 * 
 * @author DwArFeng
 * @since 0.0.0-alpha
 */
class StartTask extends AbstractTask {

	private final WCBZ wcbz;
	private final String[] args;

	public StartTask(WCBZ wcbz, String[] args) {
		this.wcbz = wcbz;
		this.args = args;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		wcbz.getActionManager().start(args);
	}

}
