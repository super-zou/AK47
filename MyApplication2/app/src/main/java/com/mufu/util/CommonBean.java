package com.mufu.util;


import com.contrarywind.interfaces.IPickerViewData;

import java.util.List;

public class CommonBean implements IPickerViewData {

    /**
     * mainItem : 高新科技
     * subItemList : ["互联网", "电子商务"]
     */

    private String mainItem;
    private List<String> subItemList;

    public String getMainItem() {
        return mainItem;
    }

    public void setMainItem(String mainItem) {
        this.mainItem = mainItem;
    }

    public List<String> getSubItemList() {
        return subItemList;
    }

    public void setSubItemList(List<String> subItemList) {
        this.subItemList = subItemList;
    }

    @Override
    public String getPickerViewText() {
        return this.mainItem;
    }
}

