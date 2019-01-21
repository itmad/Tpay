package com.sjk.tpay;

import java.util.ArrayList;
import java.util.List;

public class HookList {

    private List<HookBase> mListHook = new ArrayList<>();

    private static HookList mHookList;

    public synchronized static HookList getInstance() {
        if (mHookList == null) {
            mHookList = new HookList();
        }
        return mHookList;
    }

    public HookList() {
        mListHook.clear();

        //TODO 添加渠道都在这里添加就可以了。
        mListHook.add(HookWechat.getInstance());
        mListHook.add(HookAlipay.getInstance());

        for (HookBase hookBase : mListHook) {
            hookBase.addLocalTaskI();
        }
    }

    public List<HookBase> getmListHook() {
        return mListHook;
    }

}
