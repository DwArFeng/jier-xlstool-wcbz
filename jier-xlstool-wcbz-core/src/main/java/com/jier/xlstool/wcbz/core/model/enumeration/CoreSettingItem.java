package com.jier.xlstool.wcbz.core.model.enumeration;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dwarfeng.dutil.develop.setting.AbstractSettingInfo;
import com.dwarfeng.dutil.develop.setting.SettingEnumItem;
import com.dwarfeng.dutil.develop.setting.SettingInfo;
import com.dwarfeng.dutil.develop.setting.info.LocaleSettingInfo;

public enum CoreSettingItem implements SettingEnumItem {

	/** 国际化地区。 */
	I18N_LOCALE("i18n.locale", new LocaleSettingInfo("zh_CN")),

	/** 源文件的总表所在的表单序号，从0开始计数。 */
	SRCTABLE_INDEX_COUNT_SHEET("srctable.index.count-sheet", new PositiveIntegerSettingInfo("1")),
	/** 源文件的第一行数据行，从0开始计数。 */
	SRCTABLE_INDEX_ROW_FIRST_DATA("scrtable.index.row.first-data", new PositiveIntegerSettingInfo("3")),

	/** 导出文件的第一行表头行，从0开始计数。 */
	EXPTABLE_INDEX_EXPORT_SHEET("exptable.index.row.export-sheet", new PositiveIntegerSettingInfo("0")),
	/** 导出文件的第一行数据行，从0开始计数。 */
	EXPTABLE_INDEX_ROW_FIRST_DATA("exptable.index.row.first-data", new PositiveIntegerSettingInfo("3")),

	/** 源文件的检测数据所在的列，从0开始计数。 */
	SRCTABLE_INDEX_COLUMN_CHECK("srctable.index.column.check", new PositiveIntegerSettingInfo("0")),
	/** 源文件的职工部门所在的列，从0开始计数。 */
	SRCTABLE_INDEX_COLUMN_DEPARTMENT("srctable.index.column.department", new PositiveIntegerSettingInfo("3")),
	/** 源文件的员工工号所在的列，从0开始计数。 */
	SRCTABLE_INDEX_COLUMN_WORK_NUMBER("srctable.index.column.work-number", new PositiveIntegerSettingInfo("4")),
	/** 源文件的员工姓名所在的列，从0开始计数。 */
	SRCTABLE_INDEX_COLUMN_STUFF_NAME("srctable.index.column.stuff-name", new PositiveIntegerSettingInfo("5")),
	/** 源文件的缺勤统计所在的列，从0开始计数。 */
	SRCTABLE_INDEX_COLUMN_ABSENCE_COUNT("srctable.index.column.absence-count", new PositiveIntegerSettingInfo("8")),

	/** 源文件的职工部门所在的列，从0开始计数。 */
	EXPTABLE_INDEX_COLUMN_DEPARTMENT("exptable.index.column.department", new PositiveIntegerSettingInfo("1")),
	/** 源文件的员工工号所在的列，从0开始计数。 */
	EXPTABLE_INDEX_COLUMN_WORK_NUMBER("exptable.index.column.work-number", new PositiveIntegerSettingInfo("2")),
	/** 源文件的员工姓名所在的列，从0开始计数。 */
	EXPTABLE_INDEX_COLUMN_STUFF_NAME("exptable.index.column.stuff-name", new PositiveIntegerSettingInfo("3")),
	/** 源文件的缺勤统计所在的列，从0开始计数。 */
	EXPTABLE_INDEX_COLUMN_ABSENCE_COUNT("exptable.index.column.absence-count", new PositiveIntegerSettingInfo("4")),

	/** 源文件最多统计的行数。 */
	SRCTABLE_POLICY_MAX_LOAD_ROW("srctable.policy.max-load-row", new PositiveIntegerSettingInfo("9999")),

	;

	private static final class PositiveIntegerSettingInfo extends AbstractSettingInfo implements SettingInfo {

		private static final int RADIX = 10;
		private String lastCheckedValue = null;
		private Integer lastParsedValue = null;
		private final Lock lock = new ReentrantLock();

		public PositiveIntegerSettingInfo(String defaultValue) throws NullPointerException, IllegalArgumentException {
			super(defaultValue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return PositiveIntegerSettingInfo.class.hashCode() * 61 + defaultValue.hashCode() * 17;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (Objects.isNull(obj))
				return false;
			if (!(obj.getClass() == PositiveIntegerSettingInfo.class))
				return false;

			PositiveIntegerSettingInfo that = (PositiveIntegerSettingInfo) obj;
			return Objects.equals(this.defaultValue, that.defaultValue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return "PositiveIntegerSettingInfo [defaultValue=" + defaultValue + "]";
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean isNonNullValid(String value) {
			lock.lock();
			try {
				if (Objects.equals(value, lastCheckedValue))
					return Objects.nonNull(lastParsedValue);

				try {
					lastCheckedValue = value;
					lastParsedValue = Integer.parseInt(value, RADIX);
					if (lastParsedValue < 0) {
						lastParsedValue = null;
						return false;
					}
				} catch (Exception e) {
					lastParsedValue = null;
					return false;
				}
				return true;
			} finally {
				lock.unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object parseValidValue(String value) {
			lock.lock();
			try {
				if (Objects.equals(value, lastCheckedValue))
					return lastParsedValue;

				try {
					lastCheckedValue = value;
					lastParsedValue = Integer.parseInt(value, RADIX);
					return lastParsedValue;
				} catch (Exception e) {
					lastCheckedValue = null;
					lastParsedValue = null;
					throw new IllegalStateException();
				}
			} finally {
				lock.unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String parseNonNullObject(Object object) {
			if (!(object instanceof Integer))
				return null;

			if ((Integer) object < 0)
				return null;

			return Integer.toString((int) object, RADIX);
		}

	}

	private final String name;
	private final SettingInfo settingInfo;

	private CoreSettingItem(String name, SettingInfo settingInfo) {
		this.name = name;
		this.settingInfo = settingInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SettingInfo getSettingInfo() {
		return settingInfo;
	}

}
