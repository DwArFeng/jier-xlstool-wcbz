package com.jier.xlstool.wcbz.core.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.dwarfeng.dutil.basic.cna.AttributeComplex;
import com.dwarfeng.dutil.basic.cna.model.ReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.SyncListModel;
import com.dwarfeng.dutil.basic.cna.model.SyncReferenceModel;
import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;
import com.dwarfeng.dutil.basic.io.FileUtil;
import com.dwarfeng.dutil.basic.io.LoadFailedException;
import com.dwarfeng.dutil.basic.io.SaveFailedException;
import com.dwarfeng.dutil.basic.mea.TimeMeasurer;
import com.dwarfeng.dutil.basic.prog.RuntimeState;
import com.dwarfeng.dutil.develop.backgr.Background;
import com.dwarfeng.dutil.develop.backgr.Task;
import com.dwarfeng.dutil.develop.i18n.PropUrlI18nInfo;
import com.dwarfeng.dutil.develop.i18n.SyncI18nHandler;
import com.dwarfeng.dutil.develop.i18n.io.XmlPropFileI18nLoader;
import com.dwarfeng.dutil.develop.logger.SyncLoggerHandler;
import com.dwarfeng.dutil.develop.logger.SysOutLoggerInfo;
import com.dwarfeng.dutil.develop.logger.io.Log4jLoggerLoader;
import com.dwarfeng.dutil.develop.resource.Resource;
import com.dwarfeng.dutil.develop.resource.SyncResourceHandler;
import com.dwarfeng.dutil.develop.resource.io.ResourceResetPolicy;
import com.dwarfeng.dutil.develop.resource.io.XmlJar2FileResourceLoader;
import com.dwarfeng.dutil.develop.setting.SettingUtil;
import com.dwarfeng.dutil.develop.setting.SyncSettingHandler;
import com.dwarfeng.dutil.develop.setting.io.PropSettingValueLoader;
import com.dwarfeng.dutil.develop.setting.io.PropSettingValueSaver;
import com.jier.xlstool.wcbz.core.model.enumeration.CliSettingItem;
import com.jier.xlstool.wcbz.core.model.enumeration.CoreSettingItem;
import com.jier.xlstool.wcbz.core.model.enumeration.I18nKey;
import com.jier.xlstool.wcbz.core.model.enumeration.ModalSettingItem;
import com.jier.xlstool.wcbz.core.model.enumeration.ResourceKey;
import com.jier.xlstool.wcbz.core.model.ioprocessor.XlsStuffInfoLoader;
import com.jier.xlstool.wcbz.core.model.ioprocessor.XlsStuffInfoSaver;
import com.jier.xlstool.wcbz.core.model.struct.StuffInfoComparator;
import com.jier.xlstool.wcbz.core.util.Constants;
import com.jier.xlstool.wcbz.core.view.MainFrame;

class WCBZActionlManager implements ActionManager {

	private final WCBZ wcbz;

	public WCBZActionlManager(WCBZ wcbz) {
		this.wcbz = wcbz;
	}

	// --------------------------------------------程序控制--------------------------------------------
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(String[] args) throws IllegalStateException, NullPointerException {
		Objects.requireNonNull(args, "入口参数 args 不能为 null。");

		// 要求程序的运行状态为未启动。
		requireRuntimeState(RuntimeState.NOT_START);
		try {
			// 在置被读取之前，首先使用内置的功能模块。
			applyBuiltinFunctionBeforeApplyConfig();
			// 通知应用程序正在启动。
			info(I18nKey.LOGGER_1);
			// 解析命令行参数。
			parseCliOption(args);
			// 应用程序配置。
			applyConfig();
			// 启动GUI。
			runGUI();
			// 设置程序的运行状态为正在运行。
			setRuntimeState(RuntimeState.RUNNING);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO 紧急退出机制。
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

	private InputStream openResourceInputStream(ResourceKey resourceKey) throws IOException {
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
			try (XmlJar2FileResourceLoader loader = new XmlJar2FileResourceLoader(in,
					flag_forceReset ? ResourceResetPolicy.ALWAYS : ResourceResetPolicy.AUTO)) {
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
				in = openResourceInputStream(ResourceKey.LOGGER_SETTING);
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
				in = openResourceInputStream(ResourceKey.I18N_SETTING);
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
				in = openResourceInputStream(ResourceKey.CONFIG);
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
				in = openResourceInputStream(ResourceKey.MODAL);
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
		SyncReferenceModel<File> file2LoadModel = wcbz.getFile2LoadModel();
		SyncReferenceModel<File> file2ExportModel = wcbz.getFile2ExportModel();

		boolean isLoadedFileExists;
		File loadedFile;
		boolean isExportedFileExists;
		File exportedFile;

		modalSettingHandler.getLock().readLock().lock();
		try {
			isLoadedFileExists = modalSettingHandler.getParsedValidValue(ModalSettingItem.FLAG_LAST_LOADED_FILE_EXISTS,
					Boolean.class);
			loadedFile = modalSettingHandler.getParsedValidValue(ModalSettingItem.FILE_LAST_LOADED_FILE, File.class);
			isExportedFileExists = modalSettingHandler
					.getParsedValidValue(ModalSettingItem.FLAG_LAST_EXPORTED_FILE_EXISTS, Boolean.class);
			exportedFile = modalSettingHandler.getParsedValidValue(ModalSettingItem.FILE_LAST_EXPORTED_FILE,
					File.class);
		} finally {
			modalSettingHandler.getLock().readLock().unlock();
		}

		if (isLoadedFileExists) {
			file2LoadModel.set(loadedFile);
		}
		if (isExportedFileExists) {
			file2ExportModel.set(exportedFile);
		}
	}

	private void runGUI() {
		info(I18nKey.LOGGER_40);

		SyncReferenceModel<MainFrame> mainFrameRef = wcbz.getMainFrameRef();

		SwingUtil.invokeInEventQueue(() -> {
			try {
				UIManager.setLookAndFeel(new NimbusLookAndFeel());
			} catch (UnsupportedLookAndFeelException ignore) {
			}

			MainFrame mainFrame = new MainFrame(wcbz.getModelManager(), wcbz.getActionManager());
			mainFrame.setVisible(true);
			mainFrameRef.set(mainFrame);
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exit() throws IllegalStateException {
		// 检查并设置程序的运行状态未正在运行。
		requireRuntimeState(RuntimeState.RUNNING);
		try {
			// 通知应用程序正在退出。
			info(I18nKey.LOGGER_21);
			// 保存程序配置。
			saveConfig();
			// 关闭GUI。
			disposeGUI();
			// 停止后台。
			stopBackground();
			// 设置退出代码。
			setExitCode(0);
			// 将程序的状态设置为已经结束。
			setRuntimeState(RuntimeState.ENDED);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO 紧急退出机制。
		}
	}

	private void saveConfig() {
		// 加载模态配置。
		saveModalSettingHandler();
	}

	private OutputStream openResourceOutputStream(ResourceKey resourceKey) throws IOException {
		Resource resource = wcbz.getResourceHandler().get(resourceKey.getName());
		try {
			return resource.openOutputStream();
		} catch (IOException e) {
			formatWarn(I18nKey.LOGGER_22, e, resourceKey.getName());
			resource.reset();
			return resource.openOutputStream();
		}
	}

	private void saveModalSettingHandler() {
		SyncSettingHandler modalSettingHandler = wcbz.getModalSettingHandler();

		info(I18nKey.LOGGER_23);
		modalSettingHandler.getLock().readLock().lock();
		try {
			OutputStream out;
			try {
				out = openResourceOutputStream(ResourceKey.MODAL);
			} catch (IOException e) {
				error(I18nKey.LOGGER_24, e);
				return;
			}
			Set<SaveFailedException> eptSet = new LinkedHashSet<>();
			try (PropSettingValueSaver saver = new PropSettingValueSaver(out, true)) {
				eptSet.addAll(saver.countinuousSave(modalSettingHandler));
			} catch (IOException e) {
				formatWarn(I18nKey.LOGGER_6, e, out.toString());
			}
			for (SaveFailedException e : eptSet) {
				warn(I18nKey.LOGGER_25, e);
			}
		} finally {
			modalSettingHandler.getLock().readLock().unlock();
		}
	}

	private void disposeGUI() {
		SyncReferenceModel<MainFrame> mainFrameRef = wcbz.getMainFrameRef();

		SwingUtil.invokeInEventQueue(() -> {
			mainFrameRef.get().dispose();
		});
	}

	private void stopBackground() {
		Background background = wcbz.getBackground();

		background.shutdown();
	}

	private void setExitCode(int code) {
		SyncReferenceModel<Integer> exitCodeRef = wcbz.getExitCodeRef();
		exitCodeRef.set(code);
	}

	private void requireRuntimeState(RuntimeState state) {
		ReferenceModel<RuntimeState> runtimeStateRef = wcbz.getRuntimeStateRef();
		RuntimeState currentState = runtimeStateRef.get();

		if (currentState != state) {
			throw new IllegalStateException(String.format("非法的运行状态 %s，应该为 %s", currentState, state));
		}
	}

	private void setRuntimeState(RuntimeState state) {
		ReferenceModel<RuntimeState> runtimeStateRef = wcbz.getRuntimeStateRef();
		Lock runtimeStateLock = wcbz.getRuntimeStateLock();
		Condition runtimeStateCondition = wcbz.getRuntimeStateCondition();

		runtimeStateLock.lock();
		try {
			runtimeStateRef.set(state);
			runtimeStateCondition.signalAll();
		} finally {
			runtimeStateLock.unlock();
		}
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
	public void setFile2Load(File file) {
		wcbz.getFile2LoadModel().set(file);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFile2Export(File file) {
		wcbz.getFile2ExportModel().set(file);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void loadFile() throws IllegalStateException {
		SyncReferenceModel<File> file2OpenModel = wcbz.getFile2LoadModel();

		File file = Optional.ofNullable(file2OpenModel.get())
				.orElseThrow(() -> new IllegalStateException("待读取文件模型中没有文件。"));

		// 记录日志。
		info(I18nKey.LOGGER_26);
		// 读取文件信息持久化。
		setLoadFileModalInfo(file);
		// 定义计时器。
		TimeMeasurer tm = new TimeMeasurer();
		// 开始计时。
		tm.start();
		// 读取文件。
		loadFile0(file);
		// 数据排序。
		sortStuffInfo();
		// 计时结束。
		tm.stop();
		// 输出信息。
		formatInfo(I18nKey.LOGGER_31, tm.getTimeMs());
	}

	private void setLoadFileModalInfo(File file) {
		info(I18nKey.LOGGER_32);

		SyncSettingHandler modalSettingHandler = wcbz.getModalSettingHandler();

		modalSettingHandler.getLock().writeLock().lock();
		try {
			modalSettingHandler.setParsedValue(ModalSettingItem.FLAG_LAST_LOADED_FILE_EXISTS, true);
			modalSettingHandler.setParsedValue(ModalSettingItem.FILE_LAST_LOADED_FILE, file);
		} finally {
			modalSettingHandler.getLock().writeLock().unlock();
		}
	}

	private void loadFile0(File file) {
		info(I18nKey.LOGGER_28);

		SyncSettingHandler coreSettingHandler = wcbz.getCoreSettingHandler();
		SyncListModel<AttributeComplex> stuffInfoModel = wcbz.getStuffInfoModel();

		int dataSheetIndex;
		int firstDataRowCount;
		int checkColumnIndex;
		int departmentColumnIndex;
		int workNumberColumnIndex;
		int stuffNameColumnIndex;
		int absenceCountColumnIndex;
		int maxLoadRow;

		coreSettingHandler.getLock().readLock().lock();
		try {
			dataSheetIndex = coreSettingHandler.getParsedValidValue(CoreSettingItem.SRCTABLE_INDEX_COUNT_SHEET,
					Integer.class);
			firstDataRowCount = coreSettingHandler.getParsedValidValue(CoreSettingItem.SRCTABLE_INDEX_ROW_FIRST_DATA,
					Integer.class);
			checkColumnIndex = coreSettingHandler.getParsedValidValue(CoreSettingItem.SRCTABLE_INDEX_COLUMN_CHECK,
					Integer.class);
			departmentColumnIndex = coreSettingHandler
					.getParsedValidValue(CoreSettingItem.SRCTABLE_INDEX_COLUMN_DEPARTMENT, Integer.class);
			workNumberColumnIndex = coreSettingHandler
					.getParsedValidValue(CoreSettingItem.SRCTABLE_INDEX_COLUMN_WORK_NUMBER, Integer.class);
			stuffNameColumnIndex = coreSettingHandler
					.getParsedValidValue(CoreSettingItem.SRCTABLE_INDEX_COLUMN_STUFF_NAME, Integer.class);
			absenceCountColumnIndex = coreSettingHandler
					.getParsedValidValue(CoreSettingItem.SRCTABLE_INDEX_COLUMN_ABSENCE_COUNT, Integer.class);
			maxLoadRow = coreSettingHandler.getParsedValidValue(CoreSettingItem.SRCTABLE_POLICY_MAX_LOAD_ROW,
					Integer.class);
		} finally {
			coreSettingHandler.getLock().readLock().unlock();
		}

		InputStream in;
		try {
			in = new FileInputStream(file);
		} catch (IOException e) {
			error(I18nKey.LOGGER_28, e);
			return;
		}
		stuffInfoModel.getLock().writeLock().lock();
		try {
			// 存放读取异常的对象。
			Set<LoadFailedException> eptSet = new LinkedHashSet<>();
			try (XlsStuffInfoLoader loader = new XlsStuffInfoLoader(in, dataSheetIndex, firstDataRowCount,
					checkColumnIndex, departmentColumnIndex, workNumberColumnIndex, stuffNameColumnIndex,
					absenceCountColumnIndex, maxLoadRow)) {
				eptSet = loader.countinuousLoad(stuffInfoModel);
				for (LoadFailedException e : eptSet) {
					warn(I18nKey.LOGGER_25, e);
				}
			} catch (IOException e) {
				error(I18nKey.LOGGER_27, e);
			}
		} finally {
			stuffInfoModel.getLock().writeLock().unlock();
		}

	}

	private void sortStuffInfo() {
		info(I18nKey.LOGGER_33);

		SyncListModel<AttributeComplex> stuffInfoModel = wcbz.getStuffInfoModel();

		stuffInfoModel.getLock().writeLock().lock();
		try {
			Collections.sort(stuffInfoModel, new StuffInfoComparator());
		} finally {
			stuffInfoModel.getLock().writeLock().unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exportFile() throws IllegalStateException {
		SyncReferenceModel<File> file2ExportModel = wcbz.getFile2ExportModel();

		File file = Optional.ofNullable(file2ExportModel.get())
				.orElseThrow(() -> new IllegalStateException("待读取文件模型中没有文件。"));

		// 记录日志。
		info(I18nKey.LOGGER_34);
		// 读取文件信息持久化。
		setExportFileModalInfo(file);
		// 定义计时器。
		TimeMeasurer tm = new TimeMeasurer();
		// 开始计时。
		tm.start();
		// 读取文件。
		exportFile0(file);
		// 计时结束。
		tm.stop();
		// 输出信息。
		formatInfo(I18nKey.LOGGER_31, tm.getTimeMs());
	}

	private void setExportFileModalInfo(File file) {
		info(I18nKey.LOGGER_35);

		SyncSettingHandler modalSettingHandler = wcbz.getModalSettingHandler();

		modalSettingHandler.getLock().writeLock().lock();
		try {
			modalSettingHandler.setParsedValue(ModalSettingItem.FLAG_LAST_EXPORTED_FILE_EXISTS, true);
			modalSettingHandler.setParsedValue(ModalSettingItem.FILE_LAST_EXPORTED_FILE, file);
		} finally {
			modalSettingHandler.getLock().writeLock().unlock();
		}
	}

	private void exportFile0(File file) {
		info(I18nKey.LOGGER_36);

		SyncSettingHandler coreSettingHandler = wcbz.getCoreSettingHandler();
		SyncListModel<AttributeComplex> stuffInfoModel = wcbz.getStuffInfoModel();

		int dataSheetIndex;
		int firstDataRowCount;
		int departmentColumnIndex;
		int workNumberColumnIndex;
		int stuffNameColumnIndex;
		int absenceCountColumnIndex;

		coreSettingHandler.getLock().readLock().lock();
		try {
			dataSheetIndex = coreSettingHandler.getParsedValidValue(CoreSettingItem.EXPTABLE_INDEX_EXPORT_SHEET,
					Integer.class);
			firstDataRowCount = coreSettingHandler.getParsedValidValue(CoreSettingItem.EXPTABLE_INDEX_ROW_FIRST_DATA,
					Integer.class);
			departmentColumnIndex = coreSettingHandler
					.getParsedValidValue(CoreSettingItem.EXPTABLE_INDEX_COLUMN_DEPARTMENT, Integer.class);
			workNumberColumnIndex = coreSettingHandler
					.getParsedValidValue(CoreSettingItem.EXPTABLE_INDEX_COLUMN_WORK_NUMBER, Integer.class);
			stuffNameColumnIndex = coreSettingHandler
					.getParsedValidValue(CoreSettingItem.EXPTABLE_INDEX_COLUMN_STUFF_NAME, Integer.class);
			absenceCountColumnIndex = coreSettingHandler
					.getParsedValidValue(CoreSettingItem.EXPTABLE_INDEX_COLUMN_ABSENCE_COUNT, Integer.class);
		} finally {
			coreSettingHandler.getLock().readLock().unlock();
		}

		InputStream in;
		try {
			in = openResourceInputStream(ResourceKey.EXPORT_TEMPLATE);
		} catch (IOException e) {
			error(I18nKey.LOGGER_37, e);
			return;
		}
		OutputStream out;
		try {
			FileUtil.createFileIfNotExists(file);
			out = new FileOutputStream(file);
		} catch (IOException e) {
			error(I18nKey.LOGGER_38, e);
			return;
		}
		stuffInfoModel.getLock().writeLock().lock();
		try {
			// 存放读取异常的对象。
			Set<SaveFailedException> eptSet = new LinkedHashSet<>();
			try (XlsStuffInfoSaver saver = new XlsStuffInfoSaver(out, in, dataSheetIndex, firstDataRowCount,
					departmentColumnIndex, workNumberColumnIndex, stuffNameColumnIndex, absenceCountColumnIndex)) {
				eptSet = saver.countinuousSave(stuffInfoModel);
				for (SaveFailedException e : eptSet) {
					warn(I18nKey.LOGGER_39, e);
				}
			} catch (IOException e) {
				error(I18nKey.LOGGER_27, e);
			}
		} finally {
			stuffInfoModel.getLock().writeLock().unlock();
		}

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

	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
	private void warn(I18nKey key) throws NullPointerException {
		warn(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL));
	}

	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
	private void fatal(I18nKey key, Throwable t) throws NullPointerException {
		fatal(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL), t);
	}

	@SuppressWarnings("unused")
	private String i18nString(I18nKey key) {
		return wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL);
	}

	@SuppressWarnings("unused")
	private String formatI18nString(I18nKey key, Object... args) {
		return String.format(wcbz.getI18nHandler().getStringOrDefault(key, Constants.MISSING_LABEL), args);
	}

}
