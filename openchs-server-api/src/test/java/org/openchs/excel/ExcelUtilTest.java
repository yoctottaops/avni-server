package org.openchs.excel;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExcelUtilTest {
    @Test
    public void getDateFromString() {
        ExcelUtil.getDateFromString("20/Jan/2018");
        ExcelUtil.getDateFromString("2014-Feb-13");
    }
}