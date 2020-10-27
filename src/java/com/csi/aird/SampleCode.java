/*
 * Copyright (c) 2020 CSi Biotech
 * Aird and AirdPro are licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT, MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.csi.aird;

import com.csi.aird.api.DDAParser;
import com.csi.aird.bean.AirdInfo;

public class SampleCode {


    public static void main(String[] args) {
        DDAParser parser = new DDAParser("E:\\data\\Aird\\QE_3_WithZero\\File-1_with_zero.json");
        AirdInfo airdInfo = parser.getAirdInfo();
    }
}