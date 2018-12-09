package com.jier.xlstool.wcbz.core;

import com.jier.xlstool.wcbz.core.control.WCBZ;

public class TestLaunch {

	public static void main(String[] args) throws InterruptedException {
		WCBZ wcbz = new WCBZ();
		wcbz.getActionManager().start(new String[] { "-r" });
		wcbz.awaitFinish();
	}

}
