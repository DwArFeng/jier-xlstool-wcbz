package com.jier.xlstool.wcbz.core.model.ioprocessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.dwarfeng.dutil.basic.cna.AttributeComplex;
import com.dwarfeng.dutil.basic.cna.model.ListModel;
import com.dwarfeng.dutil.basic.io.SaveFailedException;
import com.dwarfeng.dutil.basic.io.StreamSaver;
import com.jier.xlstool.wcbz.core.util.Constants;

public class XlsStuffInfoSaver extends StreamSaver<ListModel<AttributeComplex>> {

	private final InputStream templateInputstream;
	private final int dataSheetIndex;
	private final int firstDataRowCount;
	private final int departmentColumnIndex;
	private final int workNumberColumnIndex;
	private final int stuffNameColumnIndex;
	private final int absenceCountColumnIndex;

	private boolean saveFlag = false;

	public XlsStuffInfoSaver(OutputStream out, InputStream templateInputstream, int dataSheetIndex,
			int firstDataRowCount, int departmentColumnIndex, int workNumberColumnIndex, int stuffNameColumnIndex,
			int absenceCountColumnIndex) {
		super(out);
		this.templateInputstream = templateInputstream;
		this.dataSheetIndex = dataSheetIndex;
		this.firstDataRowCount = firstDataRowCount;
		this.departmentColumnIndex = departmentColumnIndex;
		this.workNumberColumnIndex = workNumberColumnIndex;
		this.stuffNameColumnIndex = stuffNameColumnIndex;
		this.absenceCountColumnIndex = absenceCountColumnIndex;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void save(ListModel<AttributeComplex> stuffInfoModel) throws SaveFailedException, IllegalStateException {
		Objects.requireNonNull(stuffInfoModel, "入口参数 stuffInfoModel 不能为 null。");

		if (saveFlag)
			throw new IllegalStateException("Save method can be called only once");

		saveFlag = true;

		try (Workbook workbook = loadTemplate()) {
			Sheet sheet = workbook.getSheetAt(dataSheetIndex);

			for (int currentDataIndex = 0; currentDataIndex < stuffInfoModel.size(); currentDataIndex++) {
				saveRow(sheet, currentDataIndex, stuffInfoModel);
			}

			workbook.write(out);

		} catch (Exception e) {
			throw new SaveFailedException("写入XLS数据表的时候发生异常", e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<SaveFailedException> countinuousSave(ListModel<AttributeComplex> stuffInfoModel)
			throws IllegalStateException {
		Objects.requireNonNull(stuffInfoModel, "入口参数 stuffInfoModel 不能为 null。");

		if (saveFlag)
			throw new IllegalStateException("Save method can be called only once");

		final Set<SaveFailedException> exceptions = new LinkedHashSet<>();

		try (Workbook workbook = loadTemplate()) {
			Sheet sheet = workbook.getSheetAt(dataSheetIndex);

			for (int currentDataIndex = 0; currentDataIndex < stuffInfoModel.size(); currentDataIndex++) {
				try {
					saveRow(sheet, currentDataIndex, stuffInfoModel);
				} catch (Exception e) {
					exceptions.add(new SaveFailedException("写入XLS数据表的时候发生异常", e));
				}
			}

			workbook.write(out);

		} catch (Exception e) {
			exceptions.add(new SaveFailedException("写入XLS数据表的时候发生异常", e));
		}

		return exceptions;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		super.close();
		templateInputstream.close();
	}

	private Workbook loadTemplate() throws IOException {
		return new HSSFWorkbook(templateInputstream);
	}

	private void saveRow(Sheet sheet, int currentDataIndex, ListModel<AttributeComplex> stuffInfoModel)
			throws Exception {
		Row row = sheet.getRow(currentDataIndex + firstDataRowCount);

		Cell departmentCell = row.getCell(departmentColumnIndex);
		Cell workNumberCell = row.getCell(workNumberColumnIndex);
		Cell stuffNameCell = row.getCell(stuffNameColumnIndex);
		Cell absenceCountCell = row.getCell(absenceCountColumnIndex);

		AttributeComplex attributeComplex = stuffInfoModel.get(currentDataIndex);

		String departmentValue = attributeComplex.get(Constants.ATTRIBUTE_COMPLEX_MARK_DEPARTMENT, String.class);
		String workNumberValue = attributeComplex.get(Constants.ATTRIBUTE_COMPLEX_MARK_WORK_NUMBER, String.class);
		String stuffNameValue = attributeComplex.get(Constants.ATTRIBUTE_COMPLEX_MARK_STUFF_NAME, String.class);
		double absenceCountValue = attributeComplex.get(Constants.ATTRIBUTE_COMPLEX_MARK_ABSENCE_COUNT, Double.class);

		departmentCell.setCellValue(departmentValue);
		workNumberCell.setCellValue(workNumberValue);
		stuffNameCell.setCellValue(stuffNameValue);
		absenceCountCell.setCellValue(absenceCountValue);
	}

}
