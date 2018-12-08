package com.jier.xlstool.wcbz.core.control;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.dwarfeng.dutil.basic.cna.AttributeComplex;
import com.dwarfeng.dutil.basic.cna.model.ListModel;
import com.dwarfeng.dutil.basic.cna.model.ModelUtil;
import com.dwarfeng.dutil.basic.cna.model.ReferenceModel;
import com.dwarfeng.dutil.basic.io.LoadFailedException;
import com.dwarfeng.dutil.basic.prog.RuntimeState;
import com.dwarfeng.dutil.develop.backgr.Background;
import com.dwarfeng.dutil.develop.backgr.BackgroundUtil;
import com.dwarfeng.dutil.develop.backgr.Task;
import com.dwarfeng.dutil.develop.i18n.I18nHandler;
import com.dwarfeng.dutil.develop.i18n.I18nUtil;
import com.dwarfeng.dutil.develop.i18n.PropUrlI18nInfo;
import com.dwarfeng.dutil.develop.i18n.SyncI18nHandler;
import com.dwarfeng.dutil.develop.i18n.io.XmlPropFileI18nLoader;
import com.dwarfeng.dutil.develop.logger.LoggerHandler;
import com.dwarfeng.dutil.develop.logger.LoggerUtil;
import com.dwarfeng.dutil.develop.logger.SyncLoggerHandler;
import com.dwarfeng.dutil.develop.logger.SysOutLoggerInfo;
import com.dwarfeng.dutil.develop.logger.io.Log4jLoggerLoader;
import com.dwarfeng.dutil.develop.resource.Resource;
import com.dwarfeng.dutil.develop.resource.SyncResourceHandler;
import com.dwarfeng.dutil.develop.resource.io.XmlJar2FileResourceLoader;
import com.dwarfeng.dutil.develop.setting.SettingHandler;
import com.dwarfeng.dutil.develop.setting.SettingUtil;
import com.dwarfeng.dutil.develop.setting.SyncSettingHandler;
import com.dwarfeng.dutil.develop.setting.io.PropSettingValueLoader;
import com.jier.xlstool.wcbz.core.model.enumeration.CliSettingItem;
import com.jier.xlstool.wcbz.core.model.enumeration.CoreSettingItem;
import com.jier.xlstool.wcbz.core.model.enumeration.I18nKey;
import com.jier.xlstool.wcbz.core.model.enumeration.ModalSettingItem;
import com.jier.xlstool.wcbz.core.model.enumeration.ResourceKey;
import com.jier.xlstool.wcbz.core.model.obverser.ModelObverser;
import com.jier.xlstool.wcbz.core.util.Constants;

class WCBZControlManager implements ActionManager {

	private final WCBZ wcbz;

	public WCBZControlManager(WCBZ wcbz) {
		this.wcbz = wcbz;
	}

	// --------------------------------------------程序控制--------------------------------------------
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(String[] args) throws IllegalStateException, NullPointerException {
		Objects.requireNonNull(args, "入口参数 args 不能为 null。");

		// 检查并设置程序的运行状态未正在运行。
		checkAndSetRuntimeState2Running();
		try {
			// 在置被读取之前，首先使用内置的功能模块。
			applyBuiltinFunctionBeforeApplyConfig();
			// 通知应用程序正在启动。
			info(I18nKey.LOGGER_1);
			parseCliOption(args);
			// 应用程序配置。
			applyConfig();
			// 启动GUI
			runGUI();
			// TODO Auto-generated method stub
		} catch (Exception e) {
			e.printStackTrace();
			// TODO 紧急退出机制。
		}
	}

	/**
	 * 
	 * @throws IllegalStateException
	 */
	private void checkAndSetRuntimeState2Running() throws IllegalStateException {
		ReferenceModel<RuntimeState> runtimeStateRef = wcbz.getRuntimeStateRef();
		Lock runtimeStateLock = wcbz.getRuntimeStateLock();
		Condition runtimeStateCondition = wcbz.getRuntimeStateCondition();

		if (runtimeStateRef.get() != RuntimeState.NOT_START) {
			throw new IllegalStateException("禁止多次启动程序。");
		}

		runtimeStateLock.lock();
		try {
			runtimeStateRef.set(RuntimeState.RUNNING);
			runtimeStateCondition.signalAll();
		} finally {
			runtimeStateLock.unlock();
		}
	}

	/**
	 * 在配置被读取之前，首先使用内置的功能模块。
	 */
	private void applyBuiltinFunctionBeforeApplyConfig() {
		SyncLoggerHandler loggerHandler = wcbz.getLoggerHandler();
		SyncI18nHandler i18nHandler = wcbz.getI18nHandler();

		SysOutLoggerInfo defaultLoggerInfo = new SysOutLoggerInfo(null, false);
		PropUrlI18nInfo defaultI18nInfo = new PropUrlI18nInfo(null, "初始化用国际化配置",
				WCBZ.class.getResource(Constants.JPATH_DEFAULT_I18N_PROP_FILE));

		loggerHandler.getLock().writeLock().lock();
		try {
			loggerHandler.clear();
			loggerHandler.add(defaultLoggerInfo);
			loggerHandler.use(defaultLoggerInfo);
		} finally {
			loggerHandler.getLock().writeLock().unlock();
		}

		i18nHandler.getLock().writeLock().lock();
		try {
			i18nHandler.clear();
			i18nHandler.add(defaultI18nInfo);
			i18nHandler.setCurrentLocale(null);
		} finally {
			i18nHandler.getLock().writeLock().unlock();
		}
	}

	private void parseCliOption(String[] args) throws NullPointerException {
		SyncSettingHandler cliSettingHandler = wcbz.getCliSettingHandler();

		info(I18nKey.LOGGER_2);

		// 生成程序的命令行选项。
		Options options = new Options();
		options.addOption(Option.builder(Constants.CLI_OPT_FLAG_CONFIG_FORCE_RESET).hasArg(false).build());

		cliSettingHandler.getLock().writeLock().lock();
		try {
			cliSettingHandler.clear();
			SettingUtil.putEnumItems(CliSettingItem.class, cliSettingHandler);
			// 解析命令行。
			CommandLine commandLine = new DefaultParser().parse(options, args);
			// 判断是否需要强制复位配置文件。
			if (commandLine.hasOption(Constants.CLI_OPT_FLAG_CONFIG_FORCE_RESET)) {
				cliSettingHandler.setParsedValue(CliSettingItem.FLAG_CONFIG_FORCE_RESET, true);
			} else {
				cliSettingHandler.setParsedValue(CliSettingItem.FLAG_CONFIG_FORCE_RESET, false);
			}
		} catch (ParseException e) {
			warn(I18nKey.LOGGER_3, e);
		} finally {
			cliSettingHandler.getLock().writeLock().unlock();
		}
	}

	private void applyConfig() throws Exception {
		// 定义命令行变量。
		SyncSettingHandler cliSettingHandler = wcbz.getCliSettingHandler();
		final Boolean flag_forceReset = cliSettingHandler.getParsedValidValue(CliSettingItem.FLAG_CONFIG_FORCE_RESET,
				Boolean.class);

		// 加载配置信息。
		loadResource(flag_forceReset);
		// 加载记录器配置。
		loadLoggerHandler();
		// 加器国际化配置。
		loadI18nHandler();
		// 加载核心配置。
		loadCoreSettingHandler();
		// 应用核心配置
		applyCoreSetting();
		// 加载模态配置。
		loadModalSettingHandler();
		// 应用模态配置
		applyModalSetting();
	}

	private InputStream openResource(ResourceKey resourceKey) throws IOException {
		Resource resource = wcbz.getResourceHandler().get(resourceKey.getName());
		try {
			return resource.openInputStream();
		} catch (IOException e) {
			formatWarn(I18nKey.LOGGER_10, e, resourceKey.getName());
			resource.reset();
			return resource.openInputStream();
		}
	}

	private void loadResource(Boolean flag_forceReset) {
		SyncResourceHandler resourceHandler = wcbz.getResourceHandler();

		info(I18nKey.LOGGER_4);
		resourceHandler.getLock().writeLock().lock();
		try {
			resourceHandler.clear();
			// 如果在此处出现异常，则程序无法加载最基本的配置列表，但程序仍然可以继续运行。
			InputStream in;
			try {
				in = WCBZ.class.getResource(Constants.JPATH_RESOURCE_LIST).openStream();
			} catch (IOException e) {
				formatError(I18nKey.LOGGER_5, e);
				return;
			}
			Set<LoadFailedException> eptSet = new LinkedHashSet<>();
			try (XmlJar2FileResourceLoader loader = new XmlJar2FileResourceLoader(in, flag_forceReset)) {
				eptSet.addAll(loader.countinuousLoad(resourceHandler));
			} catch (IOException e) {
				formatWarn(I18nKey.LOGGER_6, e, in.toString());
			}
			for (LoadFailedException e : eptSet) {
				warn(I18nKey.LOGGER_7, e);
			}
		} finally {
			resourceHandler.getLock().writeLock().unlock();
		}
	}

	private void loadLoggerHandler() {
		SyncLoggerHandler loggerHandler = wcbz.getLoggerHandler();

		info(I18nKey.LOGGER_8);
		loggerHandler.getLock().writeLock().lock();
		try {
			loggerHandler.clear();
			InputStream in;
			try {
				in = openResource(ResourceKey.LOGGER_SETTING);
			} catch (IOException e) {
				error(I18nKey.LOGGER_9, e);
				return;
			}
			Set<LoadFailedException> eptSet = new LinkedHashSet<>();
			try (Log4jLoggerLoader loader = new Log4jLoggerLoader(in)) {
				eptSet.addAll(loader.countinuousLoad(loggerHandler));
			} catch (IOException e) {
				formatWarn(I18nKey.LOGGER_6, e, in.toString());
			}
			for (LoadFailedException e : eptSet) {
				warn(I18nKey.LOGGER_11, e);
			}
			loggerHandler.useAll();
		} finally {
			loggerHandler.getLock().writeLock().unlock();
		}
	}

	private void loadI18nHandler() {
		SyncI18nHandler i18nHandler = wcbz.getI18nHandler();

		info(I18nKey.LOGGER_12);
		i18nHandler.getLock().writeLock().lock();
		try {
			i18nHandler.clear();
			InputStream in;
			try {
				in = openResource(ResourceKey.I18N_SETTING);
			} catch (IOException e) {
				error(I18nKey.LOGGER_13, e);
				return;
			}
			Set<LoadFailedException> eptSet = new LinkedHashSet<>();
			try (XmlPropFileI18nLoader loader = new XmlPropFileI18nLoader(in)) {
				eptSet.addAll(loader.countinuousLoad(i18nHandler));
			} catch (IOException e) {
				formatWarn(I18nKey.LOGGER_6, e, in.toString());
			}
			for (LoadFailedException e : eptSet) {
				warn(I18nKey.LOGGER_14, e);
			}
			i18nHandler.setCurrentLocale(null);
		} finally {
			i18nHandler.getLock().writeLock().unlock();
		}
	}

	private void loadCoreSettingHandler() {
		SyncSettingHandler coreSettingHandler = wcbz.getCoreSettingHandler();

		info(I18nKey.LOGGER_15);
		coreSettingHandler.getLock().writeLock().lock();
		try {
			coreSettingHandler.clear();
			SettingUtil.putEnumItems(CoreSettingItem.class, coreSettingHandler);
			InputStream in;
			try {
				in = openResource(ResourceKey.CONFIG);
			} catch (IOException e) {
				error(I18nKey.LOGGER_16, e);
				return;
			}
			Set<LoadFailedException> eptSet = new LinkedHashSet<>();
			try (PropSettingValueLoader loader = new PropSettingValueLoader(in, true)) {
				eptSet.addAll(loader.countinuousLoad(coreSettingHandler));
			} catch (IOException e) {
				formatWarn(I18nKey.LOGGER_6, e, in.toString());
			}
			for (LoadFailedException e : eptSet) {
				warn(I18nKey.LOGGER_17, e);
			}
		} finally {
			coreSettingHandler.getLock().writeLock().unlock();
		}
	}

	private void applyCoreSetting() {
		SyncSettingHandler coreSettingHandler = wcbz.getCoreSettingHandler();
		SyncI18nHandler i18nHandler = wcbz.getI18nHandler();

		Locale i18nLocale;

		coreSettingHandler.getLock().readLock().lock();
		try {
			i18nLocale = coreSettingHandler.getParsedValidValue(CoreSettingItem.I18N_LOCALE, Locale.class);
		} finally {
			coreSettingHandler.getLock().readLock().unlock();
		}

		i18nHandler.getLock().writeLock().lock();
		try {
			i18nHandler.setCurrentLocale(i18nLocale);
		} finally {
			i18nHandler.getLock().writeLock().unlock();
		}
	}

	private void loadModalSettingHandler() {
		SyncSettingHandler modalSettingHandler = wcbz.getModalSettingHandler();

		info(I18nKey.LOGGER_18);
		modalSettingHandler.getLock().writeLock().lock();
		try {
			modalSettingHandler.clear();
			SettingUtil.putEnumItems(ModalSettingItem.class, modalSettingHandler);
			InputStream in;
			try {
				in = openResource(ResourceKey.CONFIG);
			} catch (IOException e) {
				error(I18nKey.LOGGER_19, e);
				return;
			}
			Set<LoadFailedException> eptSet = new LinkedHashSet<>();
			try (PropSettingValueLoader loader = new PropSettingValueLoader(in, true)) {
				eptSet.addAll(loader.countinuousLoad(modalSettingHandler));
			} catch (IOException e) {
				formatWarn(I18nKey.LOGGER_6, e, in.toString());
			}
			for (LoadFailedException e : eptSet) {
				warn(I18nKey.LOGGER_20, e);
			}
		} finally {
			modalSettingHandler.getLock().writeLock().unlock();
		}
	}

	private void applyModalSetting() {
		SyncSettingHandler modalSettingHandler = wcbz.getModalSettingHandler();

		modalSettingHandler.getLock().readLock().lock();
		try {

		} finally {
			modalSettingHandler.getLock().readLock().unlock();
		}

	}

	private void runGUI() {
		// TODO Auto-generated method stub

	}

	// --------------------------------------------模型动作--------------------------------------------
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void submit(Task task) throws NullPointerException {
		Objects.requireNonNull(task, "入口参数 task 不能为 null。");
		wcbz.getBackground().submit(task);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFile2Open(File file) {
		// TODO Auto-generated method stub

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFile2Export(File file) {
		// TODO Auto-generated method stub

	}

	// --------------------------------------------日志输出--------------------------------------------
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void trace(String message) throws NullPointerException {
		Objects.requireNonNull(message, "入口参数 message 不能为 null。");
		wcbz.getLoggerHandler().trace(message);
	}

	private void trace(I18nKey key) throws NullPointerException {
		trace(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void debug(String message) throws NullPointerException {
		Objects.requireNonNull(message, "入口参数 message 不能为 null。");
		wcbz.getLoggerHandler().debug(message);
	}

	private void debug(I18nKey key) throws NullPointerException {
		debug(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void info(String message) throws NullPointerException {
		Objects.requireNonNull(message, "入口参数 message 不能为 null。");
		wcbz.getLoggerHandler().info(message);
	}

	private void info(I18nKey key) throws NullPointerException {
		info(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL));
	}

	private void formatInfo(I18nKey key, Object... args) {
		info(String.format(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL), args));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(String message) throws NullPointerException {
		Objects.requireNonNull(message, "入口参数 message 不能为 null。");
		wcbz.getLoggerHandler().warn(message);
	}

	private void warn(I18nKey key) throws NullPointerException {
		warn(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL));
	}

	private void formatWarn(I18nKey key, Object... args) {
		warn(String.format(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL), args));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void warn(String message, Throwable t) throws NullPointerException {
		Objects.requireNonNull(message, "入口参数 message 不能为 null。");
		Objects.requireNonNull(t, "入口参数 t 不能为 null。");
		wcbz.getLoggerHandler().warn(message, t);
	}

	private void warn(I18nKey key, Throwable t) throws NullPointerException {
		warn(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL), t);
	}

	private void formatWarn(I18nKey key, Throwable t, Object... args) {
		warn(String.format(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL), args), t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void error(String message, Throwable t) throws NullPointerException {
		Objects.requireNonNull(message, "入口参数 message 不能为 null。");
		Objects.requireNonNull(t, "入口参数 t 不能为 null。");
		wcbz.getLoggerHandler().error(message, t);
	}

	private void error(I18nKey key, Throwable t) throws NullPointerException {
		error(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL), t);
	}

	private void formatError(I18nKey key, Throwable t, Object... args) {
		error(String.format(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL), args), t);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fatal(String message, Throwable t) throws NullPointerException {
		Objects.requireNonNull(message, "入口参数 message 不能为 null。");
		Objects.requireNonNull(t, "入口参数 t 不能为 null。");
		wcbz.getLoggerHandler().fatal(message, t);
	}

	private void fatal(I18nKey key, Throwable t) throws NullPointerException {
		fatal(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL), t);
	}

	private String i18nString(I18nKey key) {
		return wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL);
	}

	private String formatI18nString(I18nKey key, Object... args) {
		return String.format(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL), args);
	}

}
