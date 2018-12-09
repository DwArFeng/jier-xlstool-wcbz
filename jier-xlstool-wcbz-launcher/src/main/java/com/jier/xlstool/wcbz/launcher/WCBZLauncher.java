package com.jier.xlstool.wcbz.launcher;

import com.jier.xlstool.wcbz.core.control.WCBZ;

/**
 * 午餐补助统计程序启动器。
 * 
 * @author DwArFeng
 * @since 0.0.0-alpha
 */
public class WCBZLauncher {

	public static void main(String[] args) throws InterruptedException {
		WCBZ wcbz = new WCBZ();
		wcbz.getActionManager().submit(new StartTask(wcbz, args));
		wcbz.awaitFinish();
		System.exit(wcbz.getExitCode());
	}

}
