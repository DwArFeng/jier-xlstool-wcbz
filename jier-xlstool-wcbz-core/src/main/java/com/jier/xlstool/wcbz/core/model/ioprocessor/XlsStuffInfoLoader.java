package com.jier.xlstool.wcbz.core.model.ioprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.dwarfeng.dutil.basic.cna.AttributeComplex;
import com.dwarfeng.dutil.basic.cna.model.ListModel;
import com.dwarfeng.dutil.basic.io.LoadFailedException;
import com.dwarfeng.dutil.basic.io.StreamLoader;
import com.jier.xlstool.wcbz.core.util.Constants;

public class XlsStuffInfoLoader extends StreamLoader<ListModel<AttributeComplex>> {

	private final int dataSheetIndex;
	private final int firstDataRowCount;
	private final int checkColumnIndex;
	private final int departmentColumnIndex;
	private final int workNumberColumnIndex;
	private final int stuffNameColumnIndex;
	private final int absenceCountColumnIndex;
	private final int maxLoadRow;

	private final DataFormatter hssfDataFormatter = new HSSFDataFormatter();

	private boolean readFlag = false;

	public XlsStuffInfoLoader(InputStream in, int dataSheetIndex, int firstDataRowCount, int checkColumnIndex,
			int departmentColumnIndex, int workNumberColumnIndex, int stuffNameColumnIndex, int absenceCountColumnIndex,
			int maxLoadRow) throws IOException {
		super(in);
		this.dataSheetIndex = dataSheetIndex;
		this.firstDataRowCount = firstDataRowCount;
		this.checkColumnIndex = checkColumnIndex;
		this.departmentColumnIndex = departmentColumnIndex;
		this.workNumberColumnIndex = workNumberColumnIndex;
		this.stuffNameColumnIndex = stuffNameColumnIndex;
		this.absenceCountColumnIndex = absenceCountColumnIndex;
		this.maxLoadRow = maxLoadRow;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void load(ListModel<AttributeComplex> stuffInfoModel) throws LoadFailedException, IllegalStateException {
		if (readFlag)
			throw new IllegalStateException("Load method can be called only once");

		Objects.requireNonNull(stuffInfoModel, "入口参数 stuffInfoModel 不能为 null。");

		readFlag = true;

		try (Workbook workbook = new HSSFWorkbook(in)) {
			Sheet sheet = workbook.getSheetAt(dataSheetIndex);

			int currRowIndex = firstDataRowCount;
			while (isDataRow(sheet, currRowIndex) && currRowIndex < maxLoadRow) {
				loadRow(sheet, currRowIndex++, stuffInfoModel);
			}

		} catch (Exception e) {
			throw new LoadFailedException("读取XLS数据表的时候发生异常", e);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<LoadFailedException> countinuousLoad(ListModel<AttributeComplex> stuffInfoModel)
			throws IllegalStateException {
		if (readFlag)
			throw new IllegalStateException("Load method can be called only once");

		Objects.requireNonNull(stuffInfoModel, "入口参数 stuffInfoModel 不能为 null。");

		readFlag = true;

		final Set<LoadFailedException> exceptions = new LinkedHashSet<>();

		try (Workbook workbook = new HSSFWorkbook(in)) {
			Sheet sheet = workbook.getSheetAt(dataSheetIndex);

			int currRowIndex = firstDataRowCount;
			while (isDataRow(sheet, currRowIndex) && currRowIndex < maxLoadRow) {
				try {
					loadRow(sheet, currRowIndex++, stuffInfoModel);
				} catch (Exception e) {
					exceptions.add(new LoadFailedException("读取XLS数据表的时候发生异常", e));
				}
			}

		} catch (Exception e) {
			exceptions.add(new LoadFailedException("读取XLS数据表的时候发生异常", e));
		}

		return exceptions;
	}

	private void loadRow(Sheet sheet, int currRowIndex, ListModel<AttributeComplex> stuffInfoModel) throws Exception {
		Row row = sheet.getRow(currRowIndex);

		Cell departmentCell = Optional.ofNullable(row.getCell(departmentColumnIndex))
				.orElseThrow(() -> new IllegalStateException(String.format("第 %d行的数据有误，请检查", currRowIndex + 1)));
		Cell workNumberCell = Optional.ofNullable(row.getCell(workNumberColumnIndex))
				.orElseThrow(() -> new IllegalStateException(String.format("第 %d行的数据有误，请检查", currRowIndex + 1)));
		Cell stuffNameCell = Optional.ofNullable(row.getCell(stuffNameColumnIndex))
				.orElseThrow(() -> new IllegalStateException(String.format("第 %d行的数据有误，请检查", currRowIndex + 1)));
		Cell absenceCountCell = Optional.ofNullable(row.getCell(absenceCountColumnIndex))
				.orElseThrow(() -> new IllegalStateException(String.format("第 %d行的数据有误，请检查", currRowIndex + 1)));

		String departmentValue = hssfDataFormatter.formatCellValue(departmentCell);
		String workNumberValue = hssfDataFormatter.formatCellValue(workNumberCell);
		String stuffNameValue = hssfDataFormatter.formatCellValue(stuffNameCell);
		double absenceCountValue = absenceCountCell.getNumericCellValue();

		AttributeComplex ac = AttributeComplex.newInstance(new Object[] { //
				Constants.ATTRIBUTE_COMPLEX_MARK_DEPARTMENT, departmentValue, //
				Constants.ATTRIBUTE_COMPLEX_MARK_WORK_NUMBER, workNumberValue, //
				Constants.ATTRIBUTE_COMPLEX_MARK_STUFF_NAME, stuffNameValue, //
				Constants.ATTRIBUTE_COMPLEX_MARK_ABSENCE_COUNT, absenceCountValue,//
		});

		stuffInfoModel.add(ac);
	}

	private boolean isDataRow(Sheet sheet, int currRowIndex) {
		Row row = sheet.getRow(currRowIndex);
		String checkValue = Optional.ofNullable(row.getCell(checkColumnIndex))
				.map(cell -> hssfDataFormatter.formatCellValue(cell)).orElse("");
		return checkValue.isEmpty() ? false : checkValue.matches("\\d+");
	}

}
