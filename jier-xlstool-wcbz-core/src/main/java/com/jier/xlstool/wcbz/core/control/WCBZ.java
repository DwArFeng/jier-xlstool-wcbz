package com.jier.xlstool.wcbz.core.control;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dwarfeng.dutil.basic.cna.AttributeComplex;
import com.dwarfeng.dutil.basic.cna.model.DefaultReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.DelegateListModel;
import com.dwarfeng.dutil.basic.cna.model.ListModel;
import com.dwarfeng.dutil.basic.cna.model.ModelUtil;
import com.dwarfeng.dutil.basic.cna.model.SyncListModel;
import com.dwarfeng.dutil.basic.cna.model.SyncReferenceModel;
import com.dwarfeng.dutil.basic.prog.DefaultVersion;
import com.dwarfeng.dutil.basic.prog.RuntimeState;
import com.dwarfeng.dutil.basic.prog.Version;
import com.dwarfeng.dutil.basic.prog.VersionType;
import com.dwarfeng.dutil.develop.backgr.Background;
import com.dwarfeng.dutil.develop.backgr.ExecutorServiceBackground;
import com.dwarfeng.dutil.develop.i18n.DelegateI18nHandler;
import com.dwarfeng.dutil.develop.i18n.I18nUtil;
import com.dwarfeng.dutil.develop.i18n.SyncI18nHandler;
import com.dwarfeng.dutil.develop.logger.DelegateLoggerHandler;
import com.dwarfeng.dutil.develop.logger.LoggerUtil;
import com.dwarfeng.dutil.develop.logger.SyncLoggerHandler;
import com.dwarfeng.dutil.develop.resource.DelegateResourceHandler;
import com.dwarfeng.dutil.develop.resource.ResourceUtil;
import com.dwarfeng.dutil.develop.resource.SyncResourceHandler;
import com.dwarfeng.dutil.develop.setting.DefaultSettingHandler;
import com.dwarfeng.dutil.develop.setting.SettingUtil;
import com.dwarfeng.dutil.develop.setting.SyncSettingHandler;

/**
 * 午餐补助统计核心控制类。
 * 
 * @author DwArFeng
 * @since 0.0.0-alpha
 */
public class WCBZ {

	/** 程序的版本。 */
	public static final Version VERSION = new DefaultVersion.Builder().setType(VersionType.ALPHA)
			.setFirstVersion((byte) 0).setSecondVersion((byte) 0).setThirdVersion((byte) 0).setBuildVersion('A')
			.build();
	/** 程序的实例列表，用于持有引用 */
	private static final Set<WCBZ> INSTANCES = Collections.synchronizedSet(new HashSet<>());

	// --------------------------------------------管理器--------------------------------------------
	/** 模型管理器。 */
	private final ModelManager modelManager = new WCBZModelManager(this);
	/** 动作管理器。 */
	private final ActionManager actionManager = new WCBZControlManager(this);

	// --------------------------------------------模型--------------------------------------------
	/** 后台。 */
	private final Background background = new ExecutorServiceBackground(
			Executors.newFixedThreadPool(4, ExecutorServiceBackground.THREAD_FACTORY),
			Collections.newSetFromMap(new WeakHashMap<>()));
	/** 核心配置模型。 */
	private final SyncSettingHandler coreSettingHandler = SettingUtil.syncSettingHandler(new DefaultSettingHandler());
	/** 模态配置模型。 */
	private final SyncSettingHandler modalSettingHandler = SettingUtil.syncSettingHandler(new DefaultSettingHandler());
	/** 命令行配置模型。 */
	private final SyncSettingHandler cliSettingHandler = SettingUtil.syncSettingHandler(new DefaultSettingHandler());
	/** 记录器接口。 */
	private final SyncLoggerHandler loggerHandler = LoggerUtil.syncLoggerHandler(new DelegateLoggerHandler());
	/** 国际化处理器。 */
	private final SyncI18nHandler i18nHandler = I18nUtil.syncI18nHandler(new DelegateI18nHandler());
	/** 配置处理器。 */
	private final SyncResourceHandler resourceHandler = ResourceUtil.syncResourceHandler(new DelegateResourceHandler());

	/** 将要被打开的文件。 */
	private final SyncReferenceModel<File> file2OpenModel = ModelUtil.syncReferenceModel(new DefaultReferenceModel<>());
	/** 将要被导出的文件。 */
	private final SyncReferenceModel<File> file2ExportModel = ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>());
	/** 员工信息模型。 */
	private final SyncListModel<AttributeComplex> stuffInfoModel = ModelUtil.syncListModel(new DelegateListModel<>());

	// --------------------------------------------控制--------------------------------------------
	/** 程序的退出代码。 */
	private final SyncReferenceModel<Integer> exitCodeRef = com.dwarfeng.dutil.basic.cna.model.ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>(Integer.MIN_VALUE));
	/** 程序的状态。 */
	private final SyncReferenceModel<RuntimeState> runtimeStateRef = com.dwarfeng.dutil.basic.cna.model.ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>(RuntimeState.NOT_START));
	/** 程序的状态锁。 */
	private final Lock runtimeStateLock = new ReentrantLock();
	/** 程序的状态条件 */
	private final Condition runtimeStateCondition = runtimeStateLock.newCondition();

	/**
	 * 新实例。
	 */
	public WCBZ() {
		// 为自己保留引用。
		INSTANCES.add(this);
	}

	/**
	 * 
	 * @throws InterruptedException
	 */
	public void awaitFinish() throws InterruptedException {
		runtimeStateLock.lock();
		try {
			while (runtimeStateRef.get() != RuntimeState.ENDED) {
				runtimeStateCondition.await();
			}
		} finally {
			runtimeStateLock.unlock();
		}
	}

	/**
	 * 
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public boolean awaitFinish(long timeout, TimeUnit unit) throws InterruptedException {
		runtimeStateLock.lock();
		try {
			long nanosTimeout = unit.toNanos(timeout);
			// TODO 此处将 finishedFlag 换成了 isFinished() 方法，在将来的实际运行中确认这样做是否会产生死锁。
			while (runtimeStateRef.get() != RuntimeState.ENDED) {
				if (nanosTimeout > 0)
					nanosTimeout = runtimeStateCondition.awaitNanos(nanosTimeout);
				else
					return false;
			}
			return true;
		} finally {
			runtimeStateLock.unlock();
		}
	}

	/**
	 * @return the actionManager
	 */
	public ActionManager getActionManager() {
		return actionManager;
	}

	/**
	 * 
	 * @return
	 */
	public int getExitCode() {
		return exitCodeRef.get();
	}

	/**
	 * @return the modelManager
	 */
	public ModelManager getModelManager() {
		return modelManager;
	}

	/**
	 * @return the background
	 */
	Background getBackground() {
		return background;
	}

	/**
	 * @return the cliSettingHandler
	 */
	SyncSettingHandler getCliSettingHandler() {
		return cliSettingHandler;
	}

	/**
	 * @return the coreSettingHandler
	 */
	SyncSettingHandler getCoreSettingHandler() {
		return coreSettingHandler;
	}

	/**
	 * @return the exitCodeRef
	 */
	SyncReferenceModel<Integer> getExitCodeRef() {
		return exitCodeRef;
	}

	/**
	 * @return the file2ExportModel
	 */
	SyncReferenceModel<File> getFile2ExportModel() {
		return file2ExportModel;
	}

	/**
	 * @return the file2OpenModel
	 */
	SyncReferenceModel<File> getFile2OpenModel() {
		return file2OpenModel;
	}

	/**
	 * @return the i18nHandler
	 */
	SyncI18nHandler getI18nHandler() {
		return i18nHandler;
	}

	/**
	 * @return the loggerHandler
	 */
	SyncLoggerHandler getLoggerHandler() {
		return loggerHandler;
	}

	/**
	 * @return the modalSettingHandler
	 */
	SyncSettingHandler getModalSettingHandler() {
		return modalSettingHandler;
	}

	/**
	 * @return the resourceHandler
	 */
	SyncResourceHandler getResourceHandler() {
		return resourceHandler;
	}

	/**
	 * @return the runtimeStateCondition
	 */
	Condition getRuntimeStateCondition() {
		return runtimeStateCondition;
	}

	/**
	 * @return the runtimeStateLock
	 */
	Lock getRuntimeStateLock() {
		return runtimeStateLock;
	}

	/**
	 * @return the runtimeStateRef
	 */
	SyncReferenceModel<RuntimeState> getRuntimeStateRef() {
		return runtimeStateRef;
	}

	/**
	 * @return the stuffInfoModel
	 */
	ListModel<AttributeComplex> getStuffInfoModel() {
		return stuffInfoModel;
	}

}
